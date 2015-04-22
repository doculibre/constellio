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
package com.constellio.data.io.concurrent.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;
import org.jdom2.Document;

public class XmlWrapperTestHelper implements WrapperTestHelper<Document>{

	private XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
	@Test
	public void givenAByteArrayConstructedFromDataWhenConstructingAnotherDataFromTheByteArrayThenTwoDatasAreEqual(){
		XmlView xmlData = new XmlView();
		String xml = "<test> it is a test </test>";

		xmlData.init(xml.getBytes());
		XmlView newXmlData = new XmlView();
		
		newXmlData.init(xml.getBytes());
		
	}

	@Override
	public byte[] getAValue() {
		String xml = "<test> it is a test </test>";
		return xml.getBytes();
	}

	@Override
	public void assertEquality(DataWrapper<Document> d1, DataWrapper<Document> d2) {
		assertThat(xmlOutput.outputString(d1.getData())).isEqualTo(xmlOutput.outputString(d2.getData()));
	}

	@Override
	public void doModification(Object data) {
		Document document = (Document) data;
		document.setBaseURI("http://test.com");
		document.detachRootElement();
		
	}

	@Override
	public DataWrapper<Document> createEmptyData() {
		return new XmlView();
	}
}
