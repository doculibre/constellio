package com.constellio.model.services.schemas.builders;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.InheritedMetadataBehaviors;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.CannotCreateMultivalueReferenceToPrincipalTaxonomy;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.CannotHaveMeasurementUnitSpecifiedIfNotOfTypeIntegerOrNumber;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataInSummaryCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.InvalidAttribute;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.MetadataCannotBeUniqueAndMultivalue;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.InstanciationUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.constellio.model.entities.schemas.MetadataTransiency.PERSISTED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;

public class MetadataBuilder {

	private static final String UNDERSCORE = "_";
	AllowedReferencesBuilder allowedReferencesBuilder;
	DataEntry dataEntry;
	private MetadataBuilder inheritance;
	private String localCode;
	private String collection;
	private String code;
	private Map<Language, String> labels = new HashMap<>();
	private Boolean enabled;
	private MetadataValueType type;
	private boolean undeletable = false;
	private boolean multivalue = false;
	private boolean systemReserved = false;
	private boolean unmodifiable = false;
	private boolean uniqueValue = false;
	private boolean childOfRelationship = false;
	private boolean taxonomyRelationship = false;
	private boolean relationshipProvidingSecurity = false;
	private boolean searchable = false;
	private MetadataTransiency transiency = PERSISTED;
	private boolean schemaAutocomplete = false;
	private boolean sortable = false;
	private boolean encrypted = false;
	private boolean essentialInSummary = false;
	private boolean availableInSummary = false;
	private boolean multiLingual;
	private boolean markedForDeletion = false;
	private boolean increasedDependencyLevel = false;
	private Boolean defaultRequirement;
	private Boolean essential = false;
	private Boolean cacheIndex = false;
	private ClassListBuilder<RecordMetadataValidator<?>> recordMetadataValidators;
	private MetadataAccessRestrictionBuilder accessRestrictionBuilder;
	private Class<? extends StructureFactory> structureFactoryClass;
	private Class<? extends Enum<?>> enumClass;
	private Metadata originalMetadata;
	private Object defaultValue = null;
	private MetadataPopulateConfigsBuilder populateConfigsBuilder;
	private ClassProvider classProvider;
	private String inputMask;
	private Boolean duplicable;
	private Set<String> customAttributes;
	private MetadataSchemaBuilder schemaBuilder;
	private boolean dependencyOfAutomaticMetadata;
	private Map<String, Object> customParameter;
	private boolean fillEmptyLabelWithCode = true;
	private Integer maxLength = null;
	private String measurementUnit = null;
	short id;

	MetadataBuilder(MetadataSchemaBuilder schemaBuilder) {
		this.schemaBuilder = schemaBuilder;
	}

	static MetadataBuilder createCustomMetadataFromOriginalCustomMetadata(MetadataSchemaBuilder schemaBuilder,
																		  MetadataBuilder customMetadata,
																		  String codeSchema) {
		MetadataBuilder copy;

		if (customMetadata.getInheritance() == null) {
			copy = modifyMetadataWithoutInheritance(schemaBuilder, customMetadata.getOriginalMetadata(),
					customMetadata.getClassProvider());
		} else {
			MetadataBuilder inheritanceCopy = MetadataBuilder.modifyMetadataWithoutInheritance(
					schemaBuilder, customMetadata.getInheritance().getOriginalMetadata(), customMetadata.getClassProvider());
			copy = modifyMetadataWithInheritance(schemaBuilder, customMetadata.getOriginalMetadata(), inheritanceCopy);
		}
		copy.setCode(codeSchema + "_" + copy.getLocalCode());

		return copy;
	}

	public boolean isFillEmptyLabelWithCode() {
		return fillEmptyLabelWithCode;
	}

	public MetadataBuilder setFillEmptyLabelWithCode(boolean fillEmptyLabelWithCode) {
		this.fillEmptyLabelWithCode = fillEmptyLabelWithCode;
		return this;
	}

	public Map<String, Object> getCustomParameter() {
		return Collections.unmodifiableMap(customParameter);
	}

	public void setCustomParameter(Map<String, Object> customParameter) {
		this.customParameter = customParameter;
	}

	static MetadataBuilder createCustomMetadataFromDefault(MetadataSchemaBuilder schemaBuilder,
														   MetadataBuilder defaultMetadata,
														   String codeSchema) {
		MetadataBuilder builder = new MetadataBuilder(schemaBuilder);
		builder.classProvider = defaultMetadata.classProvider;
		builder.setLocalCode(defaultMetadata.localCode);
		builder.setCollection(defaultMetadata.collection);
		builder.setCode(defaultMetadata.getCode().replace(UNDERSCORE + "default" + UNDERSCORE,
				UNDERSCORE + codeSchema + UNDERSCORE));
		builder.setDefaultRequirement(null);
		builder.setEnabled(null);
		builder.inheritance = defaultMetadata;
		builder.type = defaultMetadata.getType();
		builder.dataEntry = defaultMetadata.dataEntry;
		builder.defaultValue = copy(defaultMetadata.defaultValue);
		builder.recordMetadataValidators = new ClassListBuilder<>(builder.classProvider, RecordMetadataValidator.class);
		builder.populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(defaultMetadata.getPopulateConfigsBuilder());
		builder.multiLingual = defaultMetadata.multiLingual;
		builder.customAttributes = defaultMetadata.customAttributes;
		builder.maxLength = defaultMetadata.maxLength;
		builder.measurementUnit = defaultMetadata.measurementUnit;

		return builder;
	}

	MetadataBuilder markAsDependencyOfAutomaticMetadata() {
		dependencyOfAutomaticMetadata = true;
		if (this.inheritance != null) {
			this.inheritance.dependencyOfAutomaticMetadata = true;
		}
		return this;
	}

	private static Object copy(Object value) {
		if (value instanceof List) {
			return new ArrayList<Object>((List) value);
		}
		return value;
	}

	static MetadataBuilder createMetadataWithoutInheritance(String localCode, MetadataSchemaBuilder schemaBuilder) {
		MetadataBuilder builder = new MetadataBuilder(schemaBuilder);
		builder.classProvider = schemaBuilder.getClassProvider();
		builder.setCollection(schemaBuilder.getCollection());
		builder.setLocalCode(localCode);
		for (Language language : schemaBuilder.getLabels().keySet()) {
			builder.addLabel(language, localCode);
		}
		builder.setEnabled(true);
		builder.setDefaultRequirement(false);
		builder.setCode(schemaBuilder.getCode() + UNDERSCORE + localCode);
		builder.recordMetadataValidators = new ClassListBuilder<>(builder.classProvider, RecordMetadataValidator.class);
		builder.accessRestrictionBuilder = MetadataAccessRestrictionBuilder.create();
		builder.populateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
		builder.setDuplicable(false);
		builder.customAttributes = new HashSet<>();
		builder.customParameter = new HashMap();

		return builder;
	}

	static MetadataBuilder modifyMetadataWithoutInheritance(MetadataSchemaBuilder schemaBuilder,
															Metadata defaultMetadata,
															ClassProvider classProvider) {
		MetadataBuilder builder = new MetadataBuilder(schemaBuilder);
		builder.classProvider = classProvider;
		setBuilderPropertiesOfMetadataWithoutInheritance(defaultMetadata, builder);
		return builder;
	}

	static MetadataBuilder modifyMetadataWithInheritance(MetadataSchemaBuilder schemaBuilder, Metadata metadata,
														 MetadataBuilder defaultMetadata) {
		MetadataBuilder builder = new MetadataBuilder(schemaBuilder);
		builder.inheritance = defaultMetadata;
		setBuilderPropertiesOfMetadataWithInheritance(metadata, defaultMetadata, builder);
		return builder;
	}

	@SuppressWarnings("unchecked")
	private static void setBuilderPropertiesOfMetadataWithoutInheritance(Metadata metadata, MetadataBuilder builder) {
		builder.setLocalCode(metadata.getLocalCode());
		builder.setCollection(metadata.getCollection());
		builder.setCode(metadata.getCode());
		builder.originalMetadata = metadata;
		builder.setLabels(metadata.getLabels());
		builder.enabled = metadata.isEnabled();
		builder.type = metadata.getType();
		builder.undeletable = metadata.isUndeletable();
		builder.defaultRequirement = metadata.isDefaultRequirement();
		builder.multivalue = metadata.isMultivalue();
		builder.searchable = metadata.isSearchable();
		builder.transiency = metadata.getTransiency();
		builder.sortable = metadata.isSortable();
		builder.schemaAutocomplete = metadata.isSchemaAutocomplete();
		builder.unmodifiable = metadata.isUnmodifiable();
		builder.uniqueValue = metadata.isUniqueValue();
		builder.systemReserved = metadata.isSystemReserved();
		builder.encrypted = metadata.isEncrypted();
		builder.essential = metadata.isEssential();
		builder.essentialInSummary = metadata.isEssentialInSummary();
		builder.availableInSummary = metadata.isAvailableInSummary();
		builder.childOfRelationship = metadata.isChildOfRelationship();
		builder.taxonomyRelationship = metadata.isTaxonomyRelationship();
		builder.relationshipProvidingSecurity = metadata.isRelationshipProvidingSecurity();
		builder.defaultValue = metadata.getDefaultValue();
		builder.inputMask = metadata.getInputMask();
		builder.markedForDeletion = metadata.isMarkedForDeletion();
		builder.dataEntry = metadata.getDataEntry();
		builder.recordMetadataValidators = new ClassListBuilder<RecordMetadataValidator<?>>(builder.classProvider,
				RecordMetadataValidator.class, metadata.getValidators());
		builder.accessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadata.getAccessRestrictions());
		builder.multiLingual = metadata.isMultiLingual();
		if (metadata.getStructureFactory() != null) {
			builder.structureFactoryClass = (Class) metadata.getStructureFactory().getClass();
		}
		builder.enumClass = metadata.getEnumClass();
		if (metadata.getAllowedReferences() != null) {
			builder.allowedReferencesBuilder = new AllowedReferencesBuilder(metadata.getAllowedReferences());
		}
		builder.populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(metadata.getPopulateConfigs());
		builder.duplicable = metadata.isDuplicable();
		builder.increasedDependencyLevel = metadata.isIncreasedDependencyLevel();
		builder.customAttributes = new HashSet<>(metadata.getCustomAttributes());
		builder.customParameter = new HashMap<>(metadata.getCustomParameter());
		builder.cacheIndex = metadata.isCacheIndex();
		builder.maxLength = metadata.getMaxLength();
		builder.measurementUnit = metadata.getMeasurementUnit();
		builder.id = metadata.getId();
	}

	@SuppressWarnings("unchecked")
	private static void setBuilderPropertiesOfMetadataWithInheritance(Metadata metadata,
																	  MetadataBuilder inheritanceMetadata,
																	  MetadataBuilder builder) {
		builder.classProvider = inheritanceMetadata.classProvider;
		builder.originalMetadata = metadata;
		builder.localCode = metadata.getLocalCode();
		builder.code = metadata.getCode();
		builder.collection = metadata.getCollection();
		builder.type = metadata.getType();
		builder.defaultRequirement = metadata.isDefaultRequirement();
		builder.undeletable = metadata.isUndeletable();
		builder.multivalue = metadata.isMultivalue();
		builder.searchable = metadata.isSearchable();
		builder.transiency = metadata.getTransiency();
		builder.sortable = metadata.isSortable();
		builder.schemaAutocomplete = metadata.isSchemaAutocomplete();
		builder.unmodifiable = metadata.isUnmodifiable();
		builder.encrypted = metadata.isEncrypted();
		builder.uniqueValue = metadata.isUniqueValue();
		builder.systemReserved = metadata.isSystemReserved();
		builder.essential = metadata.isEssential();
		builder.cacheIndex = metadata.isCacheIndex();
		builder.essentialInSummary = metadata.isEssentialInSummary();
		builder.availableInSummary = metadata.isAvailableInSummary();
		builder.childOfRelationship = metadata.isChildOfRelationship();
		builder.taxonomyRelationship = metadata.isTaxonomyRelationship();
		builder.maxLength = metadata.getMaxLength();
		builder.measurementUnit = metadata.getMeasurementUnit();
		builder.relationshipProvidingSecurity = metadata.isRelationshipProvidingSecurity();
		builder.markedForDeletion = metadata.isMarkedForDeletion();
		builder.recordMetadataValidators = new ClassListBuilder<RecordMetadataValidator<?>>(
				builder.classProvider, RecordMetadataValidator.class, metadata.getValidators());
		builder.accessRestrictionBuilder = null;
		builder.multiLingual = metadata.isMultiLingual();
		builder.customAttributes = inheritanceMetadata.customAttributes;
		builder.increasedDependencyLevel = metadata.isIncreasedDependencyLevel();
		builder.id = metadata.getId();

		for (String validatorClassName : inheritanceMetadata.recordMetadataValidators.implementationsClassname) {
			builder.recordMetadataValidators.remove(validatorClassName);
		}
		if (inheritanceMetadata.getLabels() != null && !inheritanceMetadata.getLabels().isEmpty()) {
			for (Language language : inheritanceMetadata.getLabels().keySet()) {
				if (!inheritanceMetadata.getLabel(language).equals(metadata.getLabel(language))) {
					builder.addLabel(language, metadata.getLabel(language));
				}
			}
		}
		if (metadata.getInputMask() != null && !metadata.getInputMask().equals(inheritanceMetadata.getInputMask())) {
			builder.inputMask = metadata.getInputMask();
		}
		if (metadata.getDefaultValue() != null && !metadata.getDefaultValue().equals(inheritanceMetadata.getDefaultValue())) {
			builder.defaultValue = metadata.getDefaultValue();
		}
		if (inheritanceMetadata.getEnabled() != null && !inheritanceMetadata.getEnabled().equals(metadata.isEnabled())) {
			builder.enabled = metadata.isEnabled();
		}
		if (metadata.getStructureFactory() != null) {
			builder.structureFactoryClass = (Class) metadata.getStructureFactory().getClass();
		}
		builder.enumClass = metadata.getEnumClass();
		if (inheritanceMetadata.getDefaultRequirement() != null
			&& inheritanceMetadata.getDefaultRequirement().equals(metadata.isDefaultRequirement())) {
			builder.setDefaultRequirement(null);
		}
		builder.populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(metadata.getPopulateConfigs());
		if (inheritanceMetadata.isDuplicable() != null && !inheritanceMetadata.isDuplicable().equals(metadata.isDuplicable())) {
			builder.duplicable = metadata.isDuplicable();
		}

		builder.customParameter = metadata.getCustomParameter();

	}

	public MetadataBuilder getInheritance() {
		return inheritance;
	}

	public String getLocalCode() {
		return localCode;
	}

	private MetadataBuilder setLocalCode(String localCode) {
		ensureCanModify("localCode");
		this.localCode = localCode;
		return this;
	}


	public MetadataBuilder setId(short id) {
		ensureCanModify("id");
		this.id = id;
		return this;
	}

	public String getCollection() {
		return collection;
	}

	private MetadataBuilder setCollection(String collection) {
		ensureCanModify("collection");
		this.collection = collection;
		return this;
	}

	public String getCode() {
		return code;
	}

	private MetadataBuilder setCode(String code) {
		this.code = code;
		return this;
	}

	public String getLabel(Language language) {
		return labels.get(language);
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public MetadataBuilder setLabels(Map<Language, String> labels) {

		if (labels == null) {
			this.labels = new HashMap<>();
		} else {
			this.labels = new HashMap<>(labels);
		}
		return this;
	}

	public MetadataBuilder addLabel(Language language, String label) {
		this.labels.put(language, label);
		return this;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public MetadataBuilder setEnabled(Boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Boolean isUndeletable() {
		return inheritance == null ? undeletable : inheritance.isUndeletable();
	}

	public MetadataBuilder setUndeletable(boolean undeletable) {
		ensureCanModify("undeletable");
		this.undeletable = undeletable;
		return this;
	}

	public boolean isMultivalue() {
		return inheritance == null ? multivalue : inheritance.isMultivalue();
	}

	public MetadataBuilder setMultivalue(boolean multivalue) {
		ensureCanModify("multivalue");
		if (multivalue && isUniqueValue()) {
			throw new MetadataCannotBeUniqueAndMultivalue(localCode);
		}
		this.multivalue = multivalue;
		return this;
	}

	public MetadataBuilder setCacheIndex(boolean cacheIndex) {
		ensureCanModify("cacheIndex");
		this.cacheIndex = cacheIndex;
		return this;
	}

	public boolean isCacheIndex() {
		return inheritance == null ? Boolean.TRUE.equals(cacheIndex) : inheritance.isCacheIndex();
	}

	public boolean isMultiLingual() {
		return inheritance == null ? multiLingual : inheritance.isMultiLingual();
	}


	public MetadataBuilder setMultiLingual(boolean multiLingual) {
		ensureCanModify("multiLingual");
		this.multiLingual = multiLingual;
		return this;
	}

	public boolean isSortable() {
		return inheritance == null ? sortable : inheritance.isSortable();
	}

	public MetadataBuilder setSortable(boolean sortable) {
		ensureCanModify("sortable");
		this.sortable = sortable;
		return this;
	}

	public boolean isIncreasedDependencyLevel() {
		return inheritance == null ? increasedDependencyLevel : inheritance.isIncreasedDependencyLevel();
	}

	public MetadataBuilder setIncreasedDependencyLevel(boolean increasedDependencyLevel) {
		ensureCanModify("increasedDependencyLevel");
		this.increasedDependencyLevel = increasedDependencyLevel;
		return this;
	}

	public boolean isSearchable() {
		return inheritance == null ? searchable : inheritance.isSearchable();
	}

	public MetadataBuilder setSearchable(boolean searchable) {
		ensureCanModify("searchable");
		this.searchable = searchable;
		return this;
	}

	public boolean isRelationshipProvidingSecurity() {
		return inheritance == null ? relationshipProvidingSecurity : inheritance.isRelationshipProvidingSecurity();
	}

	public MetadataBuilder setRelationshipProvidingSecurity(boolean relationshipProvidingSecurity) {
		this.relationshipProvidingSecurity = relationshipProvidingSecurity;
		return this;
	}

	public MetadataTransiency getTransiency() {
		return inheritance == null ? transiency : inheritance.getTransiency();
	}

	public MetadataBuilder setTransiency(MetadataTransiency transiency) {
		ensureCanModify("transiency");
		this.transiency = transiency;
		return this;
	}

	public boolean isSchemaAutocomplete() {
		return inheritance == null ? schemaAutocomplete : inheritance.isSchemaAutocomplete();
	}

	public MetadataBuilder setSchemaAutocomplete(boolean schemaAutocomplete) {
		ensureCanModify("autocomplete");
		this.schemaAutocomplete = schemaAutocomplete;
		return this;
	}

	public boolean isUniqueValue() {
		return inheritance == null ? uniqueValue : inheritance.uniqueValue;
	}

	public MetadataBuilder setUniqueValue(boolean uniqueValue) {
		ensureCanModify("uniqueValue");
		if (uniqueValue && isMultivalue()) {
			throw new MetadataCannotBeUniqueAndMultivalue(localCode);
		}
		this.uniqueValue = uniqueValue;
		return this;
	}

	public boolean isUnmodifiable() {
		return inheritance == null ? unmodifiable : inheritance.unmodifiable;
	}

	public MetadataBuilder setUnmodifiable(boolean unmodifiable) {
		ensureCanModify("unmodifiable");
		this.unmodifiable = unmodifiable;
		return this;
	}

	public boolean isSystemReserved() {
		return inheritance == null ? systemReserved : inheritance.systemReserved;
	}

	public MetadataBuilder setSystemReserved(boolean systemReserved) {
		ensureCanModify("systemReserved");
		this.systemReserved = systemReserved;
		if (systemReserved) {
			this.undeletable = true;
		}
		return this;
	}

	public boolean isEssential() {
		return inheritance == null ? essential : inheritance.essential;
	}

	public boolean isEssentialInSummary() {
		return inheritance == null ? essentialInSummary : inheritance.essentialInSummary;
	}

	public boolean isAvailableInSummary() {
		return inheritance == null ? availableInSummary : inheritance.availableInSummary;
	}


	public MetadataBuilder setEssential(boolean essential) {
		ensureCanModify("essential");
		this.essential = essential;
		if (essential) {
			this.undeletable = true;
		}
		return this;
	}

	public MetadataBuilder setEssentialInSummary(boolean essentialInSummary) {
		ensureCanModify("essentialInSummary");
		this.essentialInSummary = essentialInSummary;
		return this;
	}

	public MetadataBuilder setAvailableInSummary(boolean availableInSummary) {
		ensureCanModify("availableInSummary");
		this.availableInSummary = availableInSummary;
		return this;
	}

	public boolean isEncrypted() {
		return inheritance == null ? encrypted : inheritance.encrypted;
	}

	public MetadataBuilder setEncrypted(boolean encrypted) {
		ensureCanModify("encrypted");
		this.encrypted = encrypted;
		return this;
	}

	public boolean isMarkedForDeletion() {
		return inheritance == null ? markedForDeletion : inheritance.markedForDeletion;
	}

	public MetadataBuilder setMarkedForDeletion(boolean markedForDeletion) {
		ensureCanModify("markedForDeletion");
		if (markedForDeletion) {
			setEssentialInSummary(false);
			setAvailableInSummary(false);
			setEssential(false);
			setDefaultRequirement(false);
			setEnabled(false);
		}
		this.markedForDeletion = markedForDeletion;
		return this;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public MetadataBuilder setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public String getInputMask() {
		return inputMask;
	}

	public MetadataBuilder setInputMask(String inputMask) {
		this.inputMask = inputMask;
		return this;
	}

	public MetadataPopulateConfigsBuilder getPopulateConfigsBuilder() {
		return populateConfigsBuilder;
	}

	public MetadataPopulateConfigsBuilder definePopulateConfigsBuilder(
			MetadataPopulateConfigsBuilder populateConfigsBuilder) {
		this.populateConfigsBuilder = populateConfigsBuilder;
		return this.populateConfigsBuilder;
	}

	AllowedReferencesBuilder getAllowedReferencesBuider() {
		return inheritance == null ? allowedReferencesBuilder : inheritance.allowedReferencesBuilder;
	}

	public Boolean getDefaultRequirement() {
		return defaultRequirement;
	}

	public MetadataBuilder required() {
		return setDefaultRequirement(true);
	}

	public MetadataBuilder setDefaultRequirement(Boolean defaultRequirement) {
		this.defaultRequirement = defaultRequirement;
		return this;
	}

	public MetadataValueType getType() {
		return inheritance == null ? type : inheritance.type;
	}

	public MetadataBuilder setTypeWithoutValidation(MetadataValueType newType) {
		//Warning : Dangerous!
		this.type = newType;
		return this;
	}

	public MetadataBuilder setType(MetadataValueType newType) {
		ensureCanModify("type");
		if (this.type != null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(code, "type");
		}
		if (newType == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(code, "type");
		}

		this.type = newType;

		if (this.type == MetadataValueType.CONTENT) {
			this.structureFactoryClass = ContentFactory.class;
		}

		return this;
	}

	public Boolean isDuplicable() {
		return duplicable;
	}

	public MetadataBuilder setDuplicable(Boolean duplicable) {
		this.duplicable = duplicable;
		return this;
	}

	public AllowedReferencesBuilder defineReferences() {
		ensureCanModify("defineReferences");
		if (type == null) {
			type = MetadataValueType.REFERENCE;
		}

		if (type != MetadataValueType.REFERENCE) {
			throw new MetadataBuilderRuntimeException.AllowedReferencesOnlyUsableOnReferenceTypeMetadata(code);
		}
		if (allowedReferencesBuilder == null) {
			allowedReferencesBuilder = new AllowedReferencesBuilder();
		}
		return allowedReferencesBuilder;
	}

	public MetadataBuilder defineChildOfRelationshipToType(MetadataSchemaTypeBuilder schemaType) {
		setChildOfRelationship(true);
		return defineReferencesTo(schemaType);
	}

	public MetadataBuilder defineChildOfRelationshipToSchemas(MetadataSchemaBuilder... schemas) {
		return defineChildOfRelationshipToSchemas(Arrays.asList(schemas));
	}

	public MetadataBuilder defineChildOfRelationshipToSchemas(List<MetadataSchemaBuilder> schemas) {
		setChildOfRelationship(true);
		return defineReferencesTo(schemas);
	}

	public MetadataBuilder defineTaxonomyRelationshipToType(MetadataSchemaTypeBuilder schemaType) {
		setTaxonomyRelationship(true);
		return defineReferencesTo(schemaType);
	}

	public MetadataBuilder defineTaxonomyRelationshipToSchemas(MetadataSchemaBuilder... schemas) {
		return defineTaxonomyRelationshipToSchemas(Arrays.asList(schemas));
	}

	public MetadataBuilder defineTaxonomyRelationshipToSchemas(List<MetadataSchemaBuilder> schemas) {
		setTaxonomyRelationship(true);
		return defineReferencesTo(schemas);
	}

	public MetadataBuilder defineReferencesTo(MetadataSchemaTypeBuilder schemaType) {
		defineReferences().setCompleteSchemaTypeCode(schemaType.getCode());
		return this;
	}

	public MetadataBuilder removeOldReferences() {
		defineReferences().removeOldReferences();
		return this;
	}

	public MetadataBuilder defineReferencesTo(MetadataSchemaBuilder... schemas) {
		return defineReferencesTo(Arrays.asList(schemas));
	}

	public MetadataBuilder defineReferencesTo(List<MetadataSchemaBuilder> schemas) {
		for (MetadataSchemaBuilder schema : schemas) {
			defineReferences().getSchemas().add(schema.getCode());
		}
		return this;
	}

	public DataEntry getDataEntry() {
		return inheritance == null ? dataEntry : inheritance.dataEntry;
	}

	Metadata buildWithInheritance(Metadata inheritance) {

		if (this.getLabels() == null || this.getLabels().isEmpty()) {
			this.setLabels(inheritance.getLabels());
		} else {
			for (Language language : inheritance.getLabels().keySet()) {
				if ((this.getLabel(language) == null || this.getLabel(language).equals(localCode))) {
					addLabel(language, inheritance.getLabel(language));
				}
			}
		}
		if (this.getEnabled() == null) {
			this.enabled = inheritance.isEnabled();
		}
		if (this.getDefaultRequirement() == null) {
			this.defaultRequirement = inheritance.isDefaultRequirement();
		}
		if (!this.getPopulateConfigsBuilder().isEmpty()) {
			this.populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(this.getPopulateConfigsBuilder());
		} else {
			this.populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(inheritance.getPopulateConfigs());
		}

		if (inputMask == null) {
			this.inputMask = inheritance.getInputMask();
		}

		if (this.defaultValue == null) {
			this.defaultValue = inheritance.getDefaultValue();
		}

		validateWithInheritance(inheritance, this);

		MetadataPopulateConfigs populateConfigs = this.populateConfigsBuilder.build();
		if (originalMetadata != null && this.inheritance != null && this.inheritance.originalMetadata != null && populateConfigs
				.equals(originalMetadata.getPopulateConfigs())) {
			if (originalMetadata.getPopulateConfigs().equals(this.inheritance.originalMetadata.getPopulateConfigs())) {
				populateConfigs = inheritance.getPopulateConfigs();
			}
		}

		if (duplicable == null) {
			duplicable = inheritance.isDuplicable();
		}

		if (customParameter == null) {
			customParameter = new HashMap<>();
		}

		return new Metadata(inheritance, this.getLabels(), this.getEnabled(), this.getDefaultRequirement(), this.code,
				this.recordMetadataValidators.build(), this.defaultValue, this.inputMask, populateConfigs, duplicable, customParameter);
	}

	Metadata buildWithoutInheritance(DataStoreTypesFactory typesFactory, MetadataSchemaTypeBuilder typeBuilder,
									 short typeId, final ModelLayerFactory modelLayerFactory) {

		AllowedReferences references = allowedReferencesBuilder == null ? null : allowedReferencesBuilder.build();
		Set<RecordMetadataValidator<?>> validators = this.recordMetadataValidators.build();

		if (enabled == null) {
			enabled = true;
		}

		if (defaultRequirement == null) {
			defaultRequirement = false;
		}

		if (this.dataEntry == null) {
			this.dataEntry = new ManualDataEntry();
		}

		if (references != null && taxonomyRelationship && multivalue) {
			TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
			validateNotReferencingTaxonomy(references.getTypeWithAllowedSchemas(), taxonomiesManager);
		}

		validateWithoutInheritance(this);

		String dataStoreType = getDataStoreType(this.getLocalCode(), typesFactory, this.type, this.multivalue);

		StructureFactory structureFactory = new InstanciationUtils()
				.instanciateWithoutExpectableExceptions(structureFactoryClass);
		InheritedMetadataBehaviors behaviors = new InheritedMetadataBehaviors(this.isUndeletable(), multivalue, systemReserved,
				unmodifiable, uniqueValue, childOfRelationship, taxonomyRelationship, sortable, searchable, schemaAutocomplete,
				essential, encrypted, essentialInSummary, availableInSummary, multiLingual, markedForDeletion, customAttributes,
				increasedDependencyLevel, relationshipProvidingSecurity, transiency, dependencyOfAutomaticMetadata, cacheIndex, maxLength, measurementUnit);

		MetadataAccessRestriction accessRestriction = accessRestrictionBuilder.build();

		final Factory<ModelLayerFactory> modelLayerFactoryFactory = modelLayerFactory.getModelLayerFactoryFactory();
		Factory<EncryptionServices> encryptionServicesFactory = new EncryptionServicesFactory(modelLayerFactoryFactory);

		if (duplicable == null) {
			duplicable = false;
		}

		if (Schemas.isGlobalMetadata(localCode)) {
			id = Schemas.getGlobalMetadata(localCode).getId();
		}

		if (id == 0) {
			id = typeBuilder.nextMetadataId();
		}


		return new Metadata(id, localCode, this.getCode(), typeBuilder.getCollectionInfo().getCollectionId(), collection
				, typeId, this.getLabels(), this.getEnabled(), behaviors,
				this.type, references, this.getDefaultRequirement(), this.dataEntry, validators, dataStoreType,
				accessRestriction, structureFactory, enumClass, defaultValue, inputMask, populateConfigsBuilder.build(),
				encryptionServicesFactory, duplicable, customParameter);
	}

	private void validateNotReferencingTaxonomy(String typeWithAllowedSchemas, TaxonomiesManager taxonomiesManager) {
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);
		if (principalTaxonomy != null && principalTaxonomy.getSchemaTypes().contains(typeWithAllowedSchemas)) {
			if (schemaBuilder.getSchemaTypeBuilder().isSecurity()) {
				throw new CannotCreateMultivalueReferenceToPrincipalTaxonomy(code);
			}
		}
	}

	private String getDataStoreType(String metadataCode, DataStoreTypesFactory typesFactory, MetadataValueType type,
									boolean multivalue) {

		if (metadataCode.equals("id")) {
			return null;
		}

		String dataStoreType;
		switch (type) {

			case BOOLEAN:
				dataStoreType = typesFactory.forBoolean(multivalue);
				break;
			case ENUM:
				dataStoreType = typesFactory.forString(multivalue);
				break;
			case CONTENT:
				dataStoreType = typesFactory.forString(multivalue);
				break;
			case STRUCTURE:
				dataStoreType = typesFactory.forString(multivalue);
				break;
			case DATE:
				dataStoreType = typesFactory.forDate(multivalue);
				break;
			case DATE_TIME:
				dataStoreType = typesFactory.forDateTime(multivalue);
				break;
			case NUMBER:
				dataStoreType = typesFactory.forDouble(multivalue);
				break;
			case REFERENCE:
				dataStoreType = typesFactory.forString(multivalue);
				break;
			case STRING:
				dataStoreType = typesFactory.forString(multivalue);
				break;
			case TEXT:
				dataStoreType = typesFactory.forText(multivalue);
				break;
			case INTEGER:
				dataStoreType = typesFactory.forInteger(multivalue);
				break;

			default:
				throw new ImpossibleRuntimeException("Unsupported type : " + type);
		}
		return dataStoreType;
	}

	@Override
	public String toString() {
		return "MetadataBuilder [inheritance=" + inheritance + ", localCode=" + localCode + ", code=" + code + ", enabled="
			   + enabled + ", type=" + type + ", allowedReferencesBuilder=" + allowedReferencesBuilder
			   + ", undeletable=" + undeletable + ", defaultRequirement=" + defaultRequirement + ", dataEntry=" + dataEntry
			   + ", duplicable=" + duplicable + "]";
	}

	public MetadataBuilder addValidator(Class<? extends RecordMetadataValidator> clazz) {
		defineValidators().add(clazz);
		return this;
	}

	public ClassListBuilder<RecordMetadataValidator<?>> defineValidators() {
		return recordMetadataValidators;
	}

	public DataEntryBuilder defineDataEntry() {
		ensureCanModify("data entry");
		//		if (dataEntry != null) {
		//			throw new MetadataBuilderRuntimeException.InvalidAttribute(this.localCode, "data entry already defined");
		//		}
		return new DataEntryBuilder(this);
	}

	private void ensureCanModify(String attribute) {
		if (this.inheritance != null) {
			throw new MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata(this.getLocalCode(),
					attribute);
		}
	}

	private void validateWithInheritance(Metadata inheritance, MetadataBuilder builder) {
		if (inheritance == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "inheritance");
		}
		validateCode(inheritance.getLocalCode());
		if (builder.getLabels() == null || builder.getLabels().isEmpty()) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "label");
		} else {
			for (Entry<Language, String> entry : builder.getLabels().entrySet()) {
				if (StringUtils.isBlank(entry.getValue())) {
					throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "label");
				}
			}
		}
		if (builder.getEnabled() == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "enabled");
		}
		if (builder.getDefaultRequirement() == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "defaultRequirement");
		}
		if (Boolean.FALSE == builder.getEnabled() && inheritance.isEssential()) {
			throw new EssentialMetadataCannotBeDisabled(code);
		}

		if (Boolean.FALSE == builder.getEnabled() && inheritance.isEssentialInSummary() && !localCode.startsWith("USR")) {
			throw new EssentialMetadataInSummaryCannotBeDisabled(code);
		}
	}

	private void validateWithoutInheritance(MetadataBuilder builder) {
		validateCode(builder.localCode);
		if (builder.code == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "code");
		}
		if ((builder.getLabels() == null || builder.getLabels().isEmpty()) && fillEmptyLabelWithCode) {
			//FIXME
			builder.addLabel(Language.French, builder.getCode());
			//			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "label");
		}

		if (builder.getEnabled() == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "enabled");
		}
		if (builder.type == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "type");
		}
		if (!isReferenceMetadataValid(builder)) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "allowedReferences");
		}
		if (builder.dataEntry == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "dataEntry");
		}
		if (builder.defaultRequirement == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "defaultRequirement");
		}

		if (builder.getType().equals(MetadataValueType.REFERENCE)) {
			builder.defineReferences().getSchemaType();
		}

		if (Boolean.FALSE == builder.getEnabled() && builder.isEssential()) {
			throw new EssentialMetadataCannotBeDisabled(code);
		}
		if (Boolean.FALSE == builder.getEnabled() && builder.isEssentialInSummary() && !builder.localCode.startsWith("USR")) {
			throw new EssentialMetadataInSummaryCannotBeDisabled(code);
		}

		if ((transiency != null && transiency != PERSISTED) && builder.getDataEntry().getType() == MANUAL) {
			throw new MetadataBuilderRuntimeException.MetadataEnteredManuallyCannotBeTransient(code);
		}

		if ((transiency != null && transiency != PERSISTED) && builder.getType() == MetadataValueType.REFERENCE) {
			throw new MetadataBuilderRuntimeException.ReferenceCannotBeTransient(code);
		}

		if (maxLength != null
			&& !builder.getType().equals(MetadataValueType.TEXT)
			&& !builder.getType().equals(MetadataValueType.STRING)) {
			throw new MetadataBuilderRuntimeException.CannotHaveMaxLengthSpecifiedIfNotOfTypeStringOrText(code);
		}

		if (StringUtils.isNotBlank(measurementUnit)
			&& !builder.getType().equals(MetadataValueType.INTEGER)
			&& !builder.getType().equals(MetadataValueType.NUMBER)) {
			throw new CannotHaveMeasurementUnitSpecifiedIfNotOfTypeIntegerOrNumber(code);
		}
	}

	private boolean isReferenceMetadataValid(MetadataBuilder builder) {
		if (builder.type == MetadataValueType.REFERENCE) {
			AllowedReferencesBuilder allowedReferencesBuilder = builder.allowedReferencesBuilder;
			if (allowedReferencesBuilder == null) {
				return false;
			} else {
				return !((allowedReferencesBuilder.getSchemas() == null || allowedReferencesBuilder.getSchemas().isEmpty())
						 && builder.allowedReferencesBuilder.getSchemaType() == null);
			}
		}
		return true;
	}

	void validateCode(String localCode) {
		if (SchemaUtils.isInValidMetadataCodeWithCache(localCode)) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(this.getCode(), "code");
		}
	}

	public boolean isChildOfRelationship() {
		return inheritance == null ? childOfRelationship : inheritance.childOfRelationship;
	}

	public boolean isTaxonomyRelationship() {
		return inheritance == null ? taxonomyRelationship : inheritance.taxonomyRelationship;
	}

	public Integer getMaxLength() {
		return inheritance == null ? maxLength : inheritance.maxLength;
	}

	public MetadataBuilder setMaxLength(Integer maxLength) {
		ensureCanModify("maxLength");
		this.maxLength = maxLength;
		return this;
	}

	public String getMeasurementUnit() {
		return inheritance == null ? measurementUnit : inheritance.measurementUnit;
	}

	public MetadataBuilder setMeasurementUnit(String measurementUnit) {
		ensureCanModify("measurementUnit");
		if (measurementUnit != null) {
			this.measurementUnit = !measurementUnit.equals("") ? measurementUnit : null;
		} else {
			this.measurementUnit = null;
		}
		return this;
	}

	public MetadataBuilder setChildOfRelationship(boolean childOfRelationship) {
		ensureCanModify("childOfRelationship");

		this.childOfRelationship = childOfRelationship;
		return this;
	}

	public MetadataBuilder setTaxonomyRelationship(boolean taxonomyRelationship) {
		ensureCanModify("taxonomyRelationship");

		this.taxonomyRelationship = taxonomyRelationship;
		return this;
	}

	public MetadataAccessRestrictionBuilder defineAccessRestrictions() {
		return accessRestrictionBuilder;
	}

	public MetadataBuilder setAccessRestrictionBuilder(
			MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder) {
		accessRestrictionBuilder = metadataAccessRestrictionBuilder;
		return this;
	}

	public MetadataBuilder defineStructureFactory(Class<? extends StructureFactory> structureFactory) {
		this.structureFactoryClass = structureFactory;
		if (this.type == null) {
			this.type = MetadataValueType.STRUCTURE;
		}
		return this;
	}

	public Class<? extends StructureFactory> getStructureFactory() {
		return structureFactoryClass;
	}

	public boolean hasDefinedReferences() {
		return allowedReferencesBuilder != null;
	}

	public MetadataBuilder defineAsEnum(Class<? extends Enum<?>> enumClass) {

		if (enumClass == null) {
			throw new InvalidAttribute(code, "enumClass");
		}
		if (!EnumWithSmallCode.class.isAssignableFrom(enumClass)) {
			throw new MetadataBuilderRuntimeException.EnumClassMustImplementEnumWithSmallCode(enumClass);
		}

		this.enumClass = enumClass;
		this.type = MetadataValueType.ENUM;
		return this;
	}

	public Class<? extends Enum<?>> getEnumClass() {
		return enumClass;
	}

	public AllowedReferencesBuilder getAllowedReferencesBuilder() {
		return allowedReferencesBuilder;
	}

	public Metadata getOriginalMetadata() {
		return originalMetadata;
	}

	public void setAllowedReferenceBuilder(AllowedReferencesBuilder allowedReferencesBuilder) {
		this.allowedReferencesBuilder = allowedReferencesBuilder;
	}

	public boolean hasFlag(String flag) {
		return customAttributes.contains(flag);
	}

	public MetadataBuilder setCustomAttributes(Set<String> customAttributes) {
		this.customAttributes = customAttributes;
		return this;
	}

	public MetadataBuilder addCustomAttribute(String customAttribute) {
		if (customAttribute.contains(",")) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute("Custom Attribute", customAttribute);
		}

		customAttributes.add(customAttribute);
		return this;
	}

	public MetadataBuilder removeCustomAttribute(String customAttribute) {
		customAttributes.remove(customAttribute);
		return this;
	}

	public ClassProvider getClassProvider() {
		return classProvider;
	}

	public Set<String> getCustomAttributes() {
		return Collections.unmodifiableSet(customAttributes);
	}

	public boolean hasValidator(Class<?> validatorClass) {
		return defineValidators().contains(validatorClass);
	}

	public short getId() {
		return id;
	}

	public boolean isRequiringCacheReload() {
		boolean requiringCacheReload = false;
		if (originalMetadata != null) {
			requiringCacheReload = (essentialInSummary && !originalMetadata.isEssentialInSummary()) ||
								   (availableInSummary && !originalMetadata.isAvailableInSummary());
		}
		return requiringCacheReload;
	}

	public void moveToDefaultSchemas() {
		schemaBuilder.deleteMetadataWithoutValidation(localCode);

		//TODO Étienne, copier tous les champs
		schemaBuilder.getDefaultSchema().create(localCode).setId(id).setType(type).setMultivalue(multivalue).setLabels(labels);
		schemaBuilder.getDefaultSchema().get(localCode).setEnabled(false);
		schemaBuilder.get(localCode).setEnabled(true);
	}


	private static class EncryptionServicesFactory implements Factory<EncryptionServices> {

		private final Factory<ModelLayerFactory> modelLayerFactoryFactory;

		private EncryptionServicesFactory(Factory<ModelLayerFactory> modelLayerFactoryFactory) {
			this.modelLayerFactoryFactory = modelLayerFactoryFactory;
		}

		@Override
		public EncryptionServices get() {
			return modelLayerFactoryFactory.get().newEncryptionServices();
		}

	}

	public boolean isDependencyOfAutomaticMetadata() {
		return dependencyOfAutomaticMetadata;
	}
}
