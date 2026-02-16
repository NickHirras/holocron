package io.holocron.ceremony;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionalWrapper {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void run(Runnable runnable) {
        runnable.run();
    }
}
