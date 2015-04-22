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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XmlView implements DataWrapper<Document>{
	private Document document;
	private final SAXBuilder builder = new SAXBuilder();
	private final XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
	

	@Override
	public void init(byte[] data) {
		try {
			document = builder.build(new ByteArrayInputStream(data));
		} catch (JDOMException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] toBytes() {
		return xmlOutput.outputString(document).getBytes();
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
