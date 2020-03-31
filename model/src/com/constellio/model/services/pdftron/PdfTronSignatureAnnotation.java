package com.constellio.model.services.pdftron;

import com.constellio.model.services.pdftron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import org.jdom2.Content;
import org.jdom2.Element;

import java.awt.*;
import java.util.List;

public class PdfTronSignatureAnnotation implements Comparable<PdfTronSignatureAnnotation> {

	private static final String PAGE_ELEMENT_NAME = "page";
	private static final String POSITION_ELEMENT_NAME = "rect";
	private static final String USER_ELEMENT_NAME = "userId";
	private static final String USERNAME_ELEMENT_NAME = "title";
	private static final String IMAGE_ELEMENT_NAME = "imagedata";

	private int page;
	private Rectangle position;
	private String userId;
	private String username;
	private String imageData;

	public PdfTronSignatureAnnotation(Element annotation) throws PdfTronXMLException_XMLParsingException {
		page = Integer.parseInt(annotation.getAttributeValue(PAGE_ELEMENT_NAME));
		position = createPositionRectangle(annotation.getAttributeValue(POSITION_ELEMENT_NAME));
		userId = annotation.getAttributeValue(USER_ELEMENT_NAME);
		username = annotation.getAttributeValue(USERNAME_ELEMENT_NAME);
		imageData = fetchImageData(annotation);
	}

	private Rectangle createPositionRectangle(String position) {
		String[] parts = position.split(",");
		int left = (int) Math.round(Double.parseDouble(parts[0]));
		int bottom = (int) Math.round(Double.parseDouble(parts[1]));
		int right = (int) Math.round(Double.parseDouble(parts[2]));
		int top = (int) Math.round(Double.parseDouble(parts[3]));
		return new Rectangle(left, top, right - left, top - bottom);
	}

	private String fetchImageData(Element signature) throws PdfTronXMLException_XMLParsingException {
		List<Content> contents = signature.getContent();
		for (Content content : contents) {
			Element contentAsElement = (Element) content;
			if (contentAsElement != null && contentAsElement.getName().equals(IMAGE_ELEMENT_NAME)) {
				return contentAsElement.getContent(0).getValue();
			}
		}
		throw new PdfTronXMLException_XMLParsingException(new Exception());
	}

	public int getPage() {
		return page;
	}

	public Rectangle getPosition() {
		return position;
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getImageData() {
		return imageData;
	}

	@Override
	public int compareTo(PdfTronSignatureAnnotation other) {
		int result;

		result = compare(page, other.getPage());
		if (result != 0) {
			return result;
		}

		result = compare(position.y, other.getPosition().y);
		if (result != 0) {
			return result;
		}

		result = compare(position.x, other.getPosition().x);
		return result;
	}

	private int compare(int thisValue, int otherValue) {
		if (thisValue != otherValue) {
			return thisValue < otherValue ? -1 : 1;
		}
		return 0;
	}
}
