package com.constellio.app.ui.tools.vaadin;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class TableWebElement{

	private static final String CONTENT_TABLE_XPATH = "//table[@class='v-table-table']";
	protected ConstellioWebDriver driver;

	List<TableRowWebElement> rows = new ArrayList<>();

	public TableWebElement(ConstellioWebDriver driver, String tableStyleCode) {
		this.driver = driver;
		loadRows(tableStyleCode);
	}


	private void loadRows(String tableStyleCode) {
		ConstellioWebElement tableParentElement = driver.find(tableStyleCode);
		ConstellioWebElement tableElement = tableParentElement.findElement(By.xpath(CONTENT_TABLE_XPATH));
		List<WebElement> trElements = tableElement.findElements(By.tagName("tr"));
		if(trElements.isEmpty()){
			return;
		}
		for (int i = 0; i < trElements.size(); i++) {
			ConstellioWebElement trElement = (ConstellioWebElement) trElements.get(i);
			TableRowWebElement row;
			row = new TableRowWebElement(trElement, i);
			rows.add(row);
		}
		System.out.println(trElements.size());
	}

	public int countRows() {
		return rows.size();
	}

	public TableRowWebElement getRow(int index) {
		TableRowWebElement row;
		if (rows.size() > index) {
			row = rows.get(index);
		} else {
			row = null;
		}
		return row;
	}

	public static class TableRowWebElement {

		WebElement trElement;

		int index;

		private List<WebElement> columnElements = new ArrayList<>();

		public TableRowWebElement(ConstellioWebElement trElement, int index) {
			trElement.scrollIntoView();
			this.trElement = trElement.getAdaptedElement();
			this.index = index;
			columnElements = trElement.findElements(By.tagName("td"));
		}

		public String getValueInColumn(int index) {
			return getComponentInColumn(index).getText();
		}

		public WebElement getComponentInColumn(int index) {
			return columnElements.get(index);
		}

		public int getIndex() {
			return index;
		}
	}
}
