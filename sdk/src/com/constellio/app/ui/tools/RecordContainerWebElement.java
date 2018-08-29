package com.constellio.app.ui.tools;

import com.constellio.app.ui.tools.RecordContainerWebElementRuntimeException.RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RecordContainerWebElement {

	private static final String HEADER_TD_TAG_XPATH = "//td[starts-with(@class, 'v-table-header-cell')]";
	ConstellioWebElement nestedElement;
	ConstellioWebDriver webDriver;

	List<String> headerTitles = new ArrayList<String>();
	List<RecordContainerWebElementRow> rows = new ArrayList<RecordContainerWebElementRow>();

	public RecordContainerWebElement(ConstellioWebElement nestedElement) {
		this.nestedElement = nestedElement;
		this.webDriver = nestedElement.getWebDriver();
		//webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		nestedElement.click();
		loadHeaderTitles();
		loadRows();
	}

	public int size() {
		return rows.size();
	}

	private void loadHeaderTitles() {
		List<WebElement> headerTdTags = nestedElement
				.findElements(By.xpath(HEADER_TD_TAG_XPATH));//By.className("v-table-header-cell"));//
		for (WebElement headerTdTag : headerTdTags) {
			WebElement headerCaptionContainer = headerTdTag.findElement(By.className("v-table-caption-container"));
			String headerTitle = headerCaptionContainer.getText();
			headerTitles.add(headerTitle);
		}

	}

	private void loadRows() {
		ConstellioWebElement tableElement = nestedElement.findElement(By.className("v-table-table"));
		List<WebElement> trElements = tableElement.findElements(By.tagName("tr"));
		for (int i = 0; i < trElements.size(); i++) {
			ConstellioWebElement trElement = (ConstellioWebElement) trElements.get(i);
			rows.add(new RecordContainerWebElementRow(trElement, i));
		}
	}

	public List<String> getHeaderTitles() {
		return headerTitles;
	}

	public int countRows() {
		return rows.size();
	}

	public RecordContainerWebElementRow getRow(int index) {
		RecordContainerWebElementRow row;
		if (rows.size() > index) {
			row = rows.get(index);
		} else {
			row = null;
		}
		return row;
	}

	public RecordContainerWebElementRow getFirstRowWithValueInColumn(String value, int columnIndex) {
		nestedElement.click();

		for (RecordContainerWebElementRow row : rows) {
			String columnValue = row.getValueInColumn(columnIndex);
			if (columnValue.contains(value)) {
				return row;
			}
		}

		throw new RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn(value, columnIndex);
	}

	public boolean hasRowWithValueInColumn(String value, int columnIndex) {
		boolean hasRowWithValueInColumn;
		try {
			getFirstRowWithValueInColumn(value, columnIndex);
			hasRowWithValueInColumn = true;
		} catch (RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn e) {
			hasRowWithValueInColumn = false;
		}
		return hasRowWithValueInColumn;
	}

	public class RecordContainerWebElementRow {

		WebElement trElement;

		int index;

		private List<WebElement> columnElements = new ArrayList<WebElement>();

		public RecordContainerWebElementRow(ConstellioWebElement trElement, int index) {
			trElement.scrollIntoView();
			this.trElement = trElement.getAdaptedElement();
			this.index = index;
			loadRow();
		}

		private void loadRow() {
			List<WebElement> vTableCellContentTags = trElement.findElements(By.className("v-table-cell-content"));
			for (WebElement vTableCellContentTag : vTableCellContentTags) {
				WebElement vTableCellWrapperTag = vTableCellContentTag.findElement(By.className("v-table-cell-wrapper"));
				columnElements.add(vTableCellWrapperTag);
			}
		}

		public String getValueInColumn(int index) {
			return getComponentInColumn(index).getText();
		}

		public WebElement getComponentInColumn(int index) {
			return columnElements.get(index);
		}

		public WebElement getButton(String className) {
			return trElement.findElement(By.className(className));
		}

		public boolean hasButton(String className) {
			return getButton(className) != null;
		}

		private void clickElement(WebElement element) {
			int timeoutInSeconds = 100;
			String lastPageDateString = webDriver.getPageLoadTimeAsString(100);
			element.click();
			webDriver.waitForPageReload(timeoutInSeconds, lastPageDateString);
		}

		public void clickButton(String className) {
			clickElement(getButton(className));
		}

		public String clickButtonAndConfirmAndWaitForWarningMessage(String className) {
			clickElement(getButton(className));
			return clickConfirmDialogButtonAndWaitForErrorMessage("confirmdialog-ok-button");

		}

		public void clickButtonAndConfirm(String className) {
			clickElement(getButton(className));
			clickConfirmDialogButton("confirmdialog-ok-button");
		}

		public void clickButtonAndCancel(String className) {
			clickElement(getButton(className));
			clickConfirmDialogButton("confirmdialog-cancel-button");
		}

		private void clickConfirmDialogButton(String id) {
			WebElement confirmDialogOKButton = getConfirmDialogButton(id);
			int attempts = 0;
			while (confirmDialogOKButton == null && attempts < 5) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				confirmDialogOKButton = getConfirmDialogButton(id);
				attempts++;
			}
			if (confirmDialogOKButton != null) {
				//confirmDialogOKButton.click();
				clickElement(confirmDialogOKButton);
			}
		}

		private String clickConfirmDialogButtonAndWaitForErrorMessage(String id) {
			WebElement confirmDialogOKButton = getConfirmDialogButton(id);
			int attempts = 0;
			while (confirmDialogOKButton == null && attempts < 5) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				confirmDialogOKButton = getConfirmDialogButton(id);
				attempts++;
			}
			if (confirmDialogOKButton != null) {
				//confirmDialogOKButton.click();
				confirmDialogOKButton.click();
				final AtomicReference<String> warningMessage = new AtomicReference<>();
				new ConditionWithTimeout() {

					@Override
					protected boolean evaluate() {
						ConstellioWebElement message = webDriver.findRequiredElement(By.className("warning"));
						String result = message == null ? null : message.getText();
						if (result != null) {
							warningMessage.set(result);
							message.click();
							return true;
						}
						return false;
					}
				}.waitForTrue(2000);
				return warningMessage.get();
			}

			return null;
		}

		private WebElement getConfirmDialogButton(String id) {
			try {
				return webDriver.findElement(By.id(id));
			} catch (Exception e) {
				return null;
			}
		}

		public int getIndex() {
			return index;
		}
	}

}
