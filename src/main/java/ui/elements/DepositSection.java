package ui.elements;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import ui.pages.MakeDeposit;


import static com.codeborne.selenide.Condition.visible;

public class DepositSection extends BaseElement {

    private final MakeDeposit page;

    public DepositSection(SelenideElement element, MakeDeposit page) {
        super(element);
        this.page = page;
    }

    public MakeDeposit clickTheDepositButton() {
        find((Selectors.byText("\uD83D\uDCB5 Deposit"))).shouldBe(visible).click();
        return page;
    }

    public DepositSection selectAccount(String accountNumber) {
        SelenideElement dropdown = find(Selectors.byText("-- Choose an account --")).parent().shouldBe(visible);
        dropdown.findAll("option").findBy(Condition.text(accountNumber)).shouldBe(visible).click();

        return this;
    }

    public DepositSection enterAmount(Double amount) {
        String amountString = amount.toString();
        find(Selectors.byAttribute("placeholder", "Enter amount")).shouldBe(visible).sendKeys(amountString);
        return this;
    }
}
