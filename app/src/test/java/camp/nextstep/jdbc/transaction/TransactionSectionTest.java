package camp.nextstep.jdbc.transaction;

import camp.nextstep.config.MyConfiguration;
import camp.nextstep.jdbc.core.JdbcTemplate;
import camp.nextstep.support.jdbc.init.DatabasePopulatorUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

class TransactionSectionTest {
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        final var myConfiguration = new MyConfiguration();
        dataSource = myConfiguration.dataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("insert into users(account, password, email) values('new-account', 'password', 'abc@example.com')");
    }

    @Test
    @DisplayName("동일한 TransactionSection 에서는 동일한 Connection 을 사용한다.")
    void useSameConnectionInOneTransactionSection() {
        try (TransactionSection transactionSection = TransactionSection.from(dataSource)) {
            Connection first = transactionSection.getConnection();
            Connection second = transactionSection.getConnection();
            assertThat(first).isEqualTo(second);
        }
    }

    @Test
    @DisplayName("다른 TransactionSection 에서는 다른 Connection 을 사용한다.")
    void transactionSectionsUseEachConnection() {
        try (TransactionSection transactionSection = TransactionSection.from(dataSource);
             TransactionSection anotherTransactionSection = TransactionSection.from(dataSource)) {
            Connection first = transactionSection.getConnection();
            Connection another = anotherTransactionSection.getConnection();

            assertThat(first).isNotEqualTo(another);
        }
    }

    @Test
    @DisplayName("TransactionSection 이 닫히면 Connection 이 닫힌다.")
    void transactionSectionIsClosable() throws SQLException {
        Connection connection;
        TransactionSection transactionSection = TransactionSection.from(dataSource);
        try (transactionSection) {
            connection = transactionSection.getConnection();

            assertThat(connection.isClosed()).isFalse();
        }
        assertThat(connection.isClosed()).isTrue();
    }
}
