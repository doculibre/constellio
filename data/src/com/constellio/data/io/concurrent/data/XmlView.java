package com.constellio.data.io.concurrent.data;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlView extends ReaderWriterDataWrapper<Document>{
	private Document document;
	private final SAXBuilder builder = new SAXBuilder();
	private final XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
	

	@Override
	public void init(Reader reader) {
		try {
			document = builder.build(reader);
		} catch (JDOMException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void toBytes(Writer writer) {
		try {
			xmlOutput.output(document, writer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Document getData() {
		return document.clone();
	}
	
	@Override
	public XmlView setData(Document data) {
		this.document = data;
		return this;
	}
}
