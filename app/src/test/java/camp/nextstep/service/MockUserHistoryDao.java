package camp.nextstep.service;

import camp.nextstep.dao.DataAccessException;
import camp.nextstep.dao.UserHistoryDao;
import camp.nextstep.domain.UserHistory;
import camp.nextstep.jdbc.core.JdbcTemplate;
import camp.nextstep.jdbc.transaction.TransactionSection;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(TransactionSection transactionSection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
