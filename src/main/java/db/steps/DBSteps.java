package db.steps;

import api.common.helpers.StepLogger;
import db.models.AccountDao;
import db.models.TransactionDao;
import db.models.UserDao;
import db.requester.Condition;
import db.requester.RequestSkeleton;
import db.requester.RequestType;

public class DBSteps {

    //найти юзера по юзернему и по имени

    public static UserDao getUserByUsernameAndName(String username, String name) {

        return StepLogger.log("find user:  " + username + " by " + username + " and " + name, () -> {
            UserDao user = RequestSkeleton.builder()
                    .requestType(RequestType.SELECT)
                    .table("customers")
                    .where(Condition.equalTo("username", username))
                    .where(Condition.equalTo("name", name))
                    .extractAs(UserDao.class);

            return user;
        });
    }

    //найти аккаунт по id пользователя и по балансу

    public static AccountDao getAccountByUserIdAndBalance(Integer id, Double balance) {
        return StepLogger.log("find account by:  " + id + " and balance " + balance, () -> {

            AccountDao accountDao = RequestSkeleton.builder()
                    .requestType(RequestType.SELECT)
                    .table("accounts")
                    .where(Condition.equalTo("customer_id", id))
                    .where(Condition.equalTo("balance", balance))
                    .extractAs(AccountDao.class);
            return accountDao;
        });
    }

    public static TransactionDao
    findTransactionByTypeBySumByAccountIdByRelatedAccountId(String type, Double amount,
                                                            Long senderAccount, Long receiverAccount) {

        return StepLogger.log("find transaction by type  " + type + ", by sum " + amount + ", by sender account " + senderAccount + ", by receiver account " +
                receiverAccount, () -> {
            TransactionDao transaction = RequestSkeleton.builder()
                    .requestType(RequestType.SELECT)
                    .table("transactions")
                    .where(Condition.equalTo("type", type))
                    .where(Condition.equalTo("amount", amount))
                    .where(Condition.equalTo("account_id", senderAccount))
                    .where(Condition.equalTo("related_account_id", receiverAccount))
                    .extractAs(TransactionDao.class);
            return transaction;
        });
    }
}
