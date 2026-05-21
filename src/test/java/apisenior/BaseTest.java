package apisenior;

import common.extensions.UsersForApiExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(UsersForApiExtension.class)

public abstract class BaseTest {
    protected SoftAssertions soflty;

    @BeforeEach
    public void setupTest() {
        this.soflty = new SoftAssertions();
    }
    @AfterEach
    public void afterTest() {
        soflty.assertAll();
    }
}
