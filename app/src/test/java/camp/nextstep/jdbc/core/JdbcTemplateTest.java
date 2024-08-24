package camp.nextstep.jdbc.core;

import camp.nextstep.config.MyConfiguration;
import camp.nextstep.domain.User;
import camp.nextstep.jdbc.transaction.TransactionSection;
import camp.nextstep.support.jdbc.init.DatabasePopulatorUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        final var myConfiguration = new MyConfiguration();
        dataSource = myConfiguration.dataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("insert into users(account, password, email) values('new-account', 'password', 'abc@example.com')");
    }

    @Test
    void queryByRowMapper() {
        final List<User> users = jdbcTemplate.query("select * from users", (RowMapper<User>) this::mapUserRow);

        assertThat(users).isNotEmpty()
                .first().hasFieldOrPropertyWithValue("email", "hkkang@woowahan.com");
    }

    @Test
    void queryByResultSetExtractor() {
        final ResultSetExtractor<String> resultSetExtractor = resultSet -> resultSet.getString("email");

        final String email = jdbcTemplate.query("select * from users", resultSetExtractor);

        assertThat(email).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void queryByRowMapperAndPreparedStatementSetter() {
        final PreparedStatementSetter pss = new PreparedStatementSetterImpl(new Object[]{"new-account"});

        final List<User> users = jdbcTemplate.query("select * from users where account = ?", pss, (RowMapper<User>) this::mapUserRow);

        assertThat(users).isNotEmpty()
                .first().hasFieldOrPropertyWithValue("email", "abc@example.com");
    }

    @Test
    void queryByResultSetExtractorAndPreparedStatementSetter() {
        final PreparedStatementSetter pss = new PreparedStatementSetterImpl(new Object[]{"new-account"});

        final User user = jdbcTemplate.query("select * from users where account = ?", pss, (ResultSetExtractor<User>) this::mapUserRow);

        assertThat(user).hasFieldOrPropertyWithValue("email", "abc@example.com");
    }

    @Test
    void testInsertionWithVarargs() {
        final String givenAccount = "new-account";
        final String givenPassword = "new-password";
        final String givenEmail = "test@abc.com";

        final int current = findAllUser().size();

        jdbcTemplate.update("insert into users(account, password, email) values(?,?,?)",
                            givenAccount, givenPassword, givenEmail);

        assertThat(findAllUser()).hasSize(current + 1);
        assertThat(findLastUser()).hasFieldOrPropertyWithValue("account", givenAccount)
                .hasFieldOrPropertyWithValue("password", givenPassword)
                .hasFieldOrPropertyWithValue("email", givenEmail);
    }

    @Test
    void testUpdateWithPreparedStatementSetter() {
        final int current = findAllUser().size();

        final String givenAccount = "some-account";
        final String givenPassword = "newpassword";

        jdbcTemplate.update("update users set account=?, password=? where id=?",
                            new PreparedStatementSetterImpl(new Object[]{givenAccount, givenPassword, 1L}));

        assertThat(findAllUser()).hasSize(current);

        assertThat(findUserById(1L))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("account", givenAccount)
                .hasFieldOrPropertyWithValue("password", givenPassword);
    }

    @Test
    void testUpdateWithConnection() {
        TransactionSection transactionSection = TransactionSection.from(dataSource);
        String givenAccount = "new-account";
        String givenPassword = "new-password";

        jdbcTemplate.update(transactionSection,
                            "update users set account=?, password=? where id=?",
                            givenAccount,
                            givenPassword,
                            1L);
        transactionSection.commit();

        assertThat(findUserById(1L))
                .hasFieldOrPropertyWithValue("account", givenAccount)
                .hasFieldOrPropertyWithValue("password", givenPassword);
    }

    private User mapUserRow(final ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));
    }

    private List<User> findAllUser() {
        return jdbcTemplate.query("select * from users", (RowMapper<User>) this::mapUserRow);
    }

    private User findUserById(@SuppressWarnings("SameParameterValue") final long id) {
        return jdbcTemplate.query("select * from users where id = ?", (ResultSetExtractor<User>) this::mapUserRow, id);
    }

    private User findLastUser() {
        return jdbcTemplate.query("select * from users order by id desc limit 1", (ResultSetExtractor<User>) this::mapUserRow);
    }
}
