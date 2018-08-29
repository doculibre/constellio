package com.constellio.app.modules.rm.ui.accept.retentionRules;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import org.openqa.selenium.By;

public class AddEditRetentionRulePage extends PageHelper {
	public AddEditRetentionRulePage(ConstellioWebDriver driver) {
		super(driver);
	}

	public AddEditRetentionRulePage navigateToPage() {
		driver.navigateTo().url(RMNavigationConfiguration.ADD_RETENTION_RULE);
		return this;
	}

	public RecordFormWebElement getForm() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(RecordForm.BASE_FORM));
		return new RecordFormWebElement(element);
	}
}
