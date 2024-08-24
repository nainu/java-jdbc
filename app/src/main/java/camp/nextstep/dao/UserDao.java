package camp.nextstep.dao;

import camp.nextstep.domain.User;
import camp.nextstep.jdbc.core.JdbcTemplate;
import camp.nextstep.jdbc.core.ResultSetExtractor;
import camp.nextstep.jdbc.core.RowMapper;
import camp.nextstep.jdbc.transaction.TransactionSection;
import com.interface21.beans.factory.annotation.Autowired;
import com.interface21.context.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    public void update(TransactionSection transactionSection, final User user) {
        final var sql = "update users set password = ? where id = ?";

        jdbcTemplate.update(transactionSection, sql, user.getPassword(), user.getId());
    }

    public List<User> findAll() {
        final String sql = "select * from users";

        return jdbcTemplate.query(sql, (RowMapper<User>) UserDao::mapRow);
    }

    public User findById(final Long id) {
        final String sql = "select id, account, password, email from users where id = ?";

        return jdbcTemplate.query(sql, (ResultSetExtractor<User>) UserDao::mapRow, id);
    }

    public User findByAccount(final String account) {
        final var sql = "select id, account, password, email from users where account = ?";

        return jdbcTemplate.query(sql, (ResultSetExtractor<User>) UserDao::mapRow, account);
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
