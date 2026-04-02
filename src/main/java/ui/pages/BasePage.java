package ui.pages;

import api.models.CreatedUser;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Selenide;
import common.helpers.AllureAttachments;
import common.helpers.StepLogger;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BasePage<T extends BasePage> {


    public abstract String url();

    public T open() {
        return StepLogger.logUi("Opening a page...", () -> {
            return Selenide.open(url(), (Class<T>) this.getClass());
        });
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(String bankAlert) {
        return StepLogger.log("Checking and accepting alert", () -> {
            Alert alert = switchTo().alert();

            AllureAttachments.attachText("Alert text", bankAlert);
            assertThat(alert.getText()).contains(bankAlert);

            alert.accept();
            return (T) this;

        });
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreatedUser user) {
        authAsUser(user.getRequest().getUsername(), user.getRequest().getPassword());
    }
}
