package com.constellio.model.services.pdf.pdtron.signature;

import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import org.jdom2.Content;
import org.jdom2.Element;

import java.awt.*;
import java.util.List;

public class PdfTronSignatureAnnotation extends PdfSignatureAnnotation {

	private static final String PAGE_ELEMENT_NAME = "page";
	private static final String POSITION_ELEMENT_NAME = "rect";
	private static final String USER_ELEMENT_NAME = "userId";
	private static final String USERNAME_ELEMENT_NAME = "title";
	private static final String IMAGE_ELEMENT_NAME = "imagedata";

	public PdfTronSignatureAnnotation(Element annotation) throws PdfTronXMLException_XMLParsingException {
		super(
				Integer.parseInt(annotation.getAttributeValue(PAGE_ELEMENT_NAME)),
				createPositionRectangle(annotation.getAttributeValue(POSITION_ELEMENT_NAME)),
				annotation.getAttributeValue(USER_ELEMENT_NAME),
				annotation.getAttributeValue(USERNAME_ELEMENT_NAME),
				fetchImageData(annotation),
				false,
				false
		);
	}

	private static Rectangle createPositionRectangle(String position) {
		String[] parts = position.split(",");
		int left = (int) Math.round(Double.parseDouble(parts[0]));
		int bottom = (int) Math.round(Double.parseDouble(parts[1]));
		int right = (int) Math.round(Double.parseDouble(parts[2]));
		int top = (int) Math.round(Double.parseDouble(parts[3]));
		return new Rectangle(left, top, right - left, top - bottom);
	}

	private static String fetchImageData(Element signature) throws PdfTronXMLException_XMLParsingException {
		List<Content> contents = signature.getContent();
		for (Content content : contents) {
			Element contentAsElement = (Element) content;
			if (contentAsElement != null && contentAsElement.getName().equals(IMAGE_ELEMENT_NAME)) {
				return contentAsElement.getContent(0).getValue();
			}
		}
		throw new PdfTronXMLException_XMLParsingException(new Exception());
	}

}
