package ui;

import api.models.NewUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extensions.UserSessionExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import senior.BaseTest;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(UserSessionExtension.class)
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupLocal() {
        Configuration.baseUrl = "http://localhost:3000";
        Configuration.browser = "firefox";
        Configuration.browserSize = "1400x900"; //"1920x1080";
       //Configuration.holdBrowserOpen = true;
    }

    public void authAsUserUi(NewUserRequest user) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(user.getUsername(), user.getPassword());
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

}
/*  public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.105:3000"; //т.к. селеноид это докер, то для него локал хост другое значение. правая часть -- порт от обычной ссылки
        Configuration.browser = "firefox";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true) //видеть исполнение теста
        );
    }
    */
