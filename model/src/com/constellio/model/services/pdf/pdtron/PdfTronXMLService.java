package com.constellio.model.services.pdf.pdtron;

import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_CannotEditOtherUsersAnnoations;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_IOExeption;
import com.constellio.model.services.pdf.pdtron.PdfTronXMLException.PdfTronXMLException_XMLParsingException;
import com.constellio.model.services.pdf.pdtron.signature.PdfTronSignatureAnnotation;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PdfTronXMLService {
	public static final String ANNOTATION_ELEMENT_NAME = "annots";
	public static final String USER_NAME = "title";
	public static final String SUBJECT = "subject";
	public static final String SIGNATURE_TYPE = "ConstellioSignature";
	public static final String ELEMENT_NAME = "name";
	public static final String USER_ID = "userId";

	public PdfTronXMLService() {

	}

	public List<PdfTronSignatureAnnotation> getSignatureAnnotations(String currentAnnotationAsStr)
			throws PdfTronXMLException_XMLParsingException, PdfTronXMLException_IOExeption {
		Document currentAnnotationDocument = getDocumentFromStr(currentAnnotationAsStr);
		Element currentAnnotsElement = getAnnotationElementList(currentAnnotationDocument);

		Map<String, Element> currentAnnotationsById = currentAnnotsElement != null
													  ? getElementInMapById(currentAnnotsElement.getChildren()) : new HashMap<>();

		List<PdfTronSignatureAnnotation> signatures = new ArrayList<>();
		for (Element element : currentAnnotationsById.values()) {
			if (element.getAttributeValue(SUBJECT).equals(SIGNATURE_TYPE)) {
				signatures.add(new PdfTronSignatureAnnotation(element));
			}
		}

		return signatures;
	}

	public Map<String, Element> getElementInMapById(List<Element> elementList) {
		Map<String, Element> elementInMapById = new HashMap<>();

		for (Element element : elementList) {
			String elementName = element.getAttributeValue(ELEMENT_NAME);
			elementInMapById.put(elementName, element);
		}

		return elementInMapById;
	}

	public static Document getDocumentFromStr(String str)
			throws PdfTronXMLException_IOExeption, PdfTronXMLException_XMLParsingException {
		if (str == null) {
			return null;
		}

		StringReader stringReader = new StringReader(str);
		Document document = null;
		try {
			document = new SAXBuilder().build(stringReader);
		} catch (JDOMException e) {
			throw new PdfTronXMLException_XMLParsingException(e);
		} catch (IOException e) {
			throw new PdfTronXMLException.PdfTronXMLException_IOExeption(e);
		}
		stringReader.close();

		return document;
	}

	public String mergeTwoAnnotationFile(String xmlMain, String xmlFileToMerge)
			throws PdfTronXMLException_XMLParsingException, PdfTronXMLException_IOExeption {
		Document mainDocument = getDocumentFromStr(xmlMain);
		Document documentToMerge = getDocumentFromStr(xmlFileToMerge);

		Element elementParentOfElementToMerge = getAnnotationElementList(documentToMerge);
		Element elementParentOfXmlMain = getAnnotationElementList(mainDocument);

		Iterator<Element> iterableElements = elementParentOfElementToMerge.getChildren().iterator();

		while (iterableElements.hasNext()) {
			Element currentElement = iterableElements.next();
			iterableElements.remove();
			currentElement.setAttribute(ELEMENT_NAME, UUID.randomUUID().toString());

			elementParentOfXmlMain.addContent(currentElement);
		}

		XMLOutputter xmlOut = new XMLOutputter();

		return xmlOut.outputString(mainDocument);
	}

	public String processNewXML(String currentAnnotationAsStr, String newAnnotationAsStr,
								boolean canUserChangeOtherUserAnnotation, String userId)
			throws PdfTronXMLException_CannotEditOtherUsersAnnoations, PdfTronXMLException_XMLParsingException, PdfTronXMLException_IOExeption {

		Document newAnnotationDocument = getDocumentFromStr(newAnnotationAsStr);
		Document currentAnnotationDocument = getDocumentFromStr(currentAnnotationAsStr);

		Element newAnnotsElement = getAnnotationElementList(newAnnotationDocument);
		Element currentAnnotsElement = getAnnotationElementList(currentAnnotationDocument);


		Map<String, Element> currentAnnotationsById = currentAnnotsElement != null
													  ? getElementInMapById(currentAnnotsElement.getChildren()) : new HashMap<>();

		if (newAnnotsElement != null) {
			for (Element currentChildren : newAnnotsElement.getChildren()) {
				String elementName = currentChildren.getAttributeValue(ELEMENT_NAME);

				Element existingElement = currentAnnotationsById.get(elementName);

				if (existingElement == null) {
					currentChildren.setAttribute(USER_ID, userId);
				} else {
					String elementUserId = existingElement.getAttributeValue(USER_ID);
					currentChildren.setAttribute(USER_ID, elementUserId);

					if (!canUserChangeOtherUserAnnotation && !areElementEqual(existingElement, currentChildren)) {
						if (!elementUserId.equals(userId)) {
							throw new PdfTronXMLException_CannotEditOtherUsersAnnoations();
						}
					}
				}
			}
		}

		XMLOutputter xmlOut = new XMLOutputter();

		return xmlOut.outputString(newAnnotationDocument);
	}

	public static boolean areEqual(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}

		if (str1 == null && str2 != null) {
			return false;
		}

		return str1.equals(str2);
	}

	public static boolean areElementEqual(Element element1, Element element2) {
		if (element1 == null && element2 == null) {
			return true;
		}

		if (element1 == null && element2 != null
			|| element1 != null && element2 == null) {
			return false;
		}


		if (element1.getAttributes().size() != element2.getAttributes().size()) {
			return false;
		}

		if (!element1.getName().equals(element2.getName())) {
			return false;
		}

		for (Attribute currentAttributeElement1 : element1.getAttributes()) {
			String el1AttributeValue = currentAttributeElement1.getValue();
			String el2AttributeValue = element2.getAttributeValue(currentAttributeElement1.getName(), currentAttributeElement1.getNamespace());

			if (!areEqual(el1AttributeValue, el2AttributeValue)) {
				return false;
			}
		}

		if (!areEqual(element1.getText(), element2.getText())) {
			return false;
		}

		if (element1.getChildren().size() != element2.getChildren().size()) {
			return false;
		}

		List<Integer> indexDone = new ArrayList<>();

		List<Element> childrenOfElement2 = element2.getChildren();

		int size = element2.getChildren().size();

		for (Element currentChildOfElement1 : element1.getChildren()) {

			for (int i = 0; i < size; i++) {
				if (indexDone.contains(i)) {
					continue;
				}
				Element currentChildrenOfElement2 = childrenOfElement2.get(i);

				if (areElementEqual(currentChildOfElement1, currentChildrenOfElement2)) {
					indexDone.add(i);
					break;
				}
			}
		}

		return indexDone.size() == size;
	}

	public static Element getAnnotationElementList(Document xmlDocument) {
		if (xmlDocument == null) {
			return null;
		}

		Element rootElement = xmlDocument.getRootElement();

		return rootElement.getChild(ANNOTATION_ELEMENT_NAME, rootElement.getNamespace());
	}
}
