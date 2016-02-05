package com.constellio.app.ui.pages.management.collections;

import com.constellio.app.ui.tools.vaadin.BaseFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class AddEditCollectionFacade {
	private final BaseFormWebElement baseFormWebElement;
	private final ConstellioWebElement code;
	private final ConstellioWebElement name;
	ConstellioWebDriver driver;

	public AddEditCollectionFacade(ConstellioWebDriver driver) {
		this.driver = driver;
		this.baseFormWebElement = new BaseFormWebElement(this.driver.find(AddEditCollectionViewImpl.BASE_FORM_STYLE));
		this.code = this.driver.find(AddEditCollectionViewImpl.CODE_FIELD_STYLE);
		this.name = this.driver.find(AddEditCollectionViewImpl.NAME_FIELD_STYLE);

	}

	public void setName(String name) {
		this.name.changeValueTo(name);
	}

	public void cancel() {
		this.baseFormWebElement.cancel();
	}

	public void save() {
		this.baseFormWebElement.ok();
	}

	public void setCode(String code) {
		this.code.changeValueTo(code);
	}

	public String getErrorMessage() {
		ConstellioWebElement error = this.driver.find("v-Notification-error");
		return error.getText();
	}

	public boolean isCodeFieldEnabled() {
		return this.code.isEnabled();
	}

}
