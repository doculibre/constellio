package com.constellio.sdk.tests.selenium.components.vaadin;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;
import com.constellio.sdk.tests.selenium.conditions.ConditionWithTimeout;

public class VaadinUpload {

	private ConstellioWebElement element;

	public VaadinUpload(ConstellioWebElement element) {
		super();
		this.element = element;
	}

	public void uploadFile(File file) {
		ConstellioWebElement div = element.findElement(By.tagName("div"));
		final ConstellioWebElement fileUploadField = div.findAdaptElements(By.tagName("input")).get(1);

		if (StringUtils.isNotEmpty(fileUploadField.getAttribute("value"))) {
			throw new RuntimeException("Component has already a file to upload : " + fileUploadField.getAttribute("value"));
		}

		fileUploadField.sendKeys(file.getAbsolutePath());

		if (StringUtils.isEmpty(fileUploadField.getAttribute("value"))) {
			throw new RuntimeException("Component has already a file to upload : " + fileUploadField.getAttribute("value"));
		}

		ConstellioWebElement uploadButton = div.findElement(By.tagName("div"));
		uploadButton.click();

		element.getWebDriver().waitForCondition(new ConditionWithTimeout() {

			@Override
			protected boolean evaluate() {
				return StringUtils.isEmpty(fileUploadField.getAttribute("value"));
			}
		});
	}

}
