/**
 * Constellio
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This program is free software: you can redistribute it and/or modifyTo
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.sdk.tests.selenium.adapters.constellio;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.constellio.sdk.tests.selenium.adapters.base.WebElementAdapter;
import com.constellio.sdk.tests.selenium.adapters.base.WebElementFinder;

public class ConstellioWebTableElement extends ConstellioWebElement {

	private static final int XPATH_NATURAL_OFFSET = 1; // Pour xPath, tout premier élémet est d'indice 1, or on entend la

	// première
	// ligne comme la numéro 0.

	public ConstellioWebTableElement(ConstellioWebDriver webDriver, By by) {
		super(webDriver, by);
		this.validate();
	}

	public ConstellioWebTableElement(ConstellioWebDriver webDriver, WebElementFinder<WebElement> webElementFinder) {
		super(webDriver, webElementFinder);
		this.validate();
	}

	public ConstellioWebTableElement(WebElementAdapter<?, ConstellioWebDriver> webElement, By by) {
		super(webElement, by);
		this.validate();
	}

	public ConstellioWebTableElement(WebElementAdapter<ConstellioWebElement, ConstellioWebDriver> webElement) {
		super(webElement);
		this.validate();
	}

	private static By getRowAtByAccess(int index, int offset) {
		String xPath = "tbody/tr[" + Integer.toString(offset + index + XPATH_NATURAL_OFFSET) + "]";
		return By.xpath(xPath);
	}

	public List<ConstellioWebElement> getHeadersCells() {
		By headersByAccess = By.xpath("tbody/tr/th");
		return this.findAdaptElements(headersByAccess);
	}

	public ConstellioWebElement getRowAt(int index) {
		int offset = 0;
		return this.getRowAt(index, offset);
	}

	public ConstellioWebElement getRowAt(int index, int offset) {
		ConstellioWebElement line = this.getRowAtOrNull(index, offset);
		if (null == line) {
			String message = "Impossible de récupérer la ligne d'indice " + Integer.toString(index) + ".";
			throw new java.lang.RuntimeException(message);
		}
		return line;
	}

	public ConstellioWebElement getRowAtOrNull(int index) {
		int offset = 0;
		return this.getRowAtOrNull(index, offset);
	}

	public ConstellioWebElement getRowAtOrNull(int index, int offset) {
		try {
			return this.findElement(getRowAtByAccess(index, offset));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public List<ConstellioWebElement> getRows() {
		int offset = 0;
		return this.getRows(offset);
	}

	public List<ConstellioWebElement> getRows(int offset) {
		List<ConstellioWebElement> resultList = new ArrayList<ConstellioWebElement>();
		ConstellioWebElement currentLine = null;
		boolean searchAgain = true;
		for (int i = offset; searchAgain; ++i) {
			currentLine = this.getRowAtOrNull(i);
			if (null != currentLine) {
				resultList.add(currentLine);
			} else {
				searchAgain = false;
			}
		}
		return resultList;
	}

	private void validate() {
		String expectedTagName = "table";
		String actualTagName = this.getTagName();
		if (!actualTagName.equals(expectedTagName)) {
			throw new java.lang.RuntimeException(
					"L'objet ConstellioWebTableElement ne doit être construit que sur une balise <table>. Il s'agit ici d'une balise <"
							+ actualTagName + ">.");
		}
	}
}
