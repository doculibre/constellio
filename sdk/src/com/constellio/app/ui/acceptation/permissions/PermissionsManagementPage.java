package com.constellio.app.ui.acceptation.permissions;

import org.openqa.selenium.By;

import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.management.permissions.PermissionsManagementViewImpl;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.CheckboxWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class PermissionsManagementPage extends PageHelper {
	protected PermissionsManagementPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public PermissionsManagementPage navigateToPage() {
		driver.navigateTo().url(NavigatorConfigurationService.PERMISSION_MANAGEMENT);
		return new PermissionsManagementPage(driver);
	}

	public CheckboxWebElement getPermission(String roleCode, String permissionCode) {
		String permissionClass = roleCode + "-" + permissionCode.replaceAll("\\.", "_");
		return new CheckboxWebElement(driver.findRequiredElement(By.className(permissionClass)));
	}

	public ButtonWebElement getCreateRoleButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(PermissionsManagementViewImpl.CREATE_ROLE));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getSaveButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(PermissionsManagementViewImpl.SAVE));
		return new ButtonWebElement(element);
	}

	public ButtonWebElement getRevertButton() {
		ConstellioWebElement element = driver.findRequiredElement(By.className(PermissionsManagementViewImpl.REVERT));
		return new ButtonWebElement(element);
	}

	public RecordFormWebElement openCreateRoleForm() {
		getCreateRoleButton().click();
		driver.waitUntilElementExist(By.className(BaseForm.BASE_FORM));
		ConstellioWebElement element = driver.findRequiredElement(By.className(BaseForm.BASE_FORM));
		return new RecordFormWebElement(element);
	}
}
