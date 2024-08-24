package camp.nextstep.service;

import camp.nextstep.dao.UserDao;
import camp.nextstep.dao.UserHistoryDao;
import camp.nextstep.domain.User;
import camp.nextstep.domain.UserHistory;
import camp.nextstep.jdbc.transaction.TransactionSection;
import com.interface21.beans.factory.annotation.Autowired;
import com.interface21.context.stereotype.Service;

import javax.sql.DataSource;

@Service
public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    @Autowired
    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findByAccount(final String account) {
        return userDao.findByAccount(account);
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void save(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        changePasswordInTransaction(createBy, user);
    }

    private void changePasswordInTransaction(String createBy, User user) {
        try (TransactionSection transactionSection = TransactionSection.from(dataSource)) {
            userDao.update(transactionSection, user);
            userHistoryDao.log(transactionSection, new UserHistory(user, createBy));

            transactionSection.commit();
        }
    }
}
