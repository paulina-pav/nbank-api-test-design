package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import ui.elements.FoundTransactionList;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferAgainPage extends BasePage<TransferAgainPage> {
    private SelenideElement searchText = $(Selectors.byText("Search by Username or Name:"));
    private SelenideElement enterNameToFindPlaceholder = $(Selectors
            .byAttribute("placeholder", "Enter name to find transactions"));
    private SelenideElement searchTransactionsButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));
    private SelenideElement matchingTransactionHeader = $(Selectors.byText("Matching Transactions"));
    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));
    private SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));


    private final SelenideElement foundTransactionsList = $(Selectors.byClassName("list-group"));


    @Override
    public String url() {
        return "/transfer";
    }

    @Override
    public TransferAgainPage open() {
        Selenide.open(url(), (Class<TransferAgainPage>) this.getClass());
        transferAgainButton.shouldBe(visible);
        transferAgainButton.click();
        return this;
    }

    public TransferAgainPage insertUsername(String username) {
        enterNameToFindPlaceholder.shouldBe(visible).setValue(username);
        return this;
    }

    public TransferAgainPage insertName(String name) {
        enterNameToFindPlaceholder.shouldBe(visible).setValue(name);
        return this;
    }


    //способ попасть в контейнер с транзакциями: напрямую
    public FoundTransactionList getTransactionSection() {
        foundTransactionsList.shouldBe(visible);
        return new FoundTransactionList(foundTransactionsList, this);
    }

    //способ попасть в контейнер с транзакциями: сначала найти по юзернейму если нет имени или только по имени.
    public FoundTransactionList clickSearchButton() {
        for (int i = 0; i < 5; i++) { //нажать несколько раз
            searchTransactionsButton
                    .scrollIntoView(true)
                    .shouldBe(visible, Condition.enabled)
                    .click();
        }
        return new FoundTransactionList(foundTransactionsList, this);
    }

    public TransferMoneyPage clickNewTransferButton() {
        SelenideElement newTransferButton = $(Selectors.byText("\uD83C\uDD95 New Transfer"));
        newTransferButton.click();
        return Selenide.page(TransferMoneyPage.class);
    }

    public UserDashboard clickHomeButton() {
        homeButton.click();
        return Selenide.page(UserDashboard.class);
    }
}
