package common.extensions;

import api.models.CreatedUser;
import api.requests.steps.AdminSteps;
import common.annotation.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback, AfterEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // Шаг 1: проверка, что у теста есть аннотация UserSession
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {

            int userCount = annotation.value();

            SessionStorage.clear();

            List<CreatedUser> users = new LinkedList<>();

            for (int i = 0; i < userCount; i++) {
                CreatedUser user = AdminSteps.createUser();
                users.add(user);
            }

            SessionStorage.addUsers(users);

            int authAsUser = annotation.auth();

            BasePage.authAsUser(SessionStorage.getUser(authAsUser));
        }
    }
    @Override
    public void afterEach(ExtensionContext extensionContext) {
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);
        if (annotation != null) {
            System.out.println("Session users to delete: " + SessionStorage.getAllUsers().size());

            for (CreatedUser user : SessionStorage.getAllUsers()) {
                System.out.println("Deleting session user: " + user.getRequest().getUsername());
                AdminSteps.deletesUser(user.getRequest());
            }

            SessionStorage.clear();
        }
    }
}
