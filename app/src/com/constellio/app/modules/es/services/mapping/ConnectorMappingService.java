package com.constellio.app.modules.es.services.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.extensions.api.ESModuleExtensions;
import com.constellio.app.modules.es.extensions.api.params.TargetMetadataCreationParams;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringListStringStructure;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ConnectorMappingService {
	private static final String MAPPING_METADATA_PREFIX = "MAP";
	private static final String USER_METADATA_PREFIX = MAPPING_METADATA_PREFIX;

	MetadataSchemasManager metadataSchemasManager;
	SchemasDisplayManager schemasDisplayManager;
	RecordServices recordServices;
	ESSchemasRecordsServices es;
	ESModuleExtensions extensions;
	CollectionsManager collectionsManager;

	public ConnectorMappingService(ESSchemasRecordsServices es) {
		this.es = es;
		metadataSchemasManager = es.getModelLayerFactory().getMetadataSchemasManager();
		schemasDisplayManager = es.getAppLayerFactory().getMetadataSchemasDisplayManager();
		recordServices = es.getModelLayerFactory().newRecordServices();
		extensions = es.getAppLayerFactory().getExtensions().forCollection(es.getCollection()).forModule(ConstellioESModule.ID);
		collectionsManager = es.getAppLayerFactory().getCollectionsManager();
	}

	public List<String> getDocumentTypes(ConnectorInstance<?> instance) {
		return getConnectorFor(instance).getConnectorDocumentTypes();
	}

	public List<ConnectorField> getConnectorFields(ConnectorInstance<?> instance, String documentType) {
		ConnectorType connectorType = es.getConnectorType(instance.getConnectorType());
		List<ConnectorField> fields = new ArrayList<>();
		Set<String> connectorFieldsIds = new HashSet<>();
		for (ConnectorField field : connectorType.getDefaultAvailableConnectorFields()) {
			if (field.getId().startsWith(documentType + ":") && !connectorFieldsIds.contains(field.getId())) {
				connectorFieldsIds.add(field.getId());
				fields.add(field);
			}
		}

		for (ConnectorField field : instance.getAvailableFields()) {
			if (field.getId().startsWith(documentType + ":") && !connectorFieldsIds.contains(field.getId())) {
				connectorFieldsIds.add(field.getId());
				fields.add(field);
			}
		}

		Collections.sort(fields);

		return fields;
	}

	public List<Metadata> getTargetMetadata(ConnectorInstance<?> instance, String documentType) {
		MetadataSchema schema = getConnectorDocumentSchema(instance, documentType);
		List<Metadata> result = new ArrayList<>();
		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.getLocalCode().startsWith(MAPPING_METADATA_PREFIX)) {
				result.add(metadata);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public boolean canQuickConfig(ConnectorInstance<?> instance, String documentType) {
		for (List<String> mappings : getMapping(instance, documentType).values()) {
			if (mappings.size() > 1) {
				return false;
			}
		}
		return true;
	}

	public List<MappingParams> getDefaultMappingParams(ConnectorInstance<?> instance, String documentType) {
		List<MappingParams> result = new ArrayList<>();
		Map<String, Metadata> targets = getTargetMetadataMap(instance, documentType);
		Set<String> fields = getMappedFields(instance, documentType);
		for (ConnectorField field : getConnectorFields(instance, documentType)) {
			String code = cleanMetadataCode(field.getId());
			TargetParams params = new TargetParams(
					code, field.getLabel(), field.getType(), targets.containsKey(MAPPING_METADATA_PREFIX + code));
			MappingParams mapping = new MappingParams(field.getId(), params);
			mapping.setActive(fields.isEmpty() || fields.contains(field.getId()));
			result.add(mapping);
		}
		return Collections.unmodifiableList(result);
	}

	public Metadata createTargetMetadata(ConnectorInstance<?> instance, String documentType, TargetParams params) {
		if (instance == null) {
			throw new ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_InvalidArgument();
		}

		String collection = instance.getCollection();
		ConnectorMappingTransaction transaction = createTransaction(collection);
		String localCode = transaction.createTargetMetadata(instance, documentType, params);
		transaction.execute();

		MetadataSchema schema = getConnectorDocumentSchema(instance, documentType);
		return schema.getMetadata(localCode);
	}

	public Metadata createTargetUserMetadata(MetadataSchema schema, TargetParams params) {
		if (schema == null) {
			throw new ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_InvalidArgument();
		}

		String collection = schema.getCollection();
		ConnectorMappingTransaction transaction = createTransaction(collection);
		String localCode = transaction.createTargetUserMetadata(schema, params);
		transaction.execute();

		return metadataSchemasManager.getSchemaTypes(collection).getSchema(schema.getCode()).getMetadata(localCode);
	}

	public Map<String, List<String>> getMapping(ConnectorInstance<?> instance, String documentType) {
		Map<String, List<String>> allDocumentTypesMapping = new HashMap<>();

		if (instance.getPropertiesMapping() != null) {
			allDocumentTypesMapping.putAll(instance.getPropertiesMapping());
		}

		Map<String, List<String>> documentTypeMapping = new HashMap<>();
		for (Map.Entry<String, List<String>> mapEntry : allDocumentTypesMapping.entrySet()) {
			if (mapEntry.getKey().startsWith(documentType + ":")) {
				String key = mapEntry.getKey().split(":")[1];
				documentTypeMapping.put(key, mapEntry.getValue());
			}
		}
		return documentTypeMapping;
	}

	public <T extends ConnectorInstance> ConnectorInstance<T> setMapping(
			ConnectorInstance<T> instance, String documentType, Map<String, List<String>> mapping) {

		Map<String, List<String>> allDocumentTypesMapping = new HashMap<>();

		recordServices.refresh(instance);
		if (instance.getPropertiesMapping() != null) {
			allDocumentTypesMapping.putAll(instance.getPropertiesMapping());
		}

		Set<String> allKeys = new HashSet<>(allDocumentTypesMapping.keySet());
		for (String key : allKeys) {
			if (key.startsWith(documentType + ":")) {
				allDocumentTypesMapping.remove(key);
			}
		}

		for (Map.Entry<String, List<String>> mappingEntry : mapping.entrySet()) {
			allDocumentTypesMapping.put(documentType + ":" + mappingEntry.getKey(), mappingEntry.getValue());
		}
		instance.setPropertiesMapping(new MapStringListStringStructure(allDocumentTypesMapping));
		try {
			recordServices.update(instance);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		return instance;
	}

	public <T extends ConnectorInstance> ConnectorInstance<T> setMapping(
			ConnectorInstance<T> instance, String documentType, List<MappingParams> config) {

		ConnectorMappingTransaction transaction = createTransaction(instance.getCollection());
		Map<String, List<String>> mapping = new HashMap<>();
		for (MappingParams params : config) {
			if (params.isActive()) {
				String localCode = transaction.createTargetMetadata(instance, documentType, params.getTarget());
				mapping.put(localCode, Arrays.asList(params.getFieldId()));
			}
		}
		transaction.execute();
		return setMapping(instance, documentType, mapping);
	}

	private Set<String> getMappedFields(ConnectorInstance<?> instance, String documentType) {
		HashSet<String> result = new HashSet<>();
		for (List<String> fields : getMapping(instance, documentType).values()) {
			result.addAll(fields);
		}
		return result;
	}

	private Map<String, Metadata> getTargetMetadataMap(ConnectorInstance<?> instance, String documentType) {
		HashMap<String, Metadata> result = new HashMap<>();
		for (Metadata metadata : getTargetMetadata(instance, documentType)) {
			result.put(metadata.getLocalCode(), metadata);
		}
		return result;
	}

	private String cleanMetadataCode(String code) {
		String[] parts = code.split(":");
		String result = parts[parts.length - 1].replace("_", "");
		if (result.toLowerCase().endsWith("id")) {
			result += "0";
		}
		return result;
	}

	private MetadataSchema getConnectorDocumentSchema(ConnectorInstance<?> instance, String documentType) {
		Connector connector = getConnectorFor(instance);
		if (!connector.getConnectorDocumentTypes().contains(documentType)) {
			throw new ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_InvalidArgument();
		}

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(instance.getCollection());
		return types.getSchema(documentType + "_" + instance.getId());
	}

	private Connector getConnectorFor(ConnectorInstance<?> connectorInstance) {
		return connectorServicesFor(connectorInstance).instantiateConnector(connectorInstance);
	}

	private ConnectorUtilsServices connectorServicesFor(ConnectorInstance<?> connectorInstance) {
		return ConnectorServicesFactory.forConnectorInstance(es.getAppLayerFactory(), connectorInstance);
	}

	private ConnectorMappingTransaction createTransaction(String collection) {
		return new ConnectorMappingTransaction(
				metadataSchemasManager.modify(collection), new SchemaDisplayManagerTransaction());
	}

	public class ConnectorMappingTransaction {
		private final MetadataSchemaTypesBuilder types;
		private final SchemaDisplayManagerTransaction transaction;
		private final List<Alteration> alterations;

		public ConnectorMappingTransaction(MetadataSchemaTypesBuilder types, SchemaDisplayManagerTransaction transaction) {
			this.types = types;
			this.transaction = transaction;
			alterations = new ArrayList<>();
		}

		public String createTargetMetadata(ConnectorInstance<?> instance, String documentType, TargetParams params) {
			if (instance == null || documentType == null || !params.isValid()) {
				throw new ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_InvalidArgument();
			}
			MetadataSchema schema = getConnectorDocumentSchema(instance, documentType);
			extensions.beforeTargetMetadataCreation(new TargetMetadataCreationParams(this, params));
			return createTargetMetadata(schema, params, ConnectorMappingService.MAPPING_METADATA_PREFIX);
		}

		public String createTargetUserMetadata(MetadataSchema schema, TargetParams params) {
			if (schema == null || !params.isValid()) {
				throw new ConnectorMappingServiceRuntimeException.ConnectorMappingServiceRuntimeException_InvalidArgument();
			}
			return createTargetMetadata(schema, params, USER_METADATA_PREFIX);
		}

		private String createTargetMetadata(MetadataSchema schema, TargetParams params, String prefix) {
			String code = params.getCode();
			if (code.startsWith(prefix)) {
				code = code.substring(prefix.length());
			}

			String newMetadataLocalCode = prefix + code;

			if (!params.isExisting()) {
				if (schema.hasMetadataWithCode(newMetadataLocalCode)) {
					throw new ConnectorMappingServiceRuntimeException
							.ConnectorMappingServiceRuntimeException_MetadataAlreadyExist(code);
				}
				alterations.add(new Alteration(schema.getCode(), newMetadataLocalCode, params));
			}

			return newMetadataLocalCode;
		}

		protected void execute() {
			for (Alteration alteration : alterations) {
				MetadataBuilder builder = types.getSchemaType(new SchemaUtils().getSchemaTypeCode(alteration.schema))
						.getSchema(alteration.schema)
						.create(alteration.localCode)
						.setType(alteration.params.getType())
						.setMultivalue(alteration.params.isMultivalue())
						.setSearchable(alteration.params.isSearchable());

				for (String languageStr : collectionsManager.getCollectionLanguages(es.getCollection())) {
					builder.addLabel(Language.withCode(languageStr),
							alteration.params.getLabel());
				}
			}
			saveSchemas();

			for (Alteration alteration : alterations) {
				transaction.add(schemasDisplayManager.getMetadata(es.getCollection(), alteration.schema, alteration.localCode)
						.withVisibleInAdvancedSearchStatus(alteration.params.isAdvancedSearch()));

				if (alteration.params.isSearchResults()) {
					SchemaDisplayConfig schema = transaction.getModifiedSchema(alteration.schema);
					if (schema == null) {
						schema = schemasDisplayManager.getSchema(es.getCollection(), alteration.schema);
					}

					transaction.addReplacing(schema.withNewSearchResultMetadataCode(
							alteration.schema + "_" + alteration.localCode));
				}
			}
			schemasDisplayManager.execute(transaction);
		}

		private void saveSchemas() {
			try {
				metadataSchemasManager.saveUpdateSchemaTypes(types);
			} catch (OptimisticLocking e) {
				saveSchemas();
			}
		}

		private class Alteration {
			private final String schema;
			private final String localCode;
			private final TargetParams params;

			public Alteration(String schema, String localCode, TargetParams params) {
				this.localCode = localCode;
				this.schema = schema;
				this.params = params;
			}
		}
	}
}
