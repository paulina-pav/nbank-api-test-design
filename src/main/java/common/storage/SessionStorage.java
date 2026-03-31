package common.storage;

import api.models.CreatedUser;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<CreatedUser, UserSteps> userStepsMap = new LinkedHashMap<>();

    private SessionStorage() {

    }

    public static void addUsers(List<CreatedUser> users) {
        for (CreatedUser user: users) {
            INSTANCE.userStepsMap.put(user, new UserSteps());
            //убрала поля и конструктор из UserSteps,
            // оставив методы статическими, потому что это влияет на api тесты
        }
    }

    /**
     * Возвращаем объект CreatedUser по его порядковому номеру в списке созданных пользователей.
     * @param number Порядковый номер, начиная с 1 (а не с 0).
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */
    public static CreatedUser getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number - 1);
    }

    public static CreatedUser getUser() {
        return getUser(1);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number - 1);
    }

    public static UserSteps getSteps() {
        return getSteps(1);
    }

    public static List<CreatedUser> getAllUsers() {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet());
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
    }
}
