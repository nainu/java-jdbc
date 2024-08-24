package camp.nextstep.dao;

import camp.nextstep.domain.UserHistory;
import camp.nextstep.jdbc.core.JdbcTemplate;
import camp.nextstep.jdbc.transaction.TransactionSection;
import com.interface21.beans.factory.annotation.Autowired;
import com.interface21.context.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(TransactionSection transactionSection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        log.debug("query : {}", sql);

        jdbcTemplate.update(transactionSection,
                            sql,
                            userHistory.getUserId(),
                            userHistory.getAccount(),
                            userHistory.getPassword(),
                            userHistory.getEmail(),
                            userHistory.getCreatedAt(),
                            userHistory.getCreatedBy());
    }
}
