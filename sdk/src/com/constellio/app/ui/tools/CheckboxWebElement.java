package com.constellio.app.ui.tools;

import org.openqa.selenium.By;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class CheckboxWebElement {
	private final ConstellioWebElement element;
	private final String SUGGEST_POPUP = "v-filterselect-suggestmenu";
	private final String DIV_XPATH = "//div[contains(@class,'" + SUGGEST_POPUP + "')]";
	private final String ROW_XPATH = DIV_XPATH + "//tr";

	public CheckboxWebElement(ConstellioWebElement element) {
		this.element = element;
	}

	public boolean isEnabled() {
		return element.findElement(By.tagName("input")).isEnabled();
	}

	public boolean isChecked() {
		return element.findElement(By.tagName("input")).isSelected();
	}

	public String getCaptionText() {
		return element.findElement(By.tagName("label")).getText();
	}

	public CheckboxWebElement toggle() {
		ConstellioWebElement input = element.findElement(By.tagName("input"));
		input.click();
		return this;
	}
}
