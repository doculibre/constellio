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
