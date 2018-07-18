package com.constellio.app.modules.es.extensions;


import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.model.extensions.behaviors.SchemaExtension;

import java.util.List;

import static java.util.Arrays.asList;

public class ESSchemaExtension extends SchemaExtension {

	@Override
	public List<String> getAllowedSystemReservedMetadatasForExcelReport(String schemaTypeCode) {
		switch (schemaTypeCode) {
			case ConnectorSmbDocument.SCHEMA_TYPE:
				return asList(ConnectorSmbDocument.CONNECTOR_URL);
		}
		return super.getAllowedSystemReservedMetadatasForExcelReport(schemaTypeCode);
	}
}
