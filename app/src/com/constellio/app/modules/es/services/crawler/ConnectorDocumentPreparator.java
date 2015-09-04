/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.services.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.schemas.MetadataSchema;

public class ConnectorDocumentPreparator {

	private Map<String, List<String>> mappingMetadataProperties;

	MetadataSchema documentSchema;

	public ConnectorDocumentPreparator(Map<String, List<String>> mappingMetadataProperties,
			MetadataSchema documentSchema) {
		this.mappingMetadataProperties = mappingMetadataProperties;
		this.documentSchema = documentSchema;
	}

	public void applyProperties(ConnectorDocument connectorDocument) {
		Map documentProperties = connectorDocument.getProperties();
		List<String> defaultMetadata = connectorDocument.getDefaultMetadata();
		for (Entry<String, List<String>> mapEntry : mappingMetadataProperties.entrySet()) {
			String metadataCode = mapEntry.getKey();
			List<String> fieldCodes = mapEntry.getValue();

			if (!defaultMetadata.contains(metadataCode)) {
				applyMetadata(connectorDocument, documentProperties, defaultMetadata, metadataCode, fieldCodes);
			}

		}
	}

	public void applyMetadata(ConnectorDocument connectorDocument, Map documentProperties, List<String> defaultMetadata,
			String metadataCode, List<String> fieldCodes) {
		List<Object> values = new ArrayList<>();
		for (String fieldcode : fieldCodes) {
			Object value = null;
			if (defaultMetadata.contains(fieldcode)) {
				value = connectorDocument.get(fieldcode);
			} else {
				if (documentProperties.containsKey(fieldcode)) {
					value = documentProperties.get(fieldcode);
				}
			}
			if (value != null) {
				if (value instanceof List) {
					values.addAll((List) value);
				} else {
					values.add(value);
				}
			}
			connectorDocument.set(metadataCode, values);
		}
	}
}
