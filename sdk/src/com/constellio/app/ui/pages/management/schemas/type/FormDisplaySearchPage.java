package com.constellio.app.ui.pages.management.schemas.type;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class FormDisplaySearchPage extends PageHelper {

	public FormDisplaySearchPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public Select getOptionsElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-options"));
		return new Select(element);
	}

	public Select getSelectionsElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-selections"));
		return new Select(element);
	}

	public ButtonWebElement getAddButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-add"));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getRemoveButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-remove"));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getUpButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-up"));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getDownButtonElement() {
		ConstellioWebElement element = driver.findElement(By.className("v-listbuilder-button-down"));
		return new ButtonWebElement(element);
	}
}
