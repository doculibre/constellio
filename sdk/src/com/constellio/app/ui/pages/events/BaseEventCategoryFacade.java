package com.constellio.app.ui.pages.events;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.components.DateRangePanel;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.vaadin.TableWebElement;
import com.constellio.app.ui.tools.vaadin.TableWebElement.TableRowWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

public class BaseEventCategoryFacade {
	protected ConstellioWebDriver driver;
	protected ButtonWebElement returnLink;
	private TableWebElement table;
	private List<Integer> statsValues;
	private List<String> statsCaptions;
	private ButtonWebElement okButton;

	public BaseEventCategoryFacade(ConstellioWebDriver driver) {
		this.driver = driver;
	}

	public void returnToPreviousPage() {
		getReturnLink().clickAndWaitForPageReload(10);
	}

	public ButtonWebElement getReturnLink() {
		if(returnLink == null){
			returnLink = new ButtonWebElement(this.driver.findElement(By.className(BaseViewImpl.BACK_BUTTON_CODE)));
		}
		return returnLink;
	}

	public List<Integer> getValues() {
		if (statsValues == null){
			statsValues = new ArrayList<>();
			TableWebElement table = getTable();
			for(int i = 0; i < table.countRows(); i++){
				TableRowWebElement row = table.getRow(i);
				String valueField = row.getValueInColumn(1);
				statsValues.add(Integer.valueOf(valueField));
			}
			return statsValues;
		}
		return statsValues;
	}

	protected TableWebElement getTable() {
		if(this.table == null){
			this.table = new TableWebElement(driver, BaseEventCategoryViewImpl.TABLE_STYLE_CODE);
		}
		return table;
	}

	public String getStatCaption(int i) {
		return getCaptions().get(i);
	}

	public int getStatsCount() {
		return getTable().countRows();
	}

	public List<String> getCaptions() {
		if(statsCaptions == null){
			statsCaptions = new ArrayList<>();
			TableWebElement table = getTable();
			for(int i = 0; i < table.countRows(); i++){
				TableRowWebElement row = table.getRow(i);
				String valueField = row.getValueInColumn(0);
				statsCaptions.add(valueField);
			}
		}
		return statsCaptions;
	}

	public int getStatValue(int statIndex) {
		return getValues().get(statIndex);
	}

	public void clear(){
		this.table = null;
		this.statsCaptions = null;
		this.statsValues = null;
		this.okButton = null;
	}

	public void validateSelection() {
		getValidationButton().click();
	}

	private ButtonWebElement getValidationButton() {
		if(okButton == null){
			okButton = new ButtonWebElement(driver.find(DateRangePanel.OK_BUTTON));
		}
		return okButton;
	}

	public void loadEvent(int tableLineIndex) {
		getTable().getRow(tableLineIndex).getComponentInColumn(2).click();
		waitForPageReload(10);
	}

	protected void waitForPageReload(int i) {
		driver.waitForPageReload(10, driver.getPageLoadTimeAsString(10));
	}
}
