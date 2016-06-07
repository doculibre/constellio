package com.constellio.model.services.schemas.builders;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaComparators;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.CannotDeleteSchema;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.CannotDeleteSchemaSinceItHasRecords;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.utils.ClassProvider;

public class MetadataSchemaTypeBuilder {

	private static final String DEFAULT = "default";

	private static final String UNDERSCORE = "_";
	private final Set<MetadataSchemaBuilder> allSchemas = new HashSet<MetadataSchemaBuilder>();
	private String code;
	private String collection;
	private Map<Language, String> labels;
	private boolean security = true;
	private boolean inTransactionLog = true;
	private MetadataSchemaBuilder defaultSchema;
	private Set<MetadataSchemaBuilder> customSchemas = new HashSet<MetadataSchemaBuilder>();
	private Boolean undeletable = false;
	private ClassProvider classProvider;

	MetadataSchemaTypeBuilder() {
	}

	static MetadataSchemaTypeBuilder createNewSchemaType(String collection, String code,
			MetadataSchemaTypesBuilder typesBuilder) {
		return createNewSchemaType(collection, code, typesBuilder, true);
	}

	static MetadataSchemaTypeBuilder createNewSchemaType(String collection, String code,
			MetadataSchemaTypesBuilder typesBuilder, boolean initialize) {
		MetadataSchemaTypeBuilder builder = new MetadataSchemaTypeBuilder();
		builder.classProvider = typesBuilder.getClassProvider();
		builder.code = code;
		builder.collection = collection;
		builder.setLabels(configureLabels(code, typesBuilder));
		builder.customSchemas = new HashSet<>();
		builder.defaultSchema = MetadataSchemaBuilder.createDefaultSchema(builder, typesBuilder, initialize);

		return builder;
	}

	private static Map<Language, String> configureLabels(String code, MetadataSchemaTypesBuilder typesBuilder) {
		Map<Language, String> label = new HashMap<>();
		for (Language language : typesBuilder.getLanguages()) {
			label.put(language, code);
		}
		return label;
	}

	public static MetadataSchemaTypeBuilder modifySchemaType(MetadataSchemaType schemaType, ClassProvider classProvider) {
		MetadataSchemaTypeBuilder builder = new MetadataSchemaTypeBuilder();
		builder.classProvider = classProvider;
		builder.code = schemaType.getCode();
		builder.collection = schemaType.getCollection();
		builder.setLabels(schemaType.getLabels());
		builder.undeletable = schemaType.isUndeletable();
		builder.defaultSchema = MetadataSchemaBuilder.modifyDefaultSchema(schemaType.getDefaultSchema(), builder);
		builder.security = schemaType.hasSecurity();
		builder.inTransactionLog = schemaType.isInTransactionLog();
		builder.customSchemas = new HashSet<>();
		for (MetadataSchema schema : schemaType.getSchemas()) {
			builder.customSchemas.add(MetadataSchemaBuilder.modifySchema(schema, builder));
		}
		return builder;
	}

	public String getCode() {
		return code;
	}

	public String getCollection() {
		return collection;
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getLabel(Language language) {
		return labels.get(language);
	}

	public MetadataSchemaTypeBuilder setLabels(Map<Language, String> labels) {
		if (labels != null) {
			this.labels = new HashMap<>(labels);
		} else {
			this.labels = new HashMap<>();
		}
		return this;
	}

	public MetadataSchemaTypeBuilder addLabel(Language language, String label) {
		this.labels.put(language, label);
		return this;
	}

	public MetadataSchemaBuilder getDefaultSchema() {
		return defaultSchema;
	}

	public Set<MetadataSchemaBuilder> getCustomSchemas() {
		return customSchemas;
	}

	public Boolean isUndeletable() {
		return undeletable;
	}

	public void setUndeletable(Boolean undeletable) {
		this.undeletable = undeletable;
	}

	public Set<MetadataSchemaBuilder> getAllSchemas() {
		allSchemas.addAll(customSchemas);
		allSchemas.add(defaultSchema);
		return allSchemas;
	}

	public MetadataSchemaBuilder getCustomSchema(String localCode) {
		for (MetadataSchemaBuilder customSchema : customSchemas) {
			if (localCode.equals(customSchema.getLocalCode())) {
				return customSchema;
			}
		}
		throw new MetadataSchemaTypeBuilderRuntimeException.NoSuchSchema(localCode);
	}

	public static Map<Language, String> configureLabels(Map<String, String> labels) {
		Map<Language, String> newLabels = new HashMap<>();
		for (Entry<String, String> entry : labels.entrySet()) {
			newLabels.put(Language.withCode(entry.getKey()), entry.getValue());
		}
		return newLabels;
	}

	@Deprecated
	public MetadataSchemaBuilder createCustomSchema(String localCode) {
		for (MetadataSchemaBuilder customSchema : customSchemas) {
			if (localCode.equals(customSchema.getLocalCode())) {
				throw new MetadataSchemaTypeBuilderRuntimeException.SchemaAlreadyDefined(localCode);
			}
		}

		MetadataSchemaBuilder customSchema = MetadataSchemaBuilder.createSchema(defaultSchema, localCode, true);
		customSchema.setLocalCode(localCode);
		customSchema.setCollection(collection);
		customSchema.setCode(code + UNDERSCORE + localCode);
		customSchemas.add(customSchema);
		return customSchema;
	}

	public MetadataSchemaBuilder createCustomSchema(String localCode, Map<String, String> labels) {
		for (MetadataSchemaBuilder customSchema : customSchemas) {
			if (localCode.equals(customSchema.getLocalCode())) {
				throw new MetadataSchemaTypeBuilderRuntimeException.SchemaAlreadyDefined(localCode);
			}
		}

		MetadataSchemaBuilder customSchema = MetadataSchemaBuilder.createSchema(defaultSchema, localCode, true);
		customSchema.setLocalCode(localCode);
		customSchema.setCollection(collection);
		customSchema.setCode(code + UNDERSCORE + localCode);
		Map<Language, String> newLabels = configureLabels(labels);
		customSchema.setLabels(newLabels);
		customSchemas.add(customSchema);
		return customSchema;
	}

	public MetadataSchemaType build(DataStoreTypesFactory typesFactory, ModelLayerFactory modelLayerFactory) {
		MetadataSchema defaultSchema = this.defaultSchema.buildDefault(typesFactory, modelLayerFactory);

		List<MetadataSchema> schemas = new ArrayList<MetadataSchema>();
		for (MetadataSchemaBuilder metadataSchemaBuilder : this.customSchemas) {
			schemas.add(metadataSchemaBuilder.buildCustom(defaultSchema, typesFactory, modelLayerFactory));
		}

		if (labels == null || labels.isEmpty()) {
			throw new MetadataSchemaTypeBuilderRuntimeException.LabelNotDefined(code);
		} else {
			for (Entry<Language, String> entry : labels.entrySet()) {
				if (StringUtils.isBlank(entry.getValue())) {
					throw new MetadataSchemaTypeBuilderRuntimeException.LabelNotDefinedForLanguage(entry.getKey(), code);
				}
			}
		}

		Collections.sort(schemas, SchemaComparators.SCHEMA_COMPARATOR_BY_ASC_LOCAL_CODE);
		return new MetadataSchemaType(code, collection, labels, schemas, defaultSchema, undeletable, security, inTransactionLog);
	}

	public MetadataBuilder getMetadata(String metadataCode) {
		String[] parsedCode = metadataCode.split(UNDERSCORE);
		String typeCode = parsedCode[0];
		String schemaCode = parsedCode[1];
		String metadataLocalCode = parsedCode[2];

		if (!typeCode.equals(code)) {
			throw new CannotGetMetadatasOfAnotherSchemaType(typeCode, code);
		}

		MetadataBuilder metadata = null;
		if (schemaCode.equals(DEFAULT)) {
			metadata = getDefaultSchema().getMetadata(metadataLocalCode);
		} else {
			metadata = getCustomSchema(schemaCode).getMetadata(metadataLocalCode);
		}
		if (metadata == null) {
			throw new MetadataSchemaTypesBuilderRuntimeException.NoSuchMetadata(metadataLocalCode);
		} else {
			return metadata;
		}
	}

	public MetadataSchemaBuilder getSchema(String codeOrCode) {
		MetadataSchemaBuilder schema = null;
		if (codeOrCode.contains(UNDERSCORE)) {
			schema = getSchemaWithCompleteCode(codeOrCode);
		} else {
			schema = getSchemaWithCode(codeOrCode);
		}
		if (schema == null) {
			throw new MetadataSchemaTypeBuilderRuntimeException.NoSuchSchema(codeOrCode);
		} else {
			return schema;
		}
	}

	private MetadataSchemaBuilder getSchemaWithCode(String code) {
		return code.equals(DEFAULT) ? getDefaultSchema() : getCustomSchema(code);
	}

	private MetadataSchemaBuilder getSchemaWithCompleteCode(String schemaCode) {
		String[] parsedCode = schemaCode.split(UNDERSCORE);
		String type = parsedCode[0];
		if (!type.equals(code)) {
			throw new ImpossibleRuntimeException("Cannot obtain schema from other type");
		}
		String schemaLocalCode = parsedCode[1];
		return getSchemaWithCode(schemaLocalCode);
	}

	@Override
	public String toString() {
		return "MetadataSchemaTypeBuilder [code=" + code + ", label=" + labels + ", defaultSchema=" + defaultSchema
				+ ", customSchemas=" + customSchemas + ", undeletable=" + undeletable + "]";
	}

	public Set<MetadataBuilder> getAllMetadatas() {
		Set<MetadataBuilder> metadatas = new HashSet<>();
		metadatas.addAll(defaultSchema.getMetadatas());
		for (MetadataSchemaBuilder customSchema : customSchemas) {
			metadatas.addAll(customSchema.getMetadatasWithoutInheritance());
		}
		return metadatas;
	}

	public MetadataSchemaTypeBuilder setSecurity(boolean security) {
		this.security = security;
		return this;
	}

	public boolean isSecurity() {
		return security;
	}

	public MetadataSchemaTypeBuilder setInTransactionLog(boolean inTransactionLog) {
		this.inTransactionLog = inTransactionLog;
		return this;
	}

	public boolean isInTransactionLog() {
		return inTransactionLog;
	}

	public boolean hasSchema(String schema) {
		try {
			getSchema(schema);
			return true;
		} catch (MetadataSchemaTypeBuilderRuntimeException.NoSuchSchema e) {
			return false;
		}
	}

	public void deleteSchema(MetadataSchema schema, SearchServices searchServices) {
		if (searchServices.hasResults(from(schema).returnAll())) {
			throw new CannotDeleteSchemaSinceItHasRecords(schema.getCode());
		} else if (DEFAULT.equals(schema.getLocalCode())) {
			throw new CannotDeleteSchema(schema.getCode());
		} else {
			customSchemas.remove(getSchema(schema.getLocalCode()));
		}
	}

	public ClassProvider getClassProvider() {
		return classProvider;
	}

	public MetadataSchemaBuilder createCustomSchemaCopying(String localCode, String copyingSchemaWithLocalCode) {
		MetadataSchemaBuilder copiedSchemaBuilder = getCustomSchema(copyingSchemaWithLocalCode);

		for (MetadataSchemaBuilder customSchema : customSchemas) {
			if (localCode.equals(customSchema.getLocalCode())) {
				throw new MetadataSchemaTypeBuilderRuntimeException.SchemaAlreadyDefined(localCode);
			}
		}

		MetadataSchemaBuilder customSchema = MetadataSchemaBuilder.createSchema(defaultSchema, localCode, false);
		customSchema.setLocalCode(localCode);
		customSchema.setCollection(collection);
		customSchema.setCode(code + UNDERSCORE + localCode);
		customSchemas.add(customSchema);

		for (MetadataBuilder metadataBuilder : copiedSchemaBuilder.getMetadatas()) {
			customSchema.createMetadataCopying(metadataBuilder);
		}

		return customSchema;
	}
}
