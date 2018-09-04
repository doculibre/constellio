package com.constellio.app.services.corrector;

import org.jdom2.Document;
import org.jdom2.Element;

import java.util.Iterator;

public class CorrectorExcluderWriter {
	public static final String EXCLUSION = "exclusion";
	public static final String COLLECTION = "collection";
	public static final String EXCEPTION_ROOT = "exceptions";
	private Document document;

	public CorrectorExcluderWriter(Document document) {
		this.document = document;
	}

	public void createEmptyException() {
		Element exclusion_root = new Element(EXCEPTION_ROOT);
		document.setRootElement(exclusion_root);
	}

	public void addExclusion(final CorrectorExclusion addExceptions) {
		Element exclusionRoot = document.getRootElement();
		Element exclusionElement = new Element(EXCLUSION);
		exclusionElement.setAttribute(EXCLUSION, addExceptions.getExclusion());
		exclusionElement.setAttribute(COLLECTION, addExceptions.getCollection());

		exclusionRoot.addContent(exclusionElement);
	}

	public void updateExclusion(final CorrectorExclusion exclusion, CorrectorExclusion oldExclusion) {
		Element rootElement = document.getRootElement();

		Iterator<Element> iterator = rootElement.getChildren().listIterator();
		while (iterator.hasNext()) {
			Element nextValue = iterator.next();
			if (nextValue.getAttributeValue(COLLECTION).equals(oldExclusion.getCollection())
				&& nextValue.getAttributeValue(EXCLUSION).equals(oldExclusion.getExclusion())) {
				iterator.remove();
				Element element = new Element(EXCLUSION);
				element.setAttribute(EXCLUSION, exclusion.getExclusion());
				element.setAttribute(COLLECTION, exclusion.getCollection());
				rootElement.addContent(element);
				break;
			}
		}
	}

	public void deleteExclusion(final CorrectorExclusion exclusion) {
		Element rootElement = document.getRootElement();
		Iterator<Element> iterator = rootElement.getChildren().listIterator();

		while (iterator.hasNext()) {
			Element child = iterator.next();
			if (child.getAttribute(EXCLUSION).getValue().equals(exclusion.getExclusion())
				&& child.getAttribute(COLLECTION).getValue().equals(exclusion.getCollection())) {
				iterator.remove();
				break;
			}
		}
	}
}
