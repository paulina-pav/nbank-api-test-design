package senior;

import api.requests.steps.AdminSteps;
import api.requests.steps.result.CreatedUser;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseTest {
    protected SoftAssertions soflty;
    protected List<CreatedUser> users;

    @BeforeEach
    public void setupTest() {
        this.soflty = new SoftAssertions();
        users = new ArrayList<>();
    }

    protected CreatedUser createUser() {
        CreatedUser user = AdminSteps.createUser();
        users.add(user);
        return user;
    }

    @AfterEach
    public void afterTest() {

        for (CreatedUser user : users) {
            AdminSteps.deletesUser(user.getRequest());
        }

        soflty.assertAll();
    }

}
