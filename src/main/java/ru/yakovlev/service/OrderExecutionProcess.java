package ru.yakovlev.service;

import javax.persistence.OptimisticLockException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yakovlev.entities.OrderExecution;
import ru.yakovlev.entities.Transfer;
import ru.yakovlev.repositories.OrderExecutionRepository;
import ru.yakovlev.repositories.TransferRepository;

/**
 * Order execution process.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.6.0
 */
@Component
@AllArgsConstructor
@Slf4j
public class OrderExecutionProcess {
    private final OrderExecutionRepository orderExecutionRepository;
    private final TransferRepository transferRepository;
    private final TransactionTemplate transactionTemplate;

    /**
     * The execution process starts.
     *
     * @param orderExecution order execution.
     */
    @Async
    public void execute(final OrderExecution orderExecution) {
        var count = 3;
        while (count-- != 0) {
            try {
                this.transactionTemplate.executeWithoutResult(status -> {
                    val optExecution = this.orderExecutionRepository.findById(orderExecution.getId());
                    if (optExecution.isPresent()) {
                        final var execution = optExecution.get();
                        if (execution.getFromOrder().isCancelled() || execution.getToOrder().isCancelled()) {
                            this.cancelExecution(execution);
                        } else {
                            this.prepare(execution);
                        }

                    }
                });
                break;
            } catch (OptimisticLockException | OptimisticLockingFailureException ex) {
                log.debug("Optimistic exception during order execution");
            }
        }
    }

    private void cancelExecution(final OrderExecution execution) {
        execution.getFromOrder().setOnExecution(false);
        execution.getToOrder().setOnExecution(false);
        this.transferRepository.save(new Transfer(execution, 0));
    }

    private void prepare(final OrderExecution execution) {
        val fromOrder = execution.getFromOrder();
        val toOrder = execution.getToOrder();
        val fromSum = this.transferRepository.sumQuantityOfAllTransfersForOrder(fromOrder.getId());
        val fromLeftover = fromOrder.getQuantity() - fromSum;
        val toSum = this.transferRepository.sumQuantityOfAllTransfersForOrder(toOrder.getId());
        val toLeftover = toOrder.getQuantity() - toSum;
        if (fromLeftover == toLeftover) {
            fromOrder.setOnExecution(false);
            toOrder.setOnExecution(false);
            fromOrder.setFullyExecuted(true);
            toOrder.setFullyExecuted(true);
            fromOrder.setQuantityLeftover(0);
            toOrder.setQuantityLeftover(0);
            this.transferRepository.save(new Transfer(execution, fromLeftover));
        } else if (fromLeftover < toLeftover) {
            fromOrder.setOnExecution(false);
            toOrder.setOnExecution(false);
            fromOrder.setFullyExecuted(true);
            fromOrder.setQuantityLeftover(0);
            toOrder.setQuantityLeftover(toLeftover - fromLeftover);
            this.transferRepository.save(new Transfer(execution, fromLeftover));
        } else {
            fromOrder.setOnExecution(false);
            toOrder.setOnExecution(false);
            toOrder.setFullyExecuted(true);
            fromOrder.setQuantityLeftover(fromLeftover - toLeftover);
            toOrder.setQuantityLeftover(0);
            this.transferRepository.save(new Transfer(execution, toLeftover));
        }
    }

}
