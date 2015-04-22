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
