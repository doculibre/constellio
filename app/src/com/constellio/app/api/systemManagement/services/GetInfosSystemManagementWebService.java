package com.constellio.app.api.systemManagement.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import javax.servlet.http.HttpServletRequest;

import org.jdom2.Element;

public class GetInfosSystemManagementWebService extends AdminSystemManagementWebService {
	@Override
	protected void doService(HttpServletRequest req, Element responseDocumentRootElement) {
		responseDocumentRootElement.addContent(new Element("version").setText(getVersion()));
		responseDocumentRootElement.addContent(new Element("records").setText(getRecordsCount()));
	}

	private String getRecordsCount() {
		int count = 0;
		for (String collection : modelLayerFactory().getCollectionsListManager().getCollections()) {
			count += modelLayerFactory().newSearchServices().getResultsCount(fromAllSchemasIn(collection).returnAll());
		}
		return "" + count;
	}

	private String getVersion() {
		return appLayerFactory().newApplicationService().getWarVersion();
	}
}
