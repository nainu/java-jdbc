package camp.nextstep.jdbc.transaction;

import camp.nextstep.dao.DataAccessException;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionSection implements Closeable {
    private final DataSource dataSource;
    private Connection connection;

    public static TransactionSection from(DataSource dataSource) {
        TransactionSection transactionSection = new TransactionSection(dataSource);
        transactionSection.initializeConnection();
        return transactionSection;
    }

    private TransactionSection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void initializeConnection() {
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            rollback();
            throw new DataAccessException(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
