package ru.yakovlev.entities.listeners;

import java.util.Objects;
import javax.persistence.PostPersist;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.yakovlev.entities.OrderExecution;
import ru.yakovlev.service.OrderExecutionProcess;

/**
 * Order execution entity event listener.
 *
 * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
 * @since 0.6.0
 */
@NoArgsConstructor
public class OrderExecutionListener {
    private Listener listener;

    @PostPersist
    public void afterPersist(OrderExecution orderExecution) {
        this.init();
        this.listener.afterPersist(orderExecution);
    }

    private void init() {
        if (Objects.isNull(this.listener)) {
            this.listener = new Listener();
        }
    }

    /**
     * Order execution event listener.
     *
     * @author Yakovlev Aleander (sanyakovlev@yandex.ru)
     * @since 0.6.0
     */
    @Configurable
    public static class Listener {
        @Autowired
        private OrderExecutionProcess orderExecutionProcess;

        /**
         * Handle after persist event. Starts the execution process.
         *
         * @param orderExecution order execution.
         */
        public void afterPersist(OrderExecution orderExecution) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @SneakyThrows
                @Override
                public void afterCommit() {
                    orderExecutionProcess.execute(orderExecution);
                }
            });
        }

    }
}
