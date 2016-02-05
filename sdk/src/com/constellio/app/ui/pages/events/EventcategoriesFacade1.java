package com.constellio.app.ui.pages.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class EventcategoriesFacade1 {
	private final ConstellioWebDriver driver;
	List<BaseEventCategoryFacade> categories;

	public EventcategoriesFacade1(ConstellioWebDriver driver) {
		this.driver = driver;
	}


	public List<BaseEventCategoryFacade> getCategories() {
		if(categories == null){
			initCategories();
		}
		return categories;
	}

	private void initCategories() {
		this.categories = new ArrayList<>();
		for(WebElement webElement : driver.findElements(By.className(EventCategoriesViewImpl.CATEGORY_BUTTON))){
			this.categories.add(new BaseEventCategoryFacade(driver));
		}
	}

	public BaseEventCategoryFacade loadCategory(String categoryTitle) {
		return loadCategory(categoryTitle, null);
	}

	public BaseEventCategoryFacade loadCategory(String categoryTitle, String byIdFieldValue) {
		for(WebElement webElement : driver.findElements(By.className(EventCategoriesViewImpl.CATEGORY_BUTTON))){
			if(getCaption(webElement).equals(categoryTitle)){
				webElement.click();
				waitForPageReload(10);
				clear();
				if (StringUtils.isNotBlank(byIdFieldValue)){
					ByIdEventCategoryFacade returnFacade = new ByIdEventCategoryFacade(driver);
					returnFacade.selectElement(byIdFieldValue);
					returnFacade.validateSelection();
					waitForPageReload(10);
					return returnFacade;
				}else{
					return new BaseEventCategoryFacade(driver);
				}
			}
		}
		throw new UnknownCategoryTitleRuntimeException(categoryTitle);
	}

	protected void waitForPageReload(int i) {
		driver.waitForPageReload(10, driver.getPageLoadTimeAsString(10));
	}

	private String getCaption(WebElement webElement) {
		return webElement.findElement(By.className("v-button-caption")).getText();
	}

	private void clear() {
		this.categories = null;
	}

	public int getStatValue(int statIndex, String categoryTitle, String byIdFieldValue) {
		BaseEventCategoryFacade eventCategoryFacade = loadCategory(categoryTitle, byIdFieldValue);
		int returnValue = eventCategoryFacade.getStatValue(statIndex);
		eventCategoryFacade.returnToPreviousPage();
		return returnValue;
	}

	public int getStatValue(int statIndex, String categoryTitle) {
		return getStatValue(statIndex, categoryTitle, null);
	}

	public void loadEvent(String categoryTitle, int eventIndexInCategory) {
		loadCategory(categoryTitle).loadEvent(eventIndexInCategory);


	}

	private class UnknownCategoryTitleRuntimeException extends RuntimeException {
		public UnknownCategoryTitleRuntimeException(
				String categoryTitle) {
			super(categoryTitle);
		}
	}
}
