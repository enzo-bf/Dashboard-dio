package com.board.configuration;

public interface TransactionExecutor {

    <T> T transactional(ConnectionFactory.SqlFunction<T> work);
}
