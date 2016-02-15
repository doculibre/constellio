package com.constellio.app.ui.tools;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class AutocompleteWebElement {
	private final String SUGGEST_POPUP = BaseComboBox.COMBO_BOX_STYLE;
	private final String DIV_XPATH = "//div[contains(@class,'" + SUGGEST_POPUP + "')]";
	private final String ROW_XPATH = DIV_XPATH + "//tr";

	private final ConstellioWebDriver driver;
	private final ConstellioWebElement element;

	public AutocompleteWebElement(ConstellioWebElement element) {
		this.element = element;
		driver = element.getWebDriver();
	}

	public AutocompleteWebElement listTypeAndSelectFirst(String text) {
		getListValues();
		typeAndSelectFirst(text);
		return this;
	}

	public AutocompleteWebElement typeAndSelectFirst(String text) {
		return typeAndSelectFirst(text, 0);
	}

	private AutocompleteWebElement typeAndSelectFirst(String text, int attempt) {

		try {
			element.scrollIntoView();
			clear();
			getInputText().sendKeys(text);
			select(0);

		} catch (Exception e) {
			if (attempt < 9) {
				return typeAndSelectFirst(text, attempt + 1);
			} else {
				throw new RuntimeException(e);
			}
		}

		// This is a workaround for a problem on PhantomJS on OSX
		//		element.click();
		return this;
	}

	public List<String> getListValues() {
		List<String> texts = new ArrayList<>();
		expandOptions();

		List<ConstellioWebElement> elements = driver.waitUntilElementExist(By.xpath(DIV_XPATH)).findAdaptElements(
				By.tagName("tr"));
		for (ConstellioWebElement constellioWebElement : elements) {
			if (!constellioWebElement.getText().trim().isEmpty()) {
				texts.add(constellioWebElement.getText());
			}
		}
		getInputText().sendKeys(Keys.ENTER);
		return texts;
	}

	public AutocompleteWebElement clear() {
		new ButtonWebElement(element.find(LookupField.CLEAR_BUTTON_STYLE_NAME)).click();
		return this;
	}

	public AutocompleteWebElement getEmptyValue() {
		this.selectItemContainingText("   ");
		return this;
	}

	public List<String> type(String text) {
		getInputText().sendKeys(text);
		return getValues();
	}

	public AutocompleteWebElement select(int index) {
		String xpathIndex = "[" + (index + 1) + "]";
		ConstellioWebElement choiceElement = driver.waitUntilElementExist(By.xpath(ROW_XPATH + xpathIndex + "//span"));
		choiceElement.click();
		return this;

	}

	public AutocompleteWebElement selectItemContainingText(String text) {
		this.expandOptions();

		List<ConstellioWebElement> listItem = element.findAdaptElements(By.xpath(ROW_XPATH + "//span"));
		int indexItemContainingText = 0;
		for (int i = 0; i < listItem.size(); i++) {

			if (listItem.get(i).getText().contains(text)) {
				indexItemContainingText = i;
			}
		}

		select(indexItemContainingText);
		return this;
	}

	public AutocompleteWebElement expandOptions() {
		for (int i = 0; i < 10; i++) {
			try {
				element.findElement(By.className("v-filterselect-button")).click();
				element.waitUntilElementExist(By.xpath(ROW_XPATH + "//span"), 300);
				return this;
			} catch (Exception e) {
				// continue trying
			}
		}
		throw new RuntimeException("Could not expand options");
	}

	public String getSelectedValue() {
		return getInputText().getAttribute("value");
	}

	protected ConstellioWebElement getInputText() {
		return element.findElement(By.tagName("input"));
	}

	protected ButtonWebElement getFilterselectButton() {
		ConstellioWebElement filterSelectedButton = element.findElement(By.className("v-filterselect-button"));
		return new ButtonWebElement(filterSelectedButton);
	}

	protected List<String> getValues() {
		driver.waitUntilElementExist(By.className(SUGGEST_POPUP));
		List<String> result = new ArrayList<>();
		for (ConstellioWebElement each : driver.findAdaptElements(By.xpath(ROW_XPATH + "//span"))) {
			result.add(each.getText());
		}
		return result;
	}

	public boolean isEnable() {
		return getInputText().isEnabled();
	}
}
