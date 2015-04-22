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
