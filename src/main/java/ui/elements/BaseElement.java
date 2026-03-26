package ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import java.util.List;
import java.util.function.Function;

public abstract class BaseElement {
    protected final SelenideElement element;

    public BaseElement(SelenideElement element) {
        this.element = element;
    }

    public SelenideElement find(By selector) {
        return element.find(selector);
    }

    protected SelenideElement find(String cssSelector) {
        return element.find(cssSelector);

    }

    protected ElementsCollection findAll(String cssSelector) {
        return element.findAll(cssSelector);
    }

    protected ElementsCollection findAll(By selector) {
        return element.findAll(selector);
    }

    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}
