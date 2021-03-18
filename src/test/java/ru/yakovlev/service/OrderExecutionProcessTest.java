/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Yakovlev Alexander
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.yakovlev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yakovlev.entities.Order;
import ru.yakovlev.entities.OrderExecution;
import ru.yakovlev.entities.Transfer;
import ru.yakovlev.entities.embedded.OrderType;
import ru.yakovlev.repositories.OrderExecutionRepository;
import ru.yakovlev.repositories.TransferRepository;

@ExtendWith(MockitoExtension.class)
class OrderExecutionProcessTest {
    @Mock
    private OrderExecutionRepository orderExecutionRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void prepareTransactionTemplate() {
        doAnswer((invocation) -> {
            final Consumer<TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(new SimpleTransactionStatus());
            return null;
        }).when(this.transactionTemplate).executeWithoutResult(any());
    }

    @Test
    @DisplayName("When from order is cancelled, then the execution is canceled")
    void whenFromOrderIsCancelledThenExecutionIsCancelled() {
        val fromOrder = new Order(1L, OrderType.BUY, BigDecimal.valueOf(1), 1, true, true, false, 1, 1);
        val toOrder = new Order(2L, OrderType.SELL, BigDecimal.valueOf(1), 2, true, false, false, 2, 1);
        this.checkExecutionIsCancelled(fromOrder, toOrder);
    }

    @Test
    @DisplayName("When from order is cancelled, then the execution is canceled")
    void whenToOrderIsCancelledThenExecutionIsCancelled() {
        val fromOrder = new Order(3L, OrderType.SELL, BigDecimal.valueOf(5), 3, true, false, false, 3, 1);
        val toOrder = new Order(4L, OrderType.BUY, BigDecimal.valueOf(5), 4, true, true, false, 4, 1);
        this.checkExecutionIsCancelled(fromOrder, toOrder);
    }

    @Test
    @DisplayName("When both orders are cancelled, then the execution is canceled")
    void whenBothOrdersIsCancelledThenExecutionIsCancelled() {
        val fromOrder = new Order(5L, OrderType.BUY, BigDecimal.valueOf(9), 6, true, true, false, 6, 1);
        val toOrder = new Order(6L, OrderType.SELL, BigDecimal.valueOf(2), 6, true, true, false, 6, 1);
        this.checkExecutionIsCancelled(fromOrder, toOrder);
    }

    @Test
    @DisplayName("When both orders have the same quantity of goods, then they must be fully executed")
    void whenBothOrdersHaveSameVolumeThenMustBeFullyExecuted() {
        val fromId = 7L;
        val fromOrder = new Order(fromId, OrderType.SELL, BigDecimal.valueOf(3), 7L, true, false, false, 7, 1);
        val toId = 8L;
        val toOrder = new Order(toId, OrderType.BUY, BigDecimal.valueOf(3), 7L, true, false, false, 7, 1);
        val executionId = 2L;
        val orderExecution = new OrderExecution(executionId, fromOrder, toOrder, 1);
        when(this.orderExecutionRepository.findById(orderExecution.getId())).thenReturn(Optional.of(orderExecution));
        when(this.transferRepository.sumQuantityOfAllTransfersForOrder(fromId)).thenReturn(0L);
        when(this.transferRepository.sumQuantityOfAllTransfersForOrder(toId)).thenReturn(0L);
        val executionProcess = new OrderExecutionProcess(
                this.orderExecutionRepository, this.transferRepository, this.transactionTemplate);
        executionProcess.execute(orderExecution);
        assertTrue(fromOrder.isFullyExecuted());
        assertTrue(toOrder.isFullyExecuted());
        assertFalse(fromOrder.isOnExecution());
        assertFalse(toOrder.isOnExecution());
        assertThat(fromOrder.getQuantityLeftover()).isZero();
        assertThat(toOrder.getQuantityLeftover()).isZero();
        val captor = ArgumentCaptor.forClass(Transfer.class);
        Mockito.verify(this.transferRepository).save(captor.capture());
        val transfer = captor.getValue();
        assertThat(transfer.getOrderExecution()).isEqualTo(orderExecution);
        assertThat(transfer.getQuantity()).isEqualTo(7L);
    }

    @Test
    @DisplayName("when toOrder has less remaining quantity of goods, then only it must be fully executed")
    void whenToOrderHasLessQuantityThenOnlyItMustBeFullyExecuted() {
        val fromId = 9L;
        val fromOrder = new Order(fromId, OrderType.BUY, BigDecimal.valueOf(11), 16L, true, false, false, 11, 1);
        val toId = 10L;
        val toOrder = new Order(toId, OrderType.SELL, BigDecimal.valueOf(11), 20L, true, false, false, 8, 1);
        val executionId = 3L;
        val orderExecution = new OrderExecution(executionId, fromOrder, toOrder, 1);
        when(this.orderExecutionRepository.findById(orderExecution.getId())).thenReturn(Optional.of(orderExecution));
        when(this.transferRepository.sumQuantityOfAllTransfersForOrder(fromId)).thenReturn(5L);
        when(this.transferRepository.sumQuantityOfAllTransfersForOrder(toId)).thenReturn(12L);
        val executionProcess = new OrderExecutionProcess(
                this.orderExecutionRepository, this.transferRepository, this.transactionTemplate);
        executionProcess.execute(orderExecution);
        assertTrue(toOrder.isFullyExecuted());
        assertFalse(fromOrder.isOnExecution());
        assertFalse(toOrder.isOnExecution());
        assertThat(fromOrder.getQuantityLeftover()).isEqualTo(3);
        assertThat(toOrder.getQuantityLeftover()).isZero();
        val captor = ArgumentCaptor.forClass(Transfer.class);
        Mockito.verify(this.transferRepository).save(captor.capture());
        val transfer = captor.getValue();
        assertThat(transfer.getOrderExecution()).isEqualTo(orderExecution);
        assertThat(transfer.getQuantity()).isEqualTo(8L);
    }

    @Test
    @DisplayName("when fromOrder has less remaining quantity of goods, then only it must be fully executed")
    void whenFromOrderHasLessQuantityThenOnlyItMustBeFullyExecuted() {
        val fromId = 11L;
        val fromOrder = new Order(fromId, OrderType.BUY, BigDecimal.valueOf(11), 13L, true, false, false, 9, 1);
        val toId = 12L;
        val toOrder = new Order(toId, OrderType.SELL, BigDecimal.valueOf(11), 14L, true, false, false, 12, 1);
        val executionId = 3L;
        val orderExecution = new OrderExecution(executionId, fromOrder, toOrder, 1);
        when(this.orderExecutionRepository.findById(orderExecution.getId())).thenReturn(Optional.of(orderExecution));
        when(this.transferRepository.sumQuantityOfAllTransfersForOrder(fromId)).thenReturn(4L);
        when(this.transferRepository.sumQuantityOfAllTransfersForOrder(toId)).thenReturn(2L);
        val executionProcess = new OrderExecutionProcess(
                this.orderExecutionRepository, this.transferRepository, this.transactionTemplate);
        executionProcess.execute(orderExecution);
        assertTrue(fromOrder.isFullyExecuted());
        assertFalse(fromOrder.isOnExecution());
        assertFalse(toOrder.isOnExecution());
        assertThat(fromOrder.getQuantityLeftover()).isZero();
        assertThat(toOrder.getQuantityLeftover()).isEqualTo(3);
        val captor = ArgumentCaptor.forClass(Transfer.class);
        Mockito.verify(this.transferRepository).save(captor.capture());
        val transfer = captor.getValue();
        assertThat(transfer.getOrderExecution()).isEqualTo(orderExecution);
        assertThat(transfer.getQuantity()).isEqualTo(9L);
    }

    private void checkExecutionIsCancelled(final Order fromOrder, final Order toOrder) {
        val orderExecution = new OrderExecution(1L, fromOrder, toOrder, 1);
        when(this.orderExecutionRepository.findById(1L)).thenReturn(Optional.of(orderExecution));
        val executionProcess = new OrderExecutionProcess(
                this.orderExecutionRepository, this.transferRepository, this.transactionTemplate);
        executionProcess.execute(orderExecution);
        val captor = ArgumentCaptor.forClass(Transfer.class);
        Mockito.verify(this.transferRepository).save(captor.capture());
        val transfer = captor.getValue();
        assertFalse(fromOrder.isOnExecution());
        assertFalse(toOrder.isOnExecution());
        assertThat(transfer.getOrderExecution()).isEqualTo(orderExecution);
        assertEquals(0, transfer.getQuantity());
    }

}