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
package com.constellio.app.ui.pages.management.schemas.type;

import org.openqa.selenium.By;

import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.tools.AutocompleteWebElement;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.app.ui.tools.CheckboxWebElement;
import com.constellio.app.ui.tools.OptionGroupWebElement;
import com.constellio.app.ui.tools.PageHelper;
import com.constellio.app.ui.tools.components.basic.TextFieldWebElement;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

public class AddEditSchemaMetadataPage extends PageHelper {

	protected AddEditSchemaMetadataPage(ConstellioWebDriver driver) {
		super(driver);
	}

	public ButtonWebElement getAddButton() {
		return getButtonByClassName(AddButton.BUTTON_STYLE, 0);
	}

	public ButtonWebElement getEditButtonOnIndex(int index) {
		return getButtonByClassName(EditButton.BUTTON_STYLE, index);
	}

	public TextFieldWebElement getCodeElement() {
		return getTextFieldWebElementById("localcode");
	}

	public TextFieldWebElement getTitleElement() {
		return getTextFieldWebElementById("title");
	}

	public AutocompleteWebElement getValueTypeElement() {
		ConstellioWebElement element = driver.findElement(By.id("valueType"));
		return new AutocompleteWebElement(element);
	}

	public CheckboxWebElement getMultivalueElement() {
		ConstellioWebElement element = driver.findElement(By.id("multivalue"));
		return new CheckboxWebElement(element);
	}

	public AutocompleteWebElement getEntryElement() {
		ConstellioWebElement element = driver.findElement(By.id("entry"));
		return new AutocompleteWebElement(element);
	}

	public OptionGroupWebElement getMetadataGroupElement() {
		ConstellioWebElement element = driver.findElement(By.id("metadataGroup"));
		return new OptionGroupWebElement(element);
	}

	public AutocompleteWebElement getReferenceElement() {
		ConstellioWebElement element = driver.findElement(By.id("reference"));
		return new AutocompleteWebElement(element);
	}

	public CheckboxWebElement getRequiredElement() {
		ConstellioWebElement element = driver.findElement(By.id("required"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getEnableElement() {
		ConstellioWebElement element = driver.findElement(By.id("enabled"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getSearchableElement() {
		ConstellioWebElement element = driver.findElement(By.id("searchable"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getSortableElement() {
		ConstellioWebElement element = driver.findElement(By.id("sortable"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getAdvancedSearchElement() {
		ConstellioWebElement element = driver.findElement(By.id("advancedSearch"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getFacetElement() {
		ConstellioWebElement element = driver.findElement(By.id("facet"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getHighlightElement() {
		ConstellioWebElement element = driver.findElement(By.id("highlight"));
		return new CheckboxWebElement(element);
	}

	public CheckboxWebElement getAutocompleteElement() {
		ConstellioWebElement element = driver.findElement(By.id("autocomplete"));
		return new CheckboxWebElement(element);
	}

	public ConstellioWebElement getDefaultValueElement() {
		ConstellioWebElement element = driver.findElement(By.id("defaultValue"));
		return new ConstellioWebElement(element);
	}
}
