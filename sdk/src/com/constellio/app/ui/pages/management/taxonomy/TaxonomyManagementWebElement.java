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
package com.constellio.app.ui.pages.management.taxonomy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.RecordContainerWebElement;
import com.constellio.app.ui.tools.RecordDisplayWebElement;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.google.gwt.dev.util.collect.HashMap;

public class TaxonomyManagementWebElement {

	private ConstellioWebDriver driver;

	public TaxonomyManagementWebElement(ConstellioWebDriver driver) {
		this.driver = driver;
	}

	public void navigateToClassificationPlan() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("taxonomyCode", RMTaxonomies.CLASSIFICATION_PLAN);
		String viewPath = ParamUtils.addParams(NavigatorConfigurationService.TAXONOMY_MANAGEMENT, params);
		driver.navigateTo().url(viewPath);
	}

	public List<String> getConceptsCodesFromTable() {
		RecordContainerWebElement recordContainerWebElement = new RecordContainerWebElement(
				driver.findElement(By.id("childrenTable")));
		List<String> codes = new ArrayList<>();
		for (int i = 0; i < recordContainerWebElement.countRows(); i++) {
			codes.add(recordContainerWebElement.getRow(i).getValueInColumn(0));
		}
		return codes;
	}

	public void display(String code) {
		// Fetch entry in table and click its display button
		RecordContainerWebElement recordContainerWebElement = new RecordContainerWebElement(
				driver.findElement(By.id("childrenTable")));
		recordContainerWebElement.getFirstRowWithValueInColumn(code, 0).clickButton("display-button");
	}

	public String getCurrentConceptCode() {
		return getConceptDisplay().getValue(Category.DEFAULT_SCHEMA + "_" + Category.CODE);
	}

	public String getCurrentConceptTitle() {
		return getConceptDisplay().getValue(Category.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode());
	}

	public String getCurrentConceptCreationDate() {
		return getConceptDisplay().getValue(Category.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode());
	}

	public RecordFormWebElement add() {
		new ButtonWebElement(driver.findElement(By.className("add-taxo-element"))).clickUsingJavascript();
		driver.waitUntilElementExist(By.className(RecordForm.BASE_FORM));
		ConstellioWebElement element = driver.findRequiredElement(By.className(RecordForm.BASE_FORM));
		return new RecordFormWebElement(element);
	}

	public String getCurrentConceptDescription() {
		return getConceptDisplay().getValue(Category.DEFAULT_SCHEMA + "_" + Category.DESCRIPTION);
	}

	RecordDisplayWebElement getConceptDisplay() {
		driver.waitUntilElementExist(By.className("record-display"));
		return new RecordDisplayWebElement(driver.findElement(By.className("record-display")));
	}
}
