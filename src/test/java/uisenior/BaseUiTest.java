package uisenior;

import api.configs.Config;
import api.models.NewUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import common.extensions.BrowserMatchExtension;
import common.extensions.UserSessionExtension;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import apisenior.BaseTest;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupUi() {
        Configuration.baseUrl = Config.getProperty("ui.baseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browser.size");
        Configuration.holdBrowserOpen = Boolean.parseBoolean(Config.getProperty("browser.holdOpen"));
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());

        String remoteUrl = Config.getProperty("ui.remote");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            Configuration.remote = remoteUrl;
            Configuration.browserCapabilities.setCapability(
                    "selenoid:options",
                    Map.of("enableVNC", true, "enableLog", true)
            );
        }
    }

    public void authAsUserUi(NewUserRequest user) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(user.getUsername(), user.getPassword());
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

}
/*

 конфигурация для запуска в докере:

 public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.105:3000"; //т.к. селеноид это докер, то для него локал хост другое значение. правая часть -- порт от обычной ссылки
        Configuration.browser = "firefox";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true) //видеть исполнение теста
        );
    }
    */
