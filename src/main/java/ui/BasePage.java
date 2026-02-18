package ui;

import api.models.CreatedUser;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Selenide;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BasePage<T extends BasePage> {


    //header
      /*  SelenideElement noName = $(Selectors.byText("Noname")).shouldBe(visible);
        SelenideElement username = $(Selectors.byText(user.getRequest().getUsername())).shouldBe(visible);
        SelenideElement logoutButton = $(Selectors.byText("\uD83D\uDEAA Logout")).shouldBe(visible);
        //SelenideElement brandNameInHeader = $(Selectors.byText("NoBugs Bank")).shouldBe(visible);*/


    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(String bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert);
        alert.accept();
        return (T) this;
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
