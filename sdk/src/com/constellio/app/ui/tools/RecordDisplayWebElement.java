package com.constellio.app.ui.tools;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class RecordDisplayWebElement {

	ConstellioWebElement nestedElement;

	public RecordDisplayWebElement(ConstellioWebElement nestedElement) {
		this.nestedElement = nestedElement;
	}

	public boolean isVisible(String metadataCode) {
		try {
			getCaption(metadataCode);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getCaption(String metadataCode) {
		String captionId = RecordDisplay.STYLE_CAPTION + "-" + metadataCode;
		return nestedElement.findElement(By.id(captionId)).getText();
	}

	public String getValue(String metadataCode) {
		String valueId = RecordDisplay.STYLE_VALUE + "-" + metadataCode;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return nestedElement.findElement(By.id(valueId)).getText();
	}

}
