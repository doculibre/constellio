package com.constellio.model.services.search;

import java.util.Iterator;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.services.search.entities.SearchBoost;

public class SearchBoostWriter {

	private static final String FIELD = "field";
	private static final String KEY = "key";
	private static final String LABEL = "label";
	private static final String TYPE = "type";
	public static final String METADATA_TYPE = "metadata";
	public static final String QUERY_TYPE = "query";
	private Document document;

	public SearchBoostWriter(Document document) {
		this.document = document;
	}

	public void createEmptySearchBoost() {
		Element root = new Element("root");
		Element fieldTypeElement = new Element(TYPE);
		Element queryTypeElement = new Element(TYPE);
		fieldTypeElement.setAttribute(TYPE, METADATA_TYPE);
		queryTypeElement.setAttribute(TYPE, QUERY_TYPE);
		root.addContent(fieldTypeElement);
		root.addContent(queryTypeElement);
		document.setRootElement(root);
	}

	public void add(final SearchBoost searchBoost) {

		Element rootElement = document.getRootElement();
		Element typeElement = getTypeElement(searchBoost.getType(), rootElement);//String type, Element rootElement
		removeIfExists(searchBoost.getType(), searchBoost.getKey());
		Element fieldElement = new Element(FIELD);
		fieldElement.setAttribute(TYPE, searchBoost.getType());
		fieldElement.setAttribute(KEY, searchBoost.getKey());
		fieldElement.setAttribute(LABEL, searchBoost.getLabel());
		fieldElement.setText(String.valueOf(searchBoost.getValue()));
		typeElement.addContent(fieldElement);
	}

	private Element getTypeElement(String type, Element rootElement) {
		Iterator<Element> iterator = rootElement.getChildren(TYPE).listIterator();
		while (iterator.hasNext()) {
			Element child = iterator.next();
			if (child.getAttributeValue(TYPE).equals(type)) {
				return child;
			}
		}
		return null;
	}

	private void removeIfExists(String type, String metadataStoreCode) {
		Element typeElement = getTypeElement(type, document.getRootElement());
		removeElement(metadataStoreCode, typeElement);
	}

	private void removeElement(String metadataStoreCode, Element element) {
		Iterator<Element> iterator = element.getChildren().listIterator();
		while (iterator.hasNext()) {
			Element child = iterator.next();
			if (child.getAttributeValue(KEY).equals(metadataStoreCode)) {
				iterator.remove();
				break;
			}
		}
		return;
	}

	public void delete(String type, String key) {
		removeIfExists(type, key);
	}
}
