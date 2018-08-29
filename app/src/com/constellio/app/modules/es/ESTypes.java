package com.constellio.app.modules.es;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ESTypes {

	public static List<String> schemaTypesCodes;

	public static List<String> customSchemas;

	static {
		List<String> codesList = new ArrayList<>();
		codesList.add(ConnectorInstance.SCHEMA_TYPE);
		codesList.add(ConnectorType.SCHEMA_TYPE);
		codesList.add(ConnectorHttpDocument.SCHEMA_TYPE);
		codesList.add(ConnectorSmbFolder.SCHEMA_TYPE);
		codesList.add(ConnectorSmbDocument.SCHEMA_TYPE);
		codesList.add(ConnectorLDAPUserDocument.SCHEMA_TYPE);

		schemaTypesCodes = Collections.unmodifiableList(codesList);

		List<String> rmCustomSchemasList = new ArrayList<>();

		customSchemas = Collections.unmodifiableList(rmCustomSchemasList);
	}

	public static List<MetadataSchemaType> esSchemaTypes(AppLayerFactory appLayerFactory, String collection) {
		return esSchemaTypes(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchemaType> esSchemaTypes(MetadataSchemaTypes types) {
		List<MetadataSchemaType> schemaTypes = new ArrayList<>();

		for (String code : schemaTypesCodes) {
			if (types.hasType(code)) {
				schemaTypes.add(types.getSchemaType(code));
			}
		}

		return Collections.unmodifiableList(schemaTypes);
	}

	public static List<MetadataSchema> esSchemas(AppLayerFactory appLayerFactory, String collection) {
		return esSchemas(appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection));
	}

	public static List<MetadataSchema> esSchemas(MetadataSchemaTypes types) {
		List<MetadataSchemaType> esTypes = esSchemaTypes(types);
		List<MetadataSchema> esSchemas = new ArrayList<>();

		for (MetadataSchemaType schemaType : esTypes) {
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				esSchemas.add(schema);
			}
		}

		for (String customSchema : customSchemas) {
			if (types.hasSchema(customSchema)) {
				esSchemas.add(types.getSchema(customSchema));
			}
		}
		return esSchemas;
	}

}
