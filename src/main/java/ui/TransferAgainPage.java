package ui;

import com.codeborne.selenide.*;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferAgainPage extends BasePage<TransferAgainPage> {
    private SelenideElement searchText = $(Selectors.byText("Search by Username or Name:"));
    private SelenideElement enterNameToFindPlaceholder = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));

    private SelenideElement searchTransactionsButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));
    private SelenideElement matchingTransactionHeader = $(Selectors.byText("Matching Transactions"));

    private SelenideElement homeButton = $(Selectors.byText("\uD83C\uDFE0 Home"));

    private ElementsCollection matchingTransaction = $(Selectors.byClassName("list-group")).findAll("li");
    private SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));


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

    public TransferAgainPage elementsAreVisible() {
        searchText.shouldBe(visible);
        enterNameToFindPlaceholder.shouldBe(visible);
        searchTransactionsButton.shouldBe(visible);
        matchingTransactionHeader.shouldBe(visible);
        homeButton.shouldBe(visible);
        return this;
    }

    public TransferAgainPage findTransactionByName(String name) {
        enterNameToFindPlaceholder.sendKeys(name);
        searchTransactionsButton.click();

        matchingTransaction.findBy(Condition.text("\uD83D\uDD0D Found under: " + "\n" + name));

        return this;
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

    public TransferAgainModal findAndClickTransaction(String type, Double sum) {
        SelenideElement item = matchingTransaction
                .findBy(text(
                        type + " - $" + sum
                ));

        item.$("button").click();

        return Selenide.page(TransferAgainModal.class);
    }
}
