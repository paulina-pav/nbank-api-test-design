package common.extensions;

import common.annotation.UsersForApiTests;
import common.storage.UserForApiStorage;
import org.junit.jupiter.api.extension.*;


public class UsersForApiExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private UserForApiStorage context;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        UsersForApiTests annotation = extensionContext
                .getRequiredTestMethod()
                .getAnnotation(UsersForApiTests.class);

        context = new UserForApiStorage();

        if (annotation != null) {
            int userCount = annotation.value();

            for (int i = 0; i < userCount; i++) {
                context.createAndAddUserToStorage();
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter()
                .getType()
                .equals(UserForApiStorage.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return context;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        if (context != null) {
            context.clearContextAndDeleteUsers();
        }
    }
}
