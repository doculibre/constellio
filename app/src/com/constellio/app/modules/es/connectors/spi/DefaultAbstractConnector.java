package com.constellio.app.modules.es.connectors.spi;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultAbstractConnector extends Connector {

	@Override
	public List<String> fetchTokens(String username) {
		return new ArrayList<>();
	}

	@Override
	public List<String> getReportMetadatas(String reportMode) {
		return new ArrayList<>();
	}

	@Override
	public String getMainConnectorDocumentType() {
		List<String> documentTypes = getConnectorDocumentTypes();
		if (documentTypes != null && documentTypes.size() == 1) {
			return documentTypes.get(0);
		} else {
			throw new RuntimeException("Re-implement method!");
		}
	}
}
