package com.constellio.app.ui.acceptation.management.schemas;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.schemas.ListSchemaTypeViewImpl;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement.RecordContainerWebElementRow;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class SchemaTypePage extends PageHelper {
	public static final int TITLE_COLUMN = 0;

	public SchemaTypePage(ConstellioWebDriver driver) {
		super(driver);
	}

	public SchemaTypePage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_SCHEMA_TYPE);
		return this;
	}

	public RecordContainerWebElement getTypeTable() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(ListSchemaTypeViewImpl.TYPE_TABLE));
		return new RecordContainerWebElement(element);
	}

	public RecordContainerWebElementRow getTypeWithTitle(String title) {
		return getTypeTable().getFirstRowWithValueInColumn(title, TITLE_COLUMN);
	}
}
