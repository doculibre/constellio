package com.constellio.app.services.sip.xsd;

import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XSDResourceResolver implements LSResourceResolver {

	public XSDResourceResolver() {
	}

	@Override
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
		LSInput input;
		String filename = StringUtils.substringAfterLast(baseURI, "/");
		InputStream resourceAsStream = getClass().getResourceAsStream(filename);
		if (resourceAsStream == null) {
			input = null;
		} else {
			byte[] bytes = null;
			try {
				bytes = new byte[resourceAsStream.available()];
				resourceAsStream.read(bytes);
			} catch (IOException e) {

			} finally {
				try {
					resourceAsStream.close();
				} catch (IOException e) {
				}
			}
			input = new DOMInputImpl();
			input.setBaseURI(baseURI);
			input.setEncoding("UTF-8");
			input.setPublicId(publicId);
			input.setSystemId(systemId);
			input.setByteStream(new ByteArrayInputStream(bytes));
		}
		return input;
	}
}