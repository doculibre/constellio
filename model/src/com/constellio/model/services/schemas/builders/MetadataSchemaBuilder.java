package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaCalculatedInfos;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCode;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.preparationSteps.CalculateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.SequenceRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.UpdateCreationModificationUsersAndDateRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateCyclicReferencesRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateMetadatasRecordPreparationStep;
import com.constellio.model.entities.schemas.preparationSteps.ValidateUsingSchemaValidatorsRecordPreparationStep;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.extensions.behaviors.SchemaExtension.SchemaInCreationBeforeSaveEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaComparators;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.NoSuchMetadata;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.DependencyUtils;
import com.constellio.model.utils.DependencyUtilsRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class MetadataSchemaBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSchemaBuilder.class);

	private static final String UNDERSCORE = "_";

	private static final String DEFAULT = "default";

	private short id;

	private String localCode;

	private CollectionInfo collectionInfo;

	private String code;

	private Map<Language, String> labels;

	private MetadataSchemaBuilder defaultSchema;

	private MetadataSchemaTypeBuilder schemaTypeBuilder;

	private List<MetadataBuilder> metadatas;

	private boolean undeletable = false;

	private ClassProvider classProvider;

	private ClassListBuilder<RecordValidator> schemaValidators;

	private boolean active = true;

	MetadataSchemaBuilder() {
	}

	public static Logger getLOGGER() {
		return LOGGER;
	}


	static MetadataSchemaBuilder modifySchema(MetadataSchema schema, MetadataSchemaTypeBuilder schemaType) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();
		builder.classProvider = schemaType.getClassProvider();
		builder.setDefaultSchema(schemaType.getDefaultSchema());
		builder.setSchemaTypeBuilder(schemaType);
		builder.setLocalCode(schema.getLocalCode());
		builder.setCollectionInfo(schema.getCollectionInfo());
		builder.setCode(schema.getCode());
		builder.id = schema.getId();
		builder.setUndeletable(schema.isUndeletable());
		builder.setLabels(schema.getLabels());
		builder.setActive(schema.isActive());
		builder.metadatas = new ArrayList<>();
		for (Metadata metadata : schema.getMetadatas()) {
			if (metadata.inheritDefaultSchema()) {
				MetadataBuilder inheritance = builder.defaultSchema.getMetadata(metadata.getLocalCode());
				builder.metadatas.add(MetadataBuilder.modifyMetadataWithInheritance(builder, metadata, inheritance));
			} else {
				builder.metadatas.add(MetadataBuilder.modifyMetadataWithoutInheritance(builder, metadata, builder.classProvider));
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
		builder.schemaValidators = new ClassListBuilder<>(builder.classProvider, RecordValidator.class, customValidators);

		return builder;
	}

	static MetadataSchemaBuilder modifyDefaultSchema(MetadataSchema defaultSchema,
													 MetadataSchemaTypeBuilder typeBuilder) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();
		builder.classProvider = typeBuilder.getClassProvider();
		builder.setLabels(defaultSchema.getLabels());
		builder.setLocalCode(defaultSchema.getLocalCode());
		builder.setCode(defaultSchema.getCode());
		builder.setCollectionInfo(defaultSchema.getCollectionInfo());
		builder.id = defaultSchema.getId();
		builder.setUndeletable(defaultSchema.isUndeletable());
		builder.setSchemaTypeBuilder(typeBuilder);
		builder.setActive(defaultSchema.isActive());
		builder.metadatas = new ArrayList<>();
		for (Metadata metadata : defaultSchema.getMetadatas()) {
			builder.metadatas.add(MetadataBuilder.modifyMetadataWithoutInheritance(builder, metadata, builder.classProvider));
		}
		builder.schemaValidators = new ClassListBuilder<>(builder.classProvider, RecordValidator.class,
				defaultSchema.getValidators());
		return builder;
	}

	static MetadataSchemaBuilder createSchema(MetadataSchemaBuilder defaultSchema, String localCode,
											  boolean commonMetadatas) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();

		builder.classProvider = defaultSchema.classProvider;
		builder.setDefaultSchema(defaultSchema);
		builder.metadatas = new ArrayList<>();
		builder.setCollectionInfo(defaultSchema.getCollectionInfo());
		builder.setLocalCode(localCode);
		builder.setLabels(configureLabels(localCode, defaultSchema));
		builder.setActive(defaultSchema.isActive());
		builder.setCode(defaultSchema.getSchemaTypeBuilder().getCode() + UNDERSCORE + localCode);

		for (MetadataBuilder metadata : defaultSchema.metadatas) {
			builder.metadatas.add(MetadataBuilder.createCustomMetadataFromDefault(builder, metadata, localCode));
		}

		builder.schemaValidators = new ClassListBuilder<>(builder.classProvider, RecordValidator.class);
		return builder;
	}

	private static Map<Language, String> configureLabels(String code, MetadataSchemaBuilder typesBuilder) {
		return configureLabels(code, typesBuilder, new HashMap<Language, String>());
	}

	private static Map<Language, String> configureLabels(String code, MetadataSchemaBuilder typesBuilder,
														 Map<Language, String> labels) {
		for (Language language : typesBuilder.getLabels().keySet()) {
			if (labels.get(language) == null || StringUtils.isBlank(labels.get(language))) {
				labels.put(language, code);
			}
		}
		return labels;
	}

	static MetadataSchemaBuilder createDefaultSchema(ModelLayerFactory modelLayerFactory,
													 MetadataSchemaTypeBuilder schemaTypeBuilder,
													 MetadataSchemaTypesBuilder schemaTypesBuilder,
													 boolean initialize) {
		MetadataSchemaBuilder builder = new MetadataSchemaBuilder();
		builder.classProvider = schemaTypeBuilder.getClassProvider();
		builder.setSchemaTypeBuilder(schemaTypeBuilder);
		builder.setLocalCode(DEFAULT);
		builder.setCollectionInfo(schemaTypeBuilder.getCollectionInfo());
		builder.setLabels(schemaTypeBuilder.getLabels());
		builder.setCode(schemaTypeBuilder.getCode() + UNDERSCORE + DEFAULT);
		builder.setUndeletable(true);
		builder.metadatas = new ArrayList<>();
		builder.schemaValidators = new ClassListBuilder<>(builder.classProvider, RecordValidator.class);
		if (initialize) {
			new CommonMetadataBuilder().addCommonMetadataToNewSchema(builder, schemaTypesBuilder);

			if (modelLayerFactory.getExtensions() != null) {
				modelLayerFactory.getExtensions().forCollection(schemaTypeBuilder.getCollection())
						.schemaExtensions.getExtensions().forEach(extension -> {
					extension.schemaInCreationBeforeSave(
							new SchemaInCreationBeforeSaveEvent(builder, schemaTypesBuilder.getLanguages()));

				});
			}
		}
		return builder;
	}

	public String getCollection() {
		return collectionInfo.getCode();
	}

	public CollectionInfo getCollectionInfo() {
		return collectionInfo;
	}

	MetadataSchemaBuilder setCollectionInfo(CollectionInfo collectionInfo) {
		this.collectionInfo = collectionInfo;
		return this;
	}

	public String getCode() {
		return code;
	}

	MetadataSchemaBuilder setCode(String code) {
		this.code = code;
		return this;
	}

	public short getId() {
		return id;
	}

	public String getLocalCode() {
		return localCode;
	}

	MetadataSchemaBuilder setLocalCode(String localCode) {
		this.localCode = localCode;
		return this;
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getLabel(Language language) {
		return labels.get(language);
	}

	public MetadataSchemaBuilder setLabels(Map<Language, String> labels) {
		this.labels = new HashMap<>(labels);
		return this;
	}

	public MetadataSchemaBuilder addLabel(Language language, String label) {
		this.labels.put(language, label);
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
		String[] codeSplitted = SchemaUtils.underscoreSplitWithCache(codeOrLocalCode);
		if (codeSplitted.length == 3) {
			partialCode = getPartialCode(codeOrLocalCode);
		} else if (codeSplitted.length == 1) {
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

	public MetadataBuilder get(Metadata metadata) {
		return get(metadata.getLocalCode());
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


	public MetadataBuilder createIfInexisting(String code, Consumer<MetadataBuilder> metadataConsumer) {
		if (!hasMetadata(code)) {
			MetadataBuilder metadataBuilder = create(code);
			metadataConsumer.accept(metadataBuilder);
			return metadataBuilder;
		} else {
			return get(code);
		}

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

	MetadataSchema buildDefault(DataStoreTypesFactory typesFactory, MetadataSchemaTypeBuilder typeBuilder,
								MetadataSchemaTypesBuilder typesBuilder, short typeId,
								ModelLayerFactory modelLayerFactory) {
		MetadataList newMetadatas = buildMetadatas(typesFactory, typeBuilder, typeId, modelLayerFactory);

		validateDefault(this);

		Map<Language, String> newLabels = this.getLabels();
		if (newLabels == null || newLabels.isEmpty()) {
			newLabels = schemaTypeBuilder.getLabels();
		} else {
			for (Language language : schemaTypeBuilder.getLabels().keySet()) {
				if (StringUtils.isBlank(newLabels.get(language))) {
					newLabels.put(language, schemaTypeBuilder.getLabel(language));
				}
			}
		}

		Collections.sort(newMetadatas, SchemaComparators.METADATA_COMPARATOR_BY_ASC_LOCAL_CODE);

		boolean inTransactionLog = schemaTypeBuilder.isInTransactionLog();
		Set<RecordValidator> recordValidators = this.schemaValidators.build();

		if (id == 0) {
			id = typeBuilder.nextSchemaId();
		}

		Set<String> typesWithSummaryCache = getTypesWithSummaryCache(typesBuilder);

		MetadataSchema metadataSchema = new MetadataSchema(id, this.getLocalCode(), this.getCode(), collectionInfo, newLabels, newMetadatas,
				this.isUndeletable(),
				inTransactionLog, recordValidators, calculateSchemaInfos(newMetadatas, recordValidators),
				schemaTypeBuilder.getDataStore(), this.isActive(), modelLayerFactory.getSystemConfigs(), typesWithSummaryCache);
		return metadataSchema;
	}

	@NotNull
	private Set<String> getTypesWithSummaryCache(MetadataSchemaTypesBuilder typesBuilder) {
		Set<String> typesWithSummaryCache = new HashSet<>();
		for (MetadataSchemaTypeBuilder aTypeBuilder : typesBuilder.getTypes()) {
			if (aTypeBuilder.getRecordCacheType() != null && aTypeBuilder.getRecordCacheType().isSummaryCache()) {
				typesWithSummaryCache.add(aTypeBuilder.getCode());
			}
		}
		return typesWithSummaryCache;
	}

	public String getTypeCode() {

		return schemaTypeBuilder.getCode();
	}

	public void resetAllIds(SchemasIdSequence sequenceForSchemaAndTypeId,
							SchemasIdSequence metadataForSchemaAndTypeId) {
		id = sequenceForSchemaAndTypeId.getNewId();

		List<MetadataBuilder> metadatas = new ArrayList<>(getMetadatas());
		metadatas.sort(Comparator.comparing(MetadataBuilder::getCode));

		for (MetadataBuilder metadata : metadatas) {
			if (metadata.getInheritance() != null) {
				metadata.id = metadata.getInheritance().getId();
			} else {
				short newId = metadataForSchemaAndTypeId.getNewId();
				metadata.id = newId;
			}
		}

	}

	private static class SchemaRecordSteps {

		List<Metadata> automaticMetadatas;
		List<RecordPreparationStep> steps;

	}

	private MetadataSchemaCalculatedInfos calculateSchemaInfos(MetadataList newMetadatas,
															   Set<RecordValidator> recordValidators) {

		for (Metadata metadata : newMetadatas.onlyCalculated().onlyWithoutInheritance()) {
			MetadataValueCalculator<?> calculator = ((CalculatedDataEntry) metadata.getDataEntry())
					.getCalculator();
			if (calculator instanceof InitializedMetadataValueCalculator) {
				((InitializedMetadataValueCalculator) calculator).initialize(newMetadatas, metadata);
			}
		}

		Map<String, Set<String>> allAutoMetadatasDependencies = newSchemaUtils().calculatedMetadataDependencies(newMetadatas);

		List<String> sequenceMetadatas = newMetadatas.onlySequence().toLocalCodesList();
		Set<String> metadatasDependingOnSequence = new HashSet<>(sequenceMetadatas);
		Map<String, Set<String>> autoMetadatasDependencies = new HashMap<>();
		Map<String, Set<String>> autoMetadatasDependenciesBasedOnSequence = new HashMap<>();

		boolean hasNewerMetadatasDependingOnSequence = true;
		while (hasNewerMetadatasDependingOnSequence) {
			int sizeBefore = metadatasDependingOnSequence.size();

			for (Map.Entry<String, Set<String>> entry : allAutoMetadatasDependencies.entrySet()) {
				for (String dependency : entry.getValue()) {
					if (metadatasDependingOnSequence.contains(dependency)) {
						metadatasDependingOnSequence.add(entry.getKey());
					}
				}
			}

			hasNewerMetadatasDependingOnSequence = metadatasDependingOnSequence.size() != sizeBefore;
		}

		for (Map.Entry<String, Set<String>> entry : allAutoMetadatasDependencies.entrySet()) {
			if (metadatasDependingOnSequence.contains(entry.getKey())) {
				autoMetadatasDependenciesBasedOnSequence.put(entry.getKey(), entry.getValue());
			} else {
				autoMetadatasDependencies.put(entry.getKey(), entry.getValue());
			}
		}

		List<Metadata> autoMetas = orderAutomaticMetadatas(newMetadatas, autoMetadatasDependencies);
		List<Metadata> autoMetasBasedOnSequence = orderAutomaticMetadatas(newMetadatas,
				autoMetadatasDependenciesBasedOnSequence);

		List<Metadata> automaticMetadatas = new ArrayList<>();
		automaticMetadatas.addAll(autoMetas);
		automaticMetadatas.addAll(autoMetasBasedOnSequence);

		List<Metadata> lazyTransientsMetadatas = new ArrayList<>();
		List<Metadata> eagerTransientsMetadatas = new ArrayList<>();
		for (Metadata automaticMetadata : automaticMetadatas) {
			if (automaticMetadata.getTransiency() == MetadataTransiency.TRANSIENT_EAGER) {
				eagerTransientsMetadatas.add(automaticMetadata);
			}
			if (automaticMetadata.getTransiency() == MetadataTransiency.TRANSIENT_LAZY) {
				lazyTransientsMetadatas.add(automaticMetadata);
			}
		}

		List<RecordPreparationStep> steps = new ArrayList<>();
		steps.add(new UpdateCreationModificationUsersAndDateRecordPreparationStep());
		steps.add(new ValidateMetadatasRecordPreparationStep(newMetadatas.onlyManualsOrAutomaticWithEvaluator().onlyNonSystemReserved(), false));
		steps.add(new CalculateMetadatasRecordPreparationStep(autoMetas));
		steps.add(new ValidateCyclicReferencesRecordPreparationStep());
		steps.add(new ValidateMetadatasRecordPreparationStep(autoMetas, true));
		steps.add(new ValidateUsingSchemaValidatorsRecordPreparationStep(new ArrayList<>(recordValidators)));

		if (!sequenceMetadatas.isEmpty()) {
			steps.add(new SequenceRecordPreparationStep(newMetadatas.onlySequence()));
		}

		if (!autoMetasBasedOnSequence.isEmpty()) {
			steps.add(new CalculateMetadatasRecordPreparationStep(autoMetasBasedOnSequence));
			steps.add(new ValidateMetadatasRecordPreparationStep(autoMetasBasedOnSequence, true));
			if (!recordValidators.isEmpty()) {
				steps.add(new ValidateUsingSchemaValidatorsRecordPreparationStep(new ArrayList<>(recordValidators)));
			}
		}

		List<Metadata> contentMetadatas = newMetadatas.onlyWithType(MetadataValueType.CONTENT)
				.sortedUsing(new ContentsComparator());
		return new MetadataSchemaCalculatedInfos(steps, automaticMetadatas, contentMetadatas, lazyTransientsMetadatas,
				eagerTransientsMetadatas);
	}

	public static class ContentsComparator implements Comparator<Metadata> {
		@Override
		public int compare(Metadata m1, Metadata m2) {
			if (m1.isDefaultRequirement() && !m2.isDefaultRequirement()) {
				return -1;

			} else if (!m1.isDefaultRequirement() && m2.isDefaultRequirement()) {
				return 1;

			} else if (m1.isMultivalue() && !m2.isMultivalue()) {
				return 1;

			} else if (!m1.isMultivalue() && m2.isMultivalue()) {
				return -1;

			} else {
				String code1 = m1.getLocalCode();
				String code2 = m2.getLocalCode();
				return code1.compareTo(code2);
			}

		}

	}

	MetadataList buildMetadatas(DataStoreTypesFactory typesFactory, MetadataSchemaTypeBuilder typeBuilder,
								short typeId, ModelLayerFactory modelLayerFactory) {
		MetadataList newMetadatas = new MetadataList();
		for (MetadataBuilder metadataBuilder : this.metadatas) {
			newMetadatas.add(metadataBuilder.buildWithoutInheritance(typesFactory, typeBuilder, typeId, modelLayerFactory));
		}
		return newMetadatas;
	}

	private List<Metadata> orderAutomaticMetadatas(List<Metadata> metadatas,
												   Map<String, Set<String>> automaticMetadatasDependencies) {
		List<String> sortedMetadataCodes;

		try {
			sortedMetadataCodes = newDependencyUtils()
					.sortByDependency(automaticMetadatasDependencies);
		} catch (DependencyUtilsRuntimeException.CyclicDependency e) {
			throw new MetadataSchemaBuilderRuntimeException.CyclicDependenciesInMetadata(e);
		}

		List<Metadata> sortedMetadatas = new ArrayList<>();

		for (Metadata metadata : metadatas) {
			if (metadata.getDataEntry().getType() == DataEntryType.AGGREGATED) {
				sortedMetadatas.add(metadata);
			}
		}

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

	MetadataSchema buildCustom(MetadataSchema defaultSchema, MetadataSchemaTypeBuilder typeBuilder,
							   MetadataSchemaTypesBuilder typesBuilder, short typeId,
							   DataStoreTypesFactory typesFactory, ModelLayerFactory modelLayerFactory) {
		final MetadataList newMetadatas = new MetadataList();
		for (MetadataBuilder metadataBuilder : this.metadatas) {
			try {
				Metadata inheritance = defaultSchema.getMetadata(metadataBuilder.getLocalCode());
				newMetadatas.add(metadataBuilder.buildWithInheritance(inheritance));
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				LOGGER.debug("No inheritance found for metadata {}", code, e);
				newMetadatas.add(metadataBuilder.buildWithoutInheritance(typesFactory, typeBuilder, typeId, modelLayerFactory));
			}

		}

		Map<Language, String> newLabels = configureLabels(localCode, getDefaultSchema(), this.getLabels());

		Collections.sort(newMetadatas, SchemaComparators.METADATA_COMPARATOR_BY_ASC_LOCAL_CODE);

		final Set<RecordValidator> recordValidators = this.schemaValidators.build(defaultSchema.getValidators());

		if (id == 0) {
			id = typeBuilder.nextSchemaId();
		}

		boolean inTransactionLog = schemaTypeBuilder.isInTransactionLog();
		MetadataSchema metadataSchema = new MetadataSchema(this.getId(), this.getLocalCode(), this.getCode(), collectionInfo, newLabels, newMetadatas,
				this.isUndeletable(), inTransactionLog, recordValidators, calculateSchemaInfos(newMetadatas, recordValidators)
				, schemaTypeBuilder.getDataStore(), this.isActive(), modelLayerFactory.getSystemConfigs(), getTypesWithSummaryCache(typesBuilder));
		return metadataSchema;
	}

	public boolean isInheriting() {
		return defaultSchema != null;
	}

	@Override
	public String toString() {
		return "MetadataSchemaBuilder [localCode=" + localCode + ", code=" + code + ", label=" + labels + ", metadatas="
			   + metadatas + ", undeletable=" + undeletable + "]";
	}

	public ClassListBuilder<RecordValidator> defineValidators() {
		return schemaValidators;
	}

	void validateLocalCode(String localCode) {
		if (!SchemaUtils.isValidSchemaCodeWithCache(localCode)) {
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
			customSchemaBuilder.metadatas.add(MetadataBuilder.createCustomMetadataFromDefault(customSchemaBuilder, metadata,
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

	public void deleteMetadataWithoutValidation(String localCode) {
		if (hasMetadata(localCode)) {
			deleteMetadataWithoutValidation(getMetadata(localCode));
		}
	}

	public void deleteMetadataWithoutValidation(MetadataBuilder metadataToDelete) {
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

	public ClassProvider getClassProvider() {
		return classProvider;
	}

	public MetadataBuilder createMetadataCopying(MetadataBuilder metadataBuilder) {
		MetadataBuilder metadata = MetadataBuilder
				.createCustomMetadataFromOriginalCustomMetadata(this, metadataBuilder, this.code);
		metadatas.add(metadata);
		return metadata;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public MetadataSchemaBuilder setId(short id) {
		this.id = id;
		return this;
	}
}
