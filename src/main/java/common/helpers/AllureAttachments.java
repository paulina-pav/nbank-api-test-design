package common.helpers;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;

public class AllureAttachments {
    private AllureAttachments() {
    }

    public static void attachScreenshot(final String name) {
        if (!WebDriverRunner.hasWebDriverStarted()) {
            return;
        }

        byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver())
                .getScreenshotAs(OutputType.BYTES);

        Allure.addAttachment(
                name,
                "image/png",
                new ByteArrayInputStream(screenshot),
                ".png"
        );
    }

    public static void attachText(final String name, final String content) {
        Allure.addAttachment(name, "text/plain", content);
    }
}
