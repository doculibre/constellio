package com.constellio.model.services.schemas.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.InheritedMetadataBehaviors;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.CannotCreateMultivalueReferenceToPrincipalTaxonomy;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.EssentialMetadataInSummaryCannotBeDisabled;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.InvalidAttribute;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.MetadataCannotBeUniqueAndMultivalue;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.InstanciationUtils;

public class MetadataBuilder {

	private static final String UNDERSCORE = "_";
	AllowedReferencesBuilder allowedReferencesBuilder;
	DataEntry dataEntry;
	private MetadataBuilder inheritance;
	private String localCode;
	private String collection;
	private String code;
	private String label;
	private Boolean enabled;
	private MetadataValueType type;
	private boolean undeletable = false;
	private boolean multivalue = false;
	private boolean systemReserved = false;
	private boolean unmodifiable = false;
	private boolean uniqueValue = false;
	private boolean childOfRelationship = false;
	private boolean taxonomyRelationship = false;
	private boolean searchable = false;
	private boolean schemaAutocomplete = false;
	private boolean sortable = false;
	private boolean encrypted = false;
	private boolean essentialInSummary = false;
	private Boolean defaultRequirement;
	private Boolean essential = false;
	private ClassListBuilder<RecordMetadataValidator<?>> recordMetadataValidators;
	private MetadataAccessRestrictionBuilder accessRestrictionBuilder;
	private Class<? extends StructureFactory> structureFactoryClass;
	private Class<? extends Enum<?>> enumClass;
	private Metadata originalMetadata;
	private Object defaultValue = null;
	private MetadataPopulateConfigsBuilder populateConfigsBuilder;
	private ClassProvider classProvider;

	MetadataBuilder() {
	}

	static MetadataBuilder createCustomMetadataFromDefault(MetadataBuilder defaultMetadata, String codeSchema) {
		MetadataBuilder builder = new MetadataBuilder();
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

		return builder;
	}

	private static Object copy(Object value) {
		if (value instanceof List) {
			return new ArrayList<Object>((List) value);
		}
		return value;
	}

	static MetadataBuilder createMetadataWithoutInheritance(String localCode, MetadataSchemaBuilder schemaBuilder) {
		MetadataBuilder builder = new MetadataBuilder();
		builder.classProvider = schemaBuilder.getClassProvider();
		builder.setCollection(schemaBuilder.getCollection());
		builder.setLocalCode(localCode);
		builder.setLabel(localCode);
		builder.setEnabled(true);
		builder.setDefaultRequirement(false);
		builder.setCode(schemaBuilder.getCode() + UNDERSCORE + localCode);
		builder.recordMetadataValidators = new ClassListBuilder<>(builder.classProvider, RecordMetadataValidator.class);
		builder.accessRestrictionBuilder = MetadataAccessRestrictionBuilder.create();
		builder.populateConfigsBuilder = MetadataPopulateConfigsBuilder.create();
		return builder;
	}

	static MetadataBuilder modifyMetadataWithoutInheritance(Metadata defaultMetadata, ClassProvider classProvider) {
		MetadataBuilder builder = new MetadataBuilder();
		builder.classProvider = classProvider;
		setBuilderPropertiesOfMetadataWithoutInheritance(defaultMetadata, builder);
		return builder;
	}

	static MetadataBuilder modifyMetadataWithInheritance(Metadata metadata, MetadataBuilder defaultMetadata) {
		MetadataBuilder builder = new MetadataBuilder();
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
		builder.label = metadata.getLabel();
		builder.enabled = metadata.isEnabled();
		builder.type = metadata.getType();
		builder.undeletable = metadata.isUndeletable();
		builder.defaultRequirement = metadata.isDefaultRequirement();
		builder.multivalue = metadata.isMultivalue();
		builder.searchable = metadata.isSearchable();
		builder.sortable = metadata.isSortable();
		builder.schemaAutocomplete = metadata.isSchemaAutocomplete();
		builder.unmodifiable = metadata.isUnmodifiable();
		builder.uniqueValue = metadata.isUniqueValue();
		builder.systemReserved = metadata.isSystemReserved();
		builder.encrypted = metadata.isEncrypted();
		builder.essential = metadata.isEssential();
		builder.essentialInSummary = metadata.isEssentialInSummary();
		builder.childOfRelationship = metadata.isChildOfRelationship();
		builder.taxonomyRelationship = metadata.isTaxonomyRelationship();
		builder.defaultValue = metadata.getDefaultValue();
		builder.dataEntry = metadata.getDataEntry();
		builder.recordMetadataValidators = new ClassListBuilder<RecordMetadataValidator<?>>(builder.classProvider,
				RecordMetadataValidator.class, metadata.getValidators());
		builder.accessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadata.getAccessRestrictions());
		if (metadata.getStructureFactory() != null) {
			builder.structureFactoryClass = (Class) metadata.getStructureFactory().getClass();
		}
		builder.enumClass = metadata.getEnumClass();
		if (metadata.getAllowedReferences() != null) {
			builder.allowedReferencesBuilder = new AllowedReferencesBuilder(metadata.getAllowedReferences());
		}
		builder.populateConfigsBuilder = MetadataPopulateConfigsBuilder.modify(metadata.getPopulateConfigs());
	}

	@SuppressWarnings("unchecked")
	private static void setBuilderPropertiesOfMetadataWithInheritance(Metadata metadata, MetadataBuilder inheritanceMetadata,
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
		builder.sortable = metadata.isSortable();
		builder.schemaAutocomplete = metadata.isSchemaAutocomplete();
		builder.unmodifiable = metadata.isUnmodifiable();
		builder.encrypted = metadata.isEncrypted();
		builder.uniqueValue = metadata.isUniqueValue();
		builder.systemReserved = metadata.isSystemReserved();
		builder.essential = metadata.isEssential();
		builder.essentialInSummary = metadata.isEssentialInSummary();
		builder.childOfRelationship = metadata.isChildOfRelationship();
		builder.taxonomyRelationship = metadata.isTaxonomyRelationship();
		builder.recordMetadataValidators = new ClassListBuilder<RecordMetadataValidator<?>>(
				builder.classProvider, RecordMetadataValidator.class, metadata.getValidators());
		builder.accessRestrictionBuilder = null;
		builder.defaultValue = metadata.getDefaultValue();
		for (String validatorClassName : inheritanceMetadata.recordMetadataValidators.implementationsClassname) {
			builder.recordMetadataValidators.remove(validatorClassName);
		}
		if (inheritanceMetadata.getLabel() != null && !inheritanceMetadata.getLabel().equals(metadata.getLabel())) {
			builder.label = metadata.getLabel();
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

	public String getLabel() {
		return label;
	}

	public MetadataBuilder setLabel(String label) {
		this.label = label;
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

	public boolean isSortable() {
		return inheritance == null ? sortable : inheritance.isSortable();
	}

	public MetadataBuilder setSortable(boolean sortable) {
		ensureCanModify("sortable");
		this.sortable = sortable;
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

	public boolean isEncrypted() {
		return inheritance == null ? encrypted : inheritance.encrypted;
	}

	public MetadataBuilder setEncrypted(boolean encrypted) {
		ensureCanModify("encrypted");
		this.encrypted = encrypted;
		return this;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public MetadataBuilder setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public MetadataPopulateConfigsBuilder getPopulateConfigsBuilder() {
		return populateConfigsBuilder;
	}

	public MetadataPopulateConfigsBuilder definePopulateConfigsBuilder(MetadataPopulateConfigsBuilder populateConfigsBuilder) {
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

		if (this.getLabel() == null || this.getLabel().equals(localCode)) {
			this.label = inheritance.getLabel();
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

		validateWithInheritance(inheritance, this);

		MetadataPopulateConfigs populateConfigs = this.populateConfigsBuilder.build();
		if (originalMetadata != null && this.inheritance != null && this.inheritance.originalMetadata != null && populateConfigs
				.equals(originalMetadata.getPopulateConfigs())) {
			if (originalMetadata.getPopulateConfigs().equals(this.inheritance.originalMetadata.getPopulateConfigs())) {
				populateConfigs = inheritance.getPopulateConfigs();
			}
		}

		return new Metadata(inheritance, this.getLabel(), this.getEnabled(), this.getDefaultRequirement(), this.code,
				this.recordMetadataValidators.build(), this.defaultValue, populateConfigs);
	}

	Metadata buildWithoutInheritance(DataStoreTypesFactory typesFactory, final ModelLayerFactory modelLayerFactory) {

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
				essential, encrypted, essentialInSummary);

		MetadataAccessRestriction accessRestriction = accessRestrictionBuilder.build();

		Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {
			@Override
			public EncryptionServices get() {
				return modelLayerFactory.newEncryptionServices();
			}
		};

		return new Metadata(localCode, this.getCode(), collection, this.getLabel(), this.getEnabled(), behaviors,
				this.type, references, this.getDefaultRequirement(), this.dataEntry, validators, dataStoreType,
				accessRestriction, structureFactory, enumClass, defaultValue, populateConfigsBuilder.build(),
				encryptionServicesFactory);
	}

	private void validateNotReferencingTaxonomy(String typeWithAllowedSchemas, TaxonomiesManager taxonomiesManager) {
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(collection);
		if (principalTaxonomy != null && principalTaxonomy.getSchemaTypes().contains(typeWithAllowedSchemas)) {
			throw new CannotCreateMultivalueReferenceToPrincipalTaxonomy(code);
		}
	}

	private String getDataStoreType(String metadataCode, DataStoreTypesFactory typesFactory, MetadataValueType type,
			boolean multivalue) {

		if (metadataCode.equals("id")) {
			return null;
		}

		String dataStoreType = null;
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
		default:
			throw new ImpossibleRuntimeException("Unsupported type : " + type);
		}
		return dataStoreType;
	}

	@Override
	public String toString() {
		return "MetadataBuilder [inheritance=" + inheritance + ", localCode=" + localCode + ", code=" + code + ", label="
				+ label + ", enabled=" + enabled + ", type=" + type + ", allowedReferencesBuilder=" + allowedReferencesBuilder
				+ ", undeletable=" + undeletable + ", defaultRequirement=" + defaultRequirement + ", dataEntry=" + dataEntry
				+ "]";
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
		if (builder.getLabel() == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "label");
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

		if (Boolean.FALSE == builder.getEnabled() && inheritance.isEssentialInSummary()) {
			throw new EssentialMetadataInSummaryCannotBeDisabled(code);
		}
	}

	private void validateWithoutInheritance(MetadataBuilder builder) {
		validateCode(builder.localCode);
		if (builder.code == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "code");
		}
		if (builder.getLabel() == null) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(builder.getCode(), "label");
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
		if (Boolean.FALSE == builder.getEnabled() && builder.isEssentialInSummary()) {
			throw new EssentialMetadataInSummaryCannotBeDisabled(code);
		}
	}

	private boolean isReferenceMetadataValid(MetadataBuilder builder) {
		if (builder.type == MetadataValueType.REFERENCE) {
			AllowedReferencesBuilder allowedReferencesBuilder = builder.allowedReferencesBuilder;
			if (allowedReferencesBuilder == null) {
				return false;
			} else
				return !((allowedReferencesBuilder.getSchemas() == null || allowedReferencesBuilder.getSchemas().isEmpty())
						&& builder.allowedReferencesBuilder.getSchemaType() == null);
		}
		return true;
	}

	void validateCode(String localCode) {
		if (SchemaUtils.isValidMetadataCodeWithCache(localCode)) {
			throw new MetadataBuilderRuntimeException.InvalidAttribute(this.getCode(), "code");
		}
	}

	public boolean isChildOfRelationship() {
		return inheritance == null ? childOfRelationship : inheritance.childOfRelationship;
	}

	public boolean isTaxonomyRelationship() {
		return inheritance == null ? taxonomyRelationship : inheritance.taxonomyRelationship;
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

	public ClassProvider getClassProvider() {
		return classProvider;
	}
}
