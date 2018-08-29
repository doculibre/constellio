package com.constellio.model.services.search;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SynonymsWriter extends SynonymsXml {
	public SynonymsWriter(Document document) {
		super(document);
	}

	public void update(List<String> synonyms) {
		Element rootElement = initRootElement();

		for (String synonym : synonyms) {
			if (StringUtils.isNotBlank(synonym)) {
				Element doc = new Element(DOC);
				doc.setText(synonym);
				rootElement.addContent(doc);
			}
		}
	}

	@NotNull
	public Element initRootElement() {
		Element rootElement;
		if (!document.hasRootElement()) {
			rootElement = new Element(ROOT);
			document.setRootElement(rootElement);
		} else {
			rootElement = document.getRootElement();
			rootElement.removeContent();
		}
		return rootElement;
	}
}
