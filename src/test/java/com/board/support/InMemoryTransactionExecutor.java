package com.board.support;

import com.board.configuration.ConnectionFactory;
import com.board.configuration.TransactionExecutor;

import java.sql.SQLException;

public class InMemoryTransactionExecutor implements TransactionExecutor {

    @Override
    public <T> T transactional(ConnectionFactory.SqlFunction<T> work) {
        try {
            return work.apply(null);
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
