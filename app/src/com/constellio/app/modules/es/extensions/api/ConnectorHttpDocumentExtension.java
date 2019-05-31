package com.constellio.app.modules.es.extensions.api;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.model.services.thesaurus.ThesaurusService;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Locale;

public class ConnectorHttpDocumentExtension {
	public void onHttpDocumentFetched(OnHttpDocumentFetchedParams onHttpDocumentFetchedParams) {
		ConnectorHttpDocument connectorHttpDocument = onHttpDocumentFetchedParams.getConnectorHttpDocument();
		ThesaurusService thesaurusService = onHttpDocumentFetchedParams.getModelLayerFactory().getThesaurusManager()
				.get(connectorHttpDocument.getCollection());

		if (thesaurusService != null) {
			if (StringUtils.isNotBlank(connectorHttpDocument.getLanguage())) {
				List<String> thesaurusIdMatch = thesaurusService
						.matchThesaurusLabels(connectorHttpDocument.getParsedContent(), new Locale(connectorHttpDocument.getLanguage()));
				connectorHttpDocument.setThesaurusMatch(thesaurusIdMatch);
			}
		}
	}
}
