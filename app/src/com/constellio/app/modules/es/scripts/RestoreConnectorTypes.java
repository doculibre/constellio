package com.constellio.app.modules.es.scripts;

import com.constellio.app.extensions.api.scripts.ScriptParameter;
import com.constellio.app.extensions.api.scripts.ScriptParameterType;
import com.constellio.app.extensions.api.scripts.ScriptWithLogOutput;
import com.constellio.app.modules.es.connectors.http.ConnectorHttp;
import com.constellio.app.modules.es.connectors.ldap.ConnectorLDAP;
import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.migrations.EnterpriseSearchMigrationHelper;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

public class RestoreConnectorTypes extends ScriptWithLogOutput {

	private static ScriptParameter COLLECTION_PARAMETER = new ScriptParameter(ScriptParameterType.COMBOBOX, "Collection", true);

	public RestoreConnectorTypes(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Connectors", "Restaurer les types de connecteur");
	}

	@Override
	public List<ScriptParameter> getParameters() {
		List<ScriptParameter> scriptParameters = new ArrayList<>();
		scriptParameters.add(COLLECTION_PARAMETER.setOptions(getCollectionCodesExcludingSystem()));
		return scriptParameters;
	}

	@Override
	protected void execute()
			throws Exception {
		String collection = parameterValues.get(COLLECTION_PARAMETER);
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);

		Transaction transaction = new Transaction();
		if(es.getConnectorTypeWithCode(ConnectorType.CODE_SMB) == null) {
			transaction.add(
					newConnectorType(es, es.connectorInstance_smb.schema(), ConnectorSmb.class, ConnectorType.CODE_SMB, "Connecteur SMB")
							);
		}
		if(es.getConnectorTypeWithCode(ConnectorType.CODE_HTTP) == null) {
			transaction.add(
					newConnectorType(es, es.connectorInstance_http.schema(), ConnectorHttp.class, ConnectorType.CODE_HTTP, "Connecteur HTTP")
							.setDefaultAvailableConnectorFields(asList(
									field(ConnectorHttpDocument.SCHEMA_TYPE, "charset", STRING),
									field(ConnectorHttpDocument.SCHEMA_TYPE, "language", STRING),
									field(ConnectorHttpDocument.SCHEMA_TYPE, "lastModification", DATE_TIME))));
		}
		if(es.getConnectorTypeWithCode(ConnectorType.CODE_LDAP) == null) {
			transaction.add(
					newConnectorType(es, es.connectorInstance_ldap.schema(), ConnectorLDAP.class, ConnectorType.CODE_LDAP, "Connecteur LDAP")
							.setDefaultAvailableConnectorFields(asList(
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "userAccountControl", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "sAMAccountType", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "primaryGroupID", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "objectSid", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "objectGUID", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "uSNChanged", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "uSNCreated", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "userPrincipalName", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "primaryGroupID", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "name", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "displayName", MetadataValueType.STRING),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "whenChanged", MetadataValueType.DATE),
									field(ConnectorLDAPUserDocument.SCHEMA_TYPE, "whenCreated", MetadataValueType.DATE)
							)));
		}
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
	}

	public ConnectorType newConnectorType(ESSchemasRecordsServices es, MetadataSchema schema, Class<?> connectorClass, String connectorTypeCode, String title) {
		return es.newConnectorType().setCode(connectorTypeCode).setTitle(title).setLinkedSchema(schema.getCode())
				.setConnectorClassName(connectorClass.getName());
	}

	private ConnectorField field(String schemaType, String code, MetadataValueType type) {
		return new ConnectorField(schemaType + ":" + code, code, type);
	}
}
