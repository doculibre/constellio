package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.services.search.entities.SearchBoost;

public class SearchBoostReader {

	private static final String TYPE = "type";
	private static final String KEY = "key";
	private static final String LABEL = "label";
	private Document document;

	public SearchBoostReader(Document document) {
		this.document = document;
	}

	public List<SearchBoost> getAll() {

		List<SearchBoost> searchBoostList = new ArrayList<>();

		Element root = document.getRootElement();

		Iterator<Element> iteratorType = root.getChildren(TYPE).listIterator();
		while (iteratorType.hasNext()) {
			Element childType = iteratorType.next();
			for (Element child : childType.getChildren()) {
				SearchBoost searchBoost = new SearchBoost();
				searchBoost.setType(childType.getAttributeValue(TYPE));
				searchBoost.setLabel(child.getAttributeValue(LABEL));
				searchBoost.setKey(child.getAttributeValue(KEY));
				searchBoost.setValue(Double.valueOf(child.getText()));
				searchBoostList.add(searchBoost);
			}
		}
		return searchBoostList;
	}
}
