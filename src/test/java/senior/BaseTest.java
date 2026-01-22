package Tests;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    protected SoftAssertions soflty;

    @BeforeEach
    public void setupTest(){
        this.soflty = new SoftAssertions();
    }
    @AfterEach
    public void afterTest(){
        soflty.assertAll();
    }
}
