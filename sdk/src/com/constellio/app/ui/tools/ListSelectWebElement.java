package com.constellio.app.ui.tools;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class ListSelectWebElement {
	private final Select select;

	public ListSelectWebElement(ConstellioWebElement element) {
		select = new Select(element.findElement(By.tagName("select")));
	}

	public void select(int i) {
		select.selectByIndex(i);
	}

	public void selectByText(String text) {
		for (WebElement webElement : select.getOptions()) {
			if (webElement.getText().equals(text)) {
				select.selectByValue(webElement.getAttribute("value"));
				break;
			}
		}
	}

	public List<String> getValues() {
		List<String> result = new ArrayList<>();
		List<WebElement> optionsElements = getOtionsWebElement();
		for (WebElement optionsElement : optionsElements) {
			result.add(optionsElement.getText());
		}
		return result;
	}

	private List<WebElement> getOtionsWebElement() {
		return select.getOptions();
	}

	public void deselectAll() {
		select.deselectAll();
	}

	//	public boolean isEnabled() {
	//		return element.findElement(By.tagName("input")).isEnabled();
	//	}
	//
	//	public boolean isChecked() {
	//		return element.findElement(By.tagName("input")).isSelected();
	//	}
	//
	//	public String getCaptionText() {
	//		return element.findElement(By.tagName("label")).getText();
	//	}
	//
	//	public ListSelectWebElement toggle() {
	//		ConstellioWebElement input = element.findElement(By.tagName("input"));
	//		input.click();
	//		return this;
	//	}
}
