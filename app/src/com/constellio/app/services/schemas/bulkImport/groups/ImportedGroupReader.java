package com.constellio.app.services.schemas.bulkImport.groups;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class ImportedGroupReader {

	private static final String CODE = "code";
	private static final String PARENT = "parent";
	private static final String TITLE = "title";
	Document document;

	public ImportedGroupReader(Document document) {
		this.document = document;
	}

	public List<ImportedGroup> readAll() {
		List<ImportedGroup> returnList = new ArrayList<>();
		Element groupElements = document.getRootElement();
		for (Element groupElement : groupElements.getChildren()) {
			returnList.add(readGroup(groupElement));
		}
		return returnList;
	}

	private ImportedGroup readGroup(Element groupElement) {
		return new ImportedGroup().setCode(readCode(groupElement)).setParent(readParent(groupElement))
				.setTitle(readTitle(groupElement));
	}

	private String readTitle(Element groupElement) {
		Element titleElement = groupElement.getChild(TITLE);
		if (titleElement != null) {
			return titleElement.getText().trim();
		}
		return null;
	}

	private String readParent(Element groupElement) {
		Element parentElement = groupElement.getChild(PARENT);
		if (parentElement != null) {
			return parentElement.getText().trim();
		}
		return null;
	}

	private String readCode(Element groupElement) {
		Element codeElement = groupElement.getChild(CODE);
		if (codeElement != null) {
			return codeElement.getText().trim();
		}
		return null;
	}
}
