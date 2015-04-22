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
package com.constellio.app.ui.tools;

import org.openqa.selenium.By;

import com.constellio.app.ui.pages.search.AdvancedSearchViewImpl;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class AdvancedResearchWebElement {
	
	ConstellioWebElement nestedElement;

	public AdvancedResearchWebElement(ConstellioWebDriver driver) {
		this.nestedElement = driver.findElement(By.className("header-show-advanced-search-button-popup-hidden"));
	}
	
	
	public ResearchResultWebElement advancedResearchThenBatchProcessingWith(ConstellioWebDriver driver, String researchType) {
		displayAdvancedSearchPanelAndSelectIt(driver);
		selectTypeDropDownElement(researchType);
		ResearchResultWebElement resultWebElement = clickSearchButtonAndGetResultWebElement(driver); 
		resultWebElement.toggleFirstCheckBoxInResult();
		resultWebElement.clickBatchProcessingLinkAndRefreshDriver(driver);
		return resultWebElement;
	}	

	
	public ResearchResultWebElement clickSearchButtonAndGetResultWebElement(ConstellioWebDriver driver) {
		ConstellioWebElement button = nestedElement.findElement(By.className("advanced-search-button"));
		button.click();
		driver.waitUntilElementExist(By.className(AdvancedSearchViewImpl.BATCH_PROCESS_BUTTONSTYLE));
		return new ResearchResultWebElement(driver);
	}
	
	public AdvancedResearchWebElement displayAdvancedSearchPanelAndSelectIt(ConstellioWebDriver driver) {
		nestedElement.click();
		nestedElement = driver.waitUntilElementExist(By.className("header-advanced-search-form-content"));
		return this;
	}
	
	public AdvancedResearchWebElement selectTypeDropDownElement(String type) {
		getDropDown().selectItemContainingText(type);
		return this;
	}
	
	public AutocompleteWebElement getDropDown() {
		return new DropDownWebElement(getField("v-filterselect-prompt"));
	}

	public ConstellioWebElement getField(String metadataCode) {
		nestedElement.waitUntilElementExist(By.className(metadataCode));
		return nestedElement.findElement(By.className(metadataCode));
	}		
	
}
