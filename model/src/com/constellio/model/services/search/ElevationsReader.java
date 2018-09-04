package com.constellio.model.services.search;

import com.constellio.model.services.search.QueryElevation.DocElevation;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.List;

public class ElevationsReader extends ElevationsXml {

	public ElevationsReader(Document document) {
		super(document);
	}

	public Elevations load() {
		Elevations elevations = new Elevations();

		Element root = document.getRootElement();

		// Elevation
		Element elevation = root.getChild(ELEVATION);
		if (elevation != null) {
			List<Element> queries = elevation.getChildren(QUERY);
			if (queries != null) {
				for (Element query : queries) {
					QueryElevation queryElevation = new QueryElevation(StringUtils.defaultIfEmpty(query.getAttributeValue(QUERY_TEXT_ATTR), null));

					List<Element> docs = query.getChildren(DOC);
					if (docs != null) {
						for (Element doc : docs) {
							String id = StringUtils.defaultIfEmpty(doc.getAttributeValue(DOC_ID_ATTR), null);
							DocElevation docElevation = new DocElevation(id, queryElevation.getQuery());

							queryElevation.addDocElevation(docElevation);
						}
					}

					elevations.addOrUpdate(queryElevation);
				}
			}
		}

		// Exclusion
		Element exclusion = root.getChild(EXCLUSION);
		if (exclusion != null) {
			List<Element> docs = exclusion.getChildren(DOC);
			if (docs != null) {
				for (Element doc : docs) {
					String id = StringUtils.defaultIfEmpty(doc.getAttributeValue(DOC_ID_ATTR), null);
					if (id != null) {
						elevations.addDocExclusion(id);
					}
				}
			}
		}

		return elevations;
	}
}
