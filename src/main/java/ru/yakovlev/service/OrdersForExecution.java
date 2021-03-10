package ru.yakovlev.service;

import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yakovlev.entities.Order;
import ru.yakovlev.repositories.OrderExecutionRepository;
import ru.yakovlev.repositories.OrderRepository;

/**
 * Many orders that can be sent for execution.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.5.0
 */
@AllArgsConstructor
@Slf4j
public class OrdersForExecution {
    private final BlockingQueue<Order> queue;
    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final OrderExecutionRepository orderExecutionRepository;

    /**
     * Starts a new thread for order execution.
     */
    @Async("simpleAsyncTaskExecutor")
    public void startExecutionWorker() {
        log.info("{} worker started to execute orders", Thread.currentThread().getName());
        while (!Thread.interrupted()) {
            Order order = null;
            try {
                order = this.queue.take();
                final var finalOrder = order;
                this.transactionTemplate.executeWithoutResult(status ->
                        new OrderForExecution(finalOrder, this.orderRepository, this.orderExecutionRepository)
                                .sendForExecution());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error("Exception on sending order {} for execution", order, ex);
            }
        }
    }

    /**
     * Notify about a change in the order, it may be possible to send it for execution.
     *
     * @param order changed order.
     * @throws InterruptedException if thread is interrupted.
     */
    public void notify(final Order order) throws InterruptedException {
        if (!(order.isCancelled() || order.isFullyExecuted() || order.isOnExecution())) {
            log.trace("{} order added to the queue for execution", order.getId());
            this.queue.put(order);
        }
    }

    /**
     * Defines orders ready for execution and sends them for execution to order execution workers.
     *
     * @param limit orders limit.
     * @return number of orders sent for execution.
     */
    public int sendToExecution(int limit) {
        val activeOrdersForExecution = this.orderRepository.findOrdersForExecution(limit);
        val result = activeOrdersForExecution.size();
        this.queue.addAll(activeOrdersForExecution);
        log.debug("{} orders were added to the queue for subsequent execution", result);
        return result;
    }

}
