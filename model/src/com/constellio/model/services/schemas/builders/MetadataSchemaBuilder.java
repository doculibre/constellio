package com.constellio.model.services.schemas.builders;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCode;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaComparators;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.NoSuchMetadata;
import com.constellio.model.utils.DependencyUtils;
import com.constellio.model.utils.DependencyUtilsRuntimeException;

public class MetadataSchemaBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSchemaBuilder.class);

	private static final String UNDERSCORE = "_";

	private static final String DEFAULT = "default";

	private String localCode;

	private String collection;

	private String code;

	private String label;

	private MetadataSchemaBuilder defaultSchema;

	private MetadataSchemaTypeBuilder schemaTypeBuilder;

	private List<MetadataBuilder> metadatas;

	private boolean undeletable = false;

	private ClassListBuilder<RecordValidator> schemaValidators;

	MetadataSchemaBuilder() {
	}

	static MetadataSchemaBuilder modifySchema(MetadataSchema schema, MetadataSchemaTypeBuilder schemaType) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();
		builder.setDefaultSchema(schemaType.getDefaultSchema());
		builder.setSchemaTypeBuilder(schemaType);
		builder.setLocalCode(schema.getLocalCode());
		builder.setCollection(schema.getCollection());
		builder.setCode(schema.getCode());
		builder.setUndeletable(schema.isUndeletable());
		builder.label = schema.getLabel();
		builder.metadatas = new ArrayList<>();
		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.inheritDefaultSchema()) {
				MetadataBuilder inheritance = builder.defaultSchema.getMetadata(metadata.getLocalCode());
				builder.metadatas.add(MetadataBuilder.modifyMetadataWithInheritance(metadata, inheritance));
			} else {
				builder.metadatas.add(MetadataBuilder.modifyMetadataWithoutInheritance(metadata));
			}
		}

		Set<RecordValidator> customValidators = new HashSet<>();
		for (RecordValidator validator : schema.getValidators()) {
			boolean contains = false;
			for (String defaultValidatorClassName : schemaType.getDefaultSchema().schemaValidators.implementationsClassname) {
				contains |= defaultValidatorClassName.equals(validator.getClass().getName());
			}

			if (!contains) {
				customValidators.add(validator);
			}
		}
		builder.schemaValidators = new ClassListBuilder<>(RecordValidator.class, customValidators);

		return builder;
	}

	static MetadataSchemaBuilder modifyDefaultSchema(MetadataSchema defaultSchema, MetadataSchemaTypeBuilder typeBuilder) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();

		builder.label = defaultSchema.getLabel();
		builder.setLocalCode(defaultSchema.getLocalCode());
		builder.setCode(defaultSchema.getCode());
		builder.setCollection(defaultSchema.getCollection());
		builder.setUndeletable(defaultSchema.isUndeletable());
		builder.setSchemaTypeBuilder(typeBuilder);
		builder.metadatas = new ArrayList<>();
		for (Metadata metadata : defaultSchema.getMetadatas()) {
			builder.metadatas.add(MetadataBuilder.modifyMetadataWithoutInheritance(metadata));
		}
		builder.schemaValidators = new ClassListBuilder<>(RecordValidator.class, defaultSchema.getValidators());
		return builder;
	}

	static MetadataSchemaBuilder createSchema(MetadataSchemaBuilder defaultSchema, String localCode) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();
		builder.setDefaultSchema(defaultSchema);
		builder.metadatas = new ArrayList<>();
		builder.setCollection(defaultSchema.getCollection());
		builder.setLocalCode(localCode);
		builder.setLabel(localCode);
		builder.setCode(defaultSchema.getSchemaTypeBuilder().getCode() + UNDERSCORE + localCode);

		for (MetadataBuilder metadata : defaultSchema.metadatas) {
			builder.metadatas.add(MetadataBuilder.createCustomMetadataFromDefault(metadata, localCode));
		}

		builder.schemaValidators = new ClassListBuilder<>(RecordValidator.class);
		return builder;
	}

	static MetadataSchemaBuilder createDefaultSchema(MetadataSchemaTypeBuilder schemaTypeBuilder,
			MetadataSchemaTypesBuilder schemaTypesBuilder, boolean initialize) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();
		builder.setSchemaTypeBuilder(schemaTypeBuilder);
		builder.setLocalCode(DEFAULT);
		builder.setCollection(schemaTypeBuilder.getCollection());
		builder.setLabel(schemaTypeBuilder.getLabel());
		builder.setCode(schemaTypeBuilder.getCode() + UNDERSCORE + DEFAULT);
		builder.setUndeletable(true);
		builder.metadatas = new ArrayList<>();
		builder.schemaValidators = new ClassListBuilder<>(RecordValidator.class);
		if (initialize) {
			new CommonMetadataBuilder().addCommonMetadataToNewSchema(builder, schemaTypesBuilder);
		}
		return builder;
	}

	public String getCollection() {
		return collection;
	}

	MetadataSchemaBuilder setCollection(String collection) {
		this.collection = collection;
		return this;
	}

	public String getCode() {
		return code;
	}

	MetadataSchemaBuilder setCode(String code) {
		this.code = code;
		return this;
	}

	public String getLocalCode() {
		return localCode;
	}

	MetadataSchemaBuilder setLocalCode(String localCode) {
		this.localCode = localCode;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public MetadataSchemaBuilder setLabel(String label) {
		this.label = label;
		return this;
	}

	public MetadataSchemaBuilder getDefaultSchema() {
		return defaultSchema;
	}

	public MetadataSchemaBuilder setDefaultSchema(MetadataSchemaBuilder defaultSchema) {
		this.defaultSchema = defaultSchema;
		this.setSchemaTypeBuilder(defaultSchema.getSchemaTypeBuilder());
		return this;
	}

	public List<MetadataBuilder> getMetadatas() {
		return metadatas;
	}

	public MetadataSchemaTypeBuilder getSchemaTypeBuilder() {
		return schemaTypeBuilder;
	}

	private MetadataSchemaBuilder setSchemaTypeBuilder(MetadataSchemaTypeBuilder schemaTypeBuilder) {
		this.schemaTypeBuilder = schemaTypeBuilder;
		return this;
	}

	public boolean isUndeletable() {
		return undeletable;
	}

	public MetadataSchemaBuilder setUndeletable(Boolean undeletable) {
		this.undeletable = undeletable;
		return this;
	}

	public boolean hasMetadata(String codeOrLocalCode) {
		return getMetadataOrNull(codeOrLocalCode) != null;
	}

	public MetadataBuilder getUserMetadata(String localCode) {
		for (MetadataBuilder metadataBuilder : getMetadatas()) {
			if (metadataBuilder.getLocalCode().equals("USR" + localCode)) {
				return metadataBuilder;
			}
		}
		throw new MetadataSchemaBuilderRuntimeException.NoSuchMetadata("USR" + localCode);
	}

	private MetadataBuilder getMetadataOrNull(String codeOrLocalCode) {
		String partialCode;
		if (codeOrLocalCode.split(UNDERSCORE).length == 3) {
			partialCode = getPartialCode(codeOrLocalCode);
		} else if (codeOrLocalCode.matches("([a-zA-Z0-9])+")) {
			partialCode = codeOrLocalCode;
		} else {
			throw new MetadataSchemaBuilderRuntimeException.InvalidAttribute("codeOrLocalCode", codeOrLocalCode);
		}
		for (MetadataBuilder metadataBuilder : getMetadatas()) {
			if (metadataBuilder.getLocalCode().equals(partialCode)) {
				return metadataBuilder;
			}
		}
		return null;
	}

	public MetadataBuilder getMetadata(String codeOrLocalCode) {

		MetadataBuilder metadataBuilder = getMetadataOrNull(codeOrLocalCode);
		if (metadataBuilder == null) {
			throw new MetadataSchemaBuilderRuntimeException.NoSuchMetadata(codeOrLocalCode);
		} else {
			return metadataBuilder;
		}
	}

	public MetadataBuilder get(String code) {
		String metadataCode = new SchemaUtils().toLocalMetadataCode(code);

		validateLocalCode(metadataCode);
		return getMetadata(metadataCode);
	}

	public MetadataBuilder createSystemReserved(String code) {
		return this.create(code).setUndeletable(true).setSystemReserved(true);
	}

	public MetadataBuilder createUndeletable(String code) {
		return this.create(code).setUndeletable(true);
	}

	public MetadataBuilder create(String metadataLocaleCode) {

		String metadataLocalCode = new SchemaUtils().toLocalMetadataCode(metadataLocaleCode);
		validateLocalCode(metadataLocalCode);

		if (hasMetadata(metadataLocalCode)) {
			throw new MetadataSchemaBuilderRuntimeException.MetadataAlreadyExists(metadataLocaleCode);
		}

		if (this.getLocalCode().equals(DEFAULT)) {
			return createDefaultMetadata(metadataLocaleCode);
		} else {
			return createCustomMetadata(metadataLocaleCode);
		}

	}

	MetadataSchema buildDefault(DataStoreTypesFactory typesFactory, ModelLayerFactory modelLayerFactory) {
		List<Metadata> newMetadatas = buildMetadatas(typesFactory, modelLayerFactory);

		validateDefault(this);

		String newLabel = this.getLabel();
		if (newLabel == null) {
			newLabel = schemaTypeBuilder.getLabel();
		}

		if (newLabel == null) {
			newLabel = schemaTypeBuilder.getCode();
		}

		Collections.sort(newMetadatas, SchemaComparators.METADATA_COMPARATOR_BY_ASC_LOCAL_CODE);

		boolean inTransactionLog = schemaTypeBuilder.isInTransactionLog();
		return new MetadataSchema(this.getLocalCode(), this.getCode(), collection, newLabel, newMetadatas, this.isUndeletable(),
				inTransactionLog, this.schemaValidators.build(), orderAutomaticMetadatas(newMetadatas));
	}

	List<Metadata> buildMetadatas(DataStoreTypesFactory typesFactory, ModelLayerFactory modelLayerFactory) {
		List<Metadata> newMetadatas = new ArrayList<>();
		for (MetadataBuilder metadataBuilder : this.metadatas) {
			newMetadatas.add(metadataBuilder.buildWithoutInheritance(typesFactory, modelLayerFactory));
		}
		return newMetadatas;
	}

	private List<Metadata> orderAutomaticMetadatas(List<Metadata> metadatas) {
		Map<String, Set<String>> automaticMetadatasDependencies = newSchemaUtils().calculatedMetadataDependencies(metadatas);
		List<String> sortedMetadataCodes;

		try {
			sortedMetadataCodes = newDependencyUtils()
					.sortByDependency(automaticMetadatasDependencies);
		} catch (DependencyUtilsRuntimeException.CyclicDependency e) {
			throw new MetadataSchemaBuilderRuntimeException.CyclicDependenciesInMetadata(e);
		}
		List<Metadata> sortedMetadatas = new ArrayList<>();
		for (String sortedMetadataCode : sortedMetadataCodes) {
			for (Metadata metadata : metadatas) {
				if (sortedMetadataCode.equals(metadata.getLocalCode())) {
					sortedMetadatas.add(metadata);
				}
			}
		}

		return Collections.unmodifiableList(sortedMetadatas);
	}

	DependencyUtils<String> newDependencyUtils() {
		return new DependencyUtils<>();
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}

	MetadataSchema buildCustom(MetadataSchema defaultSchema, DataStoreTypesFactory typesFactory,
			ModelLayerFactory modelLayerFactory) {
		List<Metadata> newMetadatas = new ArrayList<>();
		for (MetadataBuilder metadataBuilder : this.metadatas) {
			try {
				Metadata inheritance = defaultSchema.getMetadata(metadataBuilder.getLocalCode());
				newMetadatas.add(metadataBuilder.buildWithInheritance(inheritance));
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				LOGGER.debug("No inheritance found for metadata {}", code, e);
				newMetadatas.add(metadataBuilder.buildWithoutInheritance(typesFactory, modelLayerFactory));
			}

		}

		String newLabel = this.getLabel();
		if (newLabel == null) {
			newLabel = localCode;
		}

		Collections.sort(newMetadatas, SchemaComparators.METADATA_COMPARATOR_BY_ASC_LOCAL_CODE);

		boolean inTransactionLog = schemaTypeBuilder.isInTransactionLog();
		return new MetadataSchema(this.getLocalCode(), this.getCode(), collection, newLabel, newMetadatas, this.isUndeletable(),
				inTransactionLog, this.schemaValidators.build(defaultSchema.getValidators()),
				orderAutomaticMetadatas(newMetadatas));
	}

	public boolean isInheriting() {
		return defaultSchema != null;
	}

	@Override
	public String toString() {
		return "MetadataSchemaBuilder [localCode=" + localCode + ", code=" + code + ", label=" + label + ", metadatas="
				+ metadatas + ", undeletable=" + undeletable + "]";
	}

	public ClassListBuilder<RecordValidator> defineValidators() {
		return schemaValidators;
	}

	void validateLocalCode(String localCode) {
		String pattern = "([a-zA-Z0-9])+";
		if (localCode == null || !localCode.matches(pattern)) {
			throw new MetadataSchemaBuilderRuntimeException.InvalidAttribute("localCode", localCode);
		}
	}

	private String getPartialCode(String codeOrLocalCode) {
		String partialCode;
		String[] parts = codeOrLocalCode.split(UNDERSCORE);

		if (parts.length != 3) {
			throw new InvalidCode(code);
		}

		partialCode = parts[2];

		String requestedType = parts[0];
		String requestedSchema = parts[1];
		if (!code.startsWith(requestedType)) {
			throw new CannotGetMetadatasOfAnotherSchemaType(requestedType, code);
		}

		if (!requestedSchema.equals(localCode) && !requestedSchema.equals(MetadataSchemaType.DEFAULT)) {
			throw new CannotGetMetadatasOfAnotherSchema(requestedSchema, localCode);
		}

		return partialCode;
	}

	private MetadataBuilder createDefaultMetadata(String localCode) {
		MetadataBuilder metadata = MetadataBuilder.createMetadataWithoutInheritance(localCode, this);
		this.metadatas.add(metadata);
		for (MetadataSchemaBuilder customSchemaBuilder : schemaTypeBuilder.getCustomSchemas()) {
			customSchemaBuilder.metadatas.add(MetadataBuilder.createCustomMetadataFromDefault(metadata,
					customSchemaBuilder.localCode));
		}
		return metadata;
	}

	private MetadataBuilder createCustomMetadata(String code) {
		MetadataBuilder builder = MetadataBuilder.createMetadataWithoutInheritance(code, this);
		this.metadatas.add(builder);
		return builder;
	}

	private void validateDefault(MetadataSchemaBuilder builder) {
		validateLocalCode(localCode);
	}

	public Set<MetadataBuilder> getMetadatasWithoutInheritance() {
		Set<MetadataBuilder> metadatasWithoutInheritance = new HashSet<>();
		for (MetadataBuilder metadata : metadatas) {
			if (metadata.getInheritance() == null) {
				metadatasWithoutInheritance.add(metadata);
			}
		}
		return metadatasWithoutInheritance;
	}

	public void createUniqueCodeMetadata() {
		createUndeletable("code").setEssential(true).setDefaultRequirement(true).setType(STRING).setUniqueValue(true);
	}

	public void deleteMetadataWithoutValidation(Metadata metadataToDelete) {
		try {
			MetadataBuilder metadataBuilder = getMetadata(metadataToDelete.getLocalCode());
			metadatas.remove(metadataBuilder);
			if (metadataBuilder.getCode().contains("_default_")) {
				deleteInheritedMetadatas(metadataToDelete.getLocalCode());
			}
		} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchMetadata e) {
			//OK
		} catch (NoSuchMetadata e1) {
			//OK
		}
	}

	private void deleteInheritedMetadatas(String metadataLocalCode) {
		for (MetadataSchemaBuilder customSchemaBuilder : schemaTypeBuilder.getCustomSchemas()) {
			MetadataBuilder metadataToDelete = customSchemaBuilder.getMetadata(metadataLocalCode);
			customSchemaBuilder.deleteInheritedMetadata(metadataToDelete.getLocalCode());
		}
	}

	void deleteInheritedMetadata(String localCode) {
		try {
			MetadataBuilder metadataToDelete = getMetadata(localCode);
			metadatas.remove(metadataToDelete);
		} catch (MetadataSchemaTypesBuilderRuntimeException.NoSuchMetadata e) {
			//OK
		} catch (NoSuchMetadata e1) {
			//OK
		}
	}
}
