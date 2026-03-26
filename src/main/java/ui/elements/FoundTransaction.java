package ui.elements;


import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import ui.pages.TransferAgainModal;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.visible;

@Getter
public class FoundTransaction extends BaseElement{

    private String transactionType;
    private Double sum;
    private String foundUnder;

    public FoundTransaction(SelenideElement element) {

        super(element);

        //разбили по отступам и пробелам
        String firstLine = find("span").getText().split("\n")[0];
        String[] parts = firstLine.split(" - \\$");

        this.transactionType = parts[0];
        this.sum = Double.parseDouble(parts[1]);
        this.foundUnder = find("strong").getText();
    }

    private SelenideElement repeatButton() {
        return find("button");
    }

    public TransferAgainModal clickRepeatButton() {
        repeatButton().shouldBe(visible).shouldBe(enabled).click();
        return Selenide.page(TransferAgainModal.class);
    }
}
