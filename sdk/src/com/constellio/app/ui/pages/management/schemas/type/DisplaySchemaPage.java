package com.constellio.app.ui.pages.management.schemas.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class DisplaySchemaPage extends PageHelper {

	public DisplaySchemaPage(ConstellioWebDriver driver) {
		super(driver);
	}

	void navigateToDisplaySchemaFolderPage() {
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("schemaTypeCode", "folder");
		String params = ParamUtils.addParams(NavigatorConfigurationService.DISPLAY_SCHEMA_TYPE, paramMap);
		driver.navigateTo()
				.url(NavigatorConfigurationService.DISPLAY_SCHEMA + "/" + params);
	}

	public ButtonWebElement getAddButton() {
		return getButtonByClassName(AddButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getEditButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

	public List<ConstellioWebElement> getTableRows() {
		ConstellioWebElement tableElement = driver.findAdaptElements(By.tagName("table")).get(1);
		List<ConstellioWebElement> rows = tableElement.findAdaptElements(By.tagName("tr"));
		return rows;
	}
}
