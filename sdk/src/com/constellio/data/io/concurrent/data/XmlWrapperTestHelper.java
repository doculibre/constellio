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
