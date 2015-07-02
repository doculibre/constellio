/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.management.configs;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ConfigsManagementViewAcceptTest extends ConstellioTest {
	private static int DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX = 5;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemas;
	ConfigManagementFacade configManagementFacade;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);
		recordServices = getModelLayerFactory().newRecordServices();

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
	}

	@Test
	public void validateLogo()
			throws Exception {
		SystemConfigurationsManager manager = getModelLayerFactory().getSystemConfigurationsManager();
		StreamFactory<InputStream> iconFileStream = getTestResourceInputStreamFactory("binary2.png");
		manager.setValue(ConstellioEIMConfigs.LOGO_LINK, iconFileStream);

		navigateToConfigsManagement();
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void validateDefaultGroupsValues()
			throws Exception {
		navigateToConfigsManagement();
		validateDecommissioningGroupDefaultValues();
		validateUserTitleGroupDefaultValues();
	}

	private void validateDecommissioningGroupDefaultValues() {
		String groupCode = "decommissioning";

		assertThat(configManagementFacade.getConfigGroupSize(groupCode)).isEqualTo(9);
		assertThat(configManagementFacade.getConfigGroupChecboxValue(groupCode, 0)).isEqualTo(true);
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 1)).isEqualTo("-1");
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 2)).isEqualTo("1");
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 3)).isEqualTo("1");
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 4)).isEqualTo("1");
		assertThat(configManagementFacade.getConfigGroupComboboxValue(groupCode, DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX))
				.isEqualTo(
						$("DecommissioningDateBasedOn.CLOSE_DATE"));
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 6)).isEqualTo("12/31");
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 7)).isEqualTo("90");
		assertThat(configManagementFacade.getConfigGroupChecboxValue(groupCode, 8)).isEqualTo(false);
	}

	private void validateUserTitleGroupDefaultValues() {
		String groupCode = "userTitlePattern";
		assertThat(configManagementFacade.getConfigGroupSize(groupCode)).isEqualTo(1);
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 0)).isEqualTo("${firstName} ${lastName}");
	}

	@Test
	public void whenEnumFieldModifiedThenAdequateValueSet()
			throws Exception {
		navigateToConfigsManagement();
		String groupCode = "decommissioning";
		assertThat(configManagementFacade.getConfigGroupComboboxValue(groupCode, DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX))
				.isEqualTo(
						$("DecommissioningDateBasedOn.CLOSE_DATE"));
		configManagementFacade.setConfigGroupComboboxValue(groupCode, DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX, 1);
		configManagementFacade = new ConfigManagementFacade(driver);
		assertThat(configManagementFacade.getConfigGroupComboboxValue(groupCode, DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX))
				.isEqualTo(
						$("DecommissioningDateBasedOn.OPEN_DATE"));
	}

	@Test
	public void whenIntegerFieldModifiedThenAdequateValueSet()
			throws Exception {
		navigateToConfigsManagement();
		String groupCode = "decommissioning";
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 2)).isEqualTo("1");
		configManagementFacade.setConfigGroupValue(groupCode, 2, "12");
		configManagementFacade = new ConfigManagementFacade(driver);
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 2)).isEqualTo("12");
	}

	@Test
	public void whenStringFieldModifiedThenAdequateValueSet()
			throws Exception {
		navigateToConfigsManagement();
		String groupCode = "decommissioning";
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 6)).isEqualTo("12/31");
		configManagementFacade.setConfigGroupValue(groupCode, 6, "10/31");
		configManagementFacade = new ConfigManagementFacade(driver);
		assertThat(configManagementFacade.getConfigGroupInputValue(groupCode, 6)).isEqualTo("10/31");
	}

	@Test
	public void whenBooleanFieldModifiedThenAdequateValueSet()
			throws Exception {
		navigateToConfigsManagement();
		String groupCode = "decommissioning";
		assertThat(configManagementFacade.getConfigGroupChecboxValue(groupCode, 0)).isEqualTo(true);
		configManagementFacade.toggleConfigGroupCheckBox(groupCode, 0);
		configManagementFacade = new ConfigManagementFacade(driver);
		assertThat(configManagementFacade.getConfigGroupChecboxValue(groupCode, 0)).isEqualTo(false);
	}

	@Test
	public void whenSaveButtonClickedAllConfigGroupsSaved()
			throws Exception {
		navigateToConfigsManagement();
		configManagementFacade.setConfigGroupComboboxValue("decommissioning", DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX, 1);
		configManagementFacade.setConfigGroupValue("userTitlePattern", 0, "${firstName}");
		configManagementFacade.save();
		navigateToAudit();
		configManagementFacade = null;
		navigateToConfigsManagement();
		assertThat(
				configManagementFacade.getConfigGroupComboboxValue("decommissioning", DECOMMISSIONING_DATE_BASED_ON_CONFIG_INDEX))
				.isEqualTo(
						$("DecommissioningDateBasedOn.OPEN_DATE"));
		assertThat(configManagementFacade.getConfigGroupInputValue("userTitlePattern", 0)).isEqualTo("${firstName}");
	}

	private void navigateToConfigsManagement() {
		driver.navigateTo().url(NavigatorConfigurationService.CONFIG_MANAGEMENT);
		if (configManagementFacade == null) {
			configManagementFacade = new ConfigManagementFacade(driver);
		}
	}

	private void navigateToAudit() {
		driver.navigateTo().url(NavigatorConfigurationService.EVENTS_LIST);
	}
}
