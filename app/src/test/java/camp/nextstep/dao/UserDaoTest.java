package camp.nextstep.dao;

import camp.nextstep.config.MyConfiguration;
import camp.nextstep.domain.User;
import camp.nextstep.jdbc.core.JdbcTemplate;
import camp.nextstep.jdbc.transaction.TransactionSection;
import camp.nextstep.support.jdbc.init.DatabasePopulatorUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

class UserDaoTest {

    private UserDao userDao;
    private DataSource dataSource;

    @BeforeEach
    void setup() {
        final var myConfiguration = new MyConfiguration();
        dataSource = myConfiguration.dataSource();
        DatabasePopulatorUtils.execute(dataSource);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        userDao = new UserDao(jdbcTemplate);
        final var user = new User("setup-gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");

        final var user2 = userDao.findById(2L);
        assertThat(user2.getAccount()).isEqualTo("setup-gugu");
    }

    @Test
    void findByAccount() {
        final var account = "setup-gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(3L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        TransactionSection transactionSection = TransactionSection.from(dataSource);
        userDao.update(transactionSection, user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
