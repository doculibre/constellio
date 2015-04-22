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
package com.constellio.data.dao.services.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.Element;
import org.junit.Test;

import com.constellio.data.dao.services.XMLElementUtils;
import com.constellio.sdk.tests.ConstellioTest;

public class XMLElementUtilsTest extends ConstellioTest {

	@Test
	public void givenContentAndNameThenNewElementWithContentReturnCorrectElement() {
		Element returnElement = XMLElementUtils.newElementWithContent("zeName", "zeContent");
		assertThat(returnElement.getContent().get(0).getValue()).isEqualTo("zeContent");
		assertThat(returnElement.getName()).isEqualTo("zeName");
	}

	@Test
	public void givenBooleanAndNameThenNewElementWithContentReturnCorrectElement() {
		Element returnElement = XMLElementUtils.newElementWithContent("zeName", false);
		assertThat(returnElement.getContent().get(0).getValue()).isEqualTo(Boolean.toString(false));
		assertThat(returnElement.getName()).isEqualTo("zeName");
	}
}
