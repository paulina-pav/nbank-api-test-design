package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import ui.pages.TransferAgainPage;

import java.util.List;

public class FoundTransactionList extends BaseElement {

    private final TransferAgainPage page;
    private List<FoundTransaction> transactionList = fromElementCollectionToList();


    public FoundTransactionList(SelenideElement element, TransferAgainPage page) {
        super(element);
        this.page = page;
    }

    //создали elementCollection со всеми транзакциями
    private ElementsCollection transactions() {
        return findAll("li");
    }

    //из elementCollection превратили в массив транзакций
    private List<FoundTransaction> fromElementCollectionToList() {
        return transactions().stream()
                .map(FoundTransaction::new)
                .toList();
    }

    public FoundTransaction searchTransactionByName(Double sum, String transactionType, String name) {
        return StepLogger.logUi("User finds a transaction by name", () -> {
            return transactionList.stream()
                    .filter(t -> t.getSum().equals(sum))
                    .filter(t -> t.getFoundUnder().equals(name))
                    .filter(t -> t.getTransactionType().equals(transactionType))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("There's no transaction with the " + name + " name"));
        });
    }

    public FoundTransaction searchTransactionByUsername(Double sum, String transactionType, String username) {
        return StepLogger.logUi("User finds a transaction by username", () -> {
            return transactionList.stream()
                    .filter(t -> t.getSum().equals(sum))
                    .filter(t -> t.getFoundUnder().equals(username))
                    .filter(t -> t.getTransactionType().equals(transactionType))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("There's no transaction with the " + username + " username"));
        });
    }


    //метод чтобы просто пробежаться по транзакциям которые отображаются без поиска по кнопке
    public FoundTransaction findTransactionByTypeAndSum(String type, Double sum) {
        return StepLogger.logUi("User finds a transaction by type and by sum", () -> {
            return transactionList.stream()
                    .filter(t -> t.getSum().equals(sum))
                    .filter(t -> t.getTransactionType().equals(type))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("There's no transaction with the "
                            + sum + " sum and the " + type + " type"));
        });
    }
}
