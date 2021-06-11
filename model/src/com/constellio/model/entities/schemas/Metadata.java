package com.constellio.model.entities.schemas;

import com.constellio.data.dao.services.solr.SolrDataStoreTypesUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.sort.DefaultStringSortFieldNormalizer;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.schemas.MetadataTransiency.PERSISTED;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.STRUCTURE;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.schemas.builders.ClassListBuilder.combine;

public class Metadata implements DataStoreField {

	final short id;

	final Metadata inheritance;

	final String collection;

	final Byte collectionId;

	final Short typeId;

	final String localCode;

	final String code;

	final Map<Language, String> labels;

	final String dataStoreType;

	final boolean enabled;

	final MetadataValueType type;

	final AllowedReferences allowedReferences;

	final boolean defaultRequirement;

	final DataEntry dataEntry;

	final InheritedMetadataBehaviors inheritedMetadataBehaviors;

	final Set<RecordMetadataValidator<?>> recordMetadataValidators;

	final MetadataAccessRestriction accessRestriction;

	final StructureFactory structureFactory;

	final Class<? extends Enum<?>> enumClass;

	final Object defaultValue;

	final MetadataPopulateConfigs populateConfigs;

	final Factory<EncryptionServices> encryptionServicesFactory;

	final String inputMask;

	final boolean duplicable;

	final String mainLanguageDataStoreCode;
	final String inheritanceCode;

	final boolean global;

	final Map<String, Object> customParameter;

	final String schemaTypeCode;

	final boolean secured;

	MetadataSchema schema;
	MetadataSchemaType referencedSchemaType;

	Metadata(int id, String localCode, MetadataValueType type, boolean multivalue) {
		this(id, "global_default", localCode, type, multivalue, false);
	}

	Metadata(int id, String localCode, MetadataValueType type, boolean multivalue, boolean multiLingual) {
		this(id, "global_default", localCode, type, multivalue, multiLingual);
	}

	Metadata(int id, String schemaCode, String datastoreCode, MetadataValueType type, boolean multivalue,
			 boolean multiLingual) {
		this(id, schemaCode, datastoreCode, type, multivalue, multiLingual, null);
	}

	Metadata(int id, String schemaCode, String datastoreCode, MetadataValueType type, boolean multivalue,
			 boolean multiLingual, StructureFactory structureFactory) {
		this.id = (short) id;
		this.inheritance = null;

		this.enabled = false;
		this.collection = null;
		this.collectionId = null;
		this.typeId = null;
		this.type = type;
		this.allowedReferences = null;
		this.inheritedMetadataBehaviors = new InheritedMetadataBehaviors(false,
				multivalue, false, false, false, false, false,
				false, false, false, false, false, false,
				false, multiLingual, false, null, null,
				new HashSet<String>(), false, false, PERSISTED, false,
				false, null, null);
		this.defaultRequirement = false;
		this.dataEntry = null;
		this.encryptionServicesFactory = null;
		this.accessRestriction = new MetadataAccessRestriction();
		this.inputMask = null;

		if (datastoreCode.contains("_") && !datastoreCode.equals("_version_")) {
			int firstUnderscoreIndex = datastoreCode.indexOf("_");
			String codeWithoutId = datastoreCode.substring(0, firstUnderscoreIndex);
			this.dataStoreType = datastoreCode.substring(firstUnderscoreIndex + 1);
			if (codeWithoutId.endsWith("PId")) {
				codeWithoutId = codeWithoutId.substring(0, codeWithoutId.length() - 3);
			}
			if (codeWithoutId.endsWith("Id")) {
				codeWithoutId = codeWithoutId.substring(0, codeWithoutId.length() - 2);
			}

			this.localCode = codeWithoutId;
			this.code = schemaCode + "_" + this.localCode;
		} else {
			this.localCode = datastoreCode;
			this.dataStoreType = null;
			this.code = schemaCode + "_" + localCode;
		}
		this.labels = Collections.emptyMap();
		this.recordMetadataValidators = null;
		this.structureFactory = structureFactory;
		this.enumClass = null;
		this.defaultValue = multivalue ? Collections.emptyList() : null;
		this.populateConfigs = new MetadataPopulateConfigs();
		this.duplicable = false;
		this.mainLanguageDataStoreCode = computeMainLanguageDataStoreCode();
		this.inheritanceCode = computeInheritanceCode();
		this.global = computeIsGlobal();
		this.customParameter = Collections.unmodifiableMap(new HashMap<String, Object>());
		this.schemaTypeCode = new SchemaUtils().getSchemaTypeCode(this);
		this.secured = getAccessRestrictions() != null && getAccessRestrictions().getRequiredReadRoles() != null &&
					   !getAccessRestrictions().getRequiredReadRoles().isEmpty();

	}

	public void setBuiltSchema(MetadataSchema schema) {
		if (this.schema != null) {
			throw new IllegalStateException("Schematype already");
		}
		this.schema = schema;
	}

	public boolean isFilteredByAny(List<MetadataFilter> metadataFilterList) {
		for (MetadataFilter metadataFilter : metadataFilterList) {
			if (metadataFilter.isFiltered(this)) {
				return true;
			}
		}

		return false;
	}

	public boolean isSecured() {
		return secured;
	}

	public final String computeInheritanceCode() {
		if (getInheritance() == null) {
			return getCode();
		} else {
			String[] parts = SchemaUtils.underscoreSplitWithCache(getCode());
			return parts[0] + "_default_" + parts[2];
		}
	}

	private String computeMainLanguageDataStoreCode() {
		if (type == MetadataValueType.REFERENCE) {
			if (isChildOfRelationship()) {
				return dataStoreType == null ? localCode : (localCode + "PId_" + dataStoreType);
			} else {
				return dataStoreType == null ? localCode : (localCode + "Id_" + dataStoreType);
			}
		} else {
			return dataStoreType == null ? localCode : (localCode + "_" + dataStoreType);
		}
	}

	private String computeSecondaryLanguageDataStoreCode(String language) {
		if (type == MetadataValueType.REFERENCE) {
			return computeMainLanguageDataStoreCode();
		} else {
			return dataStoreType == null ? localCode : (localCode + "." + language + "_" + dataStoreType);
		}
	}

	public final boolean computeIsGlobal() {
		return Schemas.isGlobalMetadata(getLocalCode());
	}

	public Metadata(short id, String localCode, String code, byte collectionId, String collection, short typeId,
					Map<Language, String> labels,
					Boolean enabled,
					InheritedMetadataBehaviors inheritedMetadataBehaviors, MetadataValueType type,
					AllowedReferences allowedReferences, Boolean defaultRequirement, DataEntry dataEntry,
					Set<RecordMetadataValidator<?>> recordMetadataValidators, String dataStoreType,
					MetadataAccessRestriction accessRestriction, StructureFactory structureFactory,
					Class<? extends Enum<?>> enumClass,
					Object defaultValue, String inputMask, MetadataPopulateConfigs populateConfigs,
					Factory<EncryptionServices> encryptionServices, Boolean duplicatbale,
					Map<String, Object> customParameter) {
		super();
		this.id = id;
		this.inheritance = null;
		this.localCode = localCode;
		this.code = code;
		this.collectionId = collectionId;
		this.typeId = typeId;
		this.collection = collection;
		this.labels = Collections.unmodifiableMap(labels);
		this.enabled = enabled;
		this.type = type;
		this.allowedReferences = allowedReferences;
		this.inheritedMetadataBehaviors = inheritedMetadataBehaviors;
		this.defaultRequirement = defaultRequirement;
		this.dataEntry = dataEntry;
		this.dataStoreType = dataStoreType;
		this.recordMetadataValidators = recordMetadataValidators;
		this.accessRestriction = accessRestriction;
		this.structureFactory = structureFactory;
		this.enumClass = enumClass;
		this.defaultValue = defaultValue;
		this.inputMask = inputMask;
		this.populateConfigs = populateConfigs;
		this.encryptionServicesFactory = encryptionServices;
		this.duplicable = duplicatbale;
		this.mainLanguageDataStoreCode = computeMainLanguageDataStoreCode();
		this.inheritanceCode = computeInheritanceCode();
		this.global = computeIsGlobal();
		this.customParameter = Collections.unmodifiableMap(customParameter);
		this.schemaTypeCode = new SchemaUtils().getSchemaTypeCode(this);
		this.secured = getAccessRestrictions() != null && getAccessRestrictions().getRequiredReadRoles() != null &&
					   !getAccessRestrictions().getRequiredReadRoles().isEmpty();
	}

	public Metadata(Metadata inheritance, Map<Language, String> labels, boolean enabled,
					boolean defaultRequirement,
					String code,
					Set<RecordMetadataValidator<?>> recordMetadataValidators, Object defaultValue, String inputMask,
					MetadataPopulateConfigs populateConfigs, boolean duplicable, Map<String, Object> customParameter) {
		super();

		this.id = inheritance.getId();
		this.localCode = inheritance.getLocalCode();
		this.code = code;
		this.collection = inheritance.collection;
		this.collectionId = inheritance.collectionId;
		this.typeId = inheritance.typeId;
		this.inheritance = inheritance;
		this.labels = Collections.unmodifiableMap(labels);
		this.enabled = enabled;
		this.type = inheritance.getType();
		this.allowedReferences = inheritance.getAllowedReferences();
		this.inheritedMetadataBehaviors = inheritance.getInheritedMetadataBehaviors();
		this.defaultRequirement = defaultRequirement;
		this.dataEntry = inheritance.getDataEntry();
		this.dataStoreType = inheritance.getDataStoreType();
		this.accessRestriction = inheritance.getAccessRestrictions();
		this.recordMetadataValidators = combine(inheritance.recordMetadataValidators, recordMetadataValidators);
		this.populateConfigs = populateConfigs;
		this.structureFactory = inheritance.structureFactory;
		this.enumClass = inheritance.enumClass;
		this.defaultValue = defaultValue;
		this.inputMask = inputMask;
		this.encryptionServicesFactory = inheritance.encryptionServicesFactory;
		this.duplicable = duplicable;
		this.mainLanguageDataStoreCode = computeMainLanguageDataStoreCode();
		this.inheritanceCode = computeInheritanceCode();
		this.global = computeIsGlobal();
		this.customParameter = Collections.unmodifiableMap(customParameter);
		this.schemaTypeCode = inheritance.getSchemaTypeCode();
		this.secured = getAccessRestrictions() != null && getAccessRestrictions().getRequiredReadRoles() != null &&
					   !getAccessRestrictions().getRequiredReadRoles().isEmpty();
	}

	public Map<String, Object> getCustomParameter() {
		return customParameter;
	}

	public short getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getNoInheritanceCode() {
		return inheritance == null ? code : inheritance.getCode();
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getDataStoreCode() {
		return mainLanguageDataStoreCode;
	}

	public String getSecondaryLanguageDataStoreCode(String language) {
		return computeSecondaryLanguageDataStoreCode(language);
	}

	public String getFrenchLabel() {
		return getLabel(French);
	}

	public String getLabel(Language language) {
		return labels.get(language);
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public Map<String, String> getLabelsByLanguageCodes() {
		Map<String, String> labelsMap = new HashMap<>();
		for (Language language : getLabels().keySet()) {
			labelsMap.put(language.getCode(), getLabels().get(language));
		}
		return labelsMap;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean inheritDefaultSchema() {
		return inheritance != null;
	}

	public MetadataValueType getType() {
		return type;
	}

	public MetadataValueType getMarkedForMigrationToType() {
		return inheritedMetadataBehaviors.markedForMigrationToType;
	}

	public String getReferencedSchemaTypeCode() {
		return getAllowedReferences().getTypeWithAllowedSchemas();
	}

	public MetadataSchemaType getReferencedSchemaType() {
		if (referencedSchemaType == null) {
			referencedSchemaType = schema.getSchemaType().getSchemaTypes().getSchemaType(getReferencedSchemaTypeCode());
		}
		return referencedSchemaType;
	}

	public AllowedReferences getAllowedReferences() {
		return allowedReferences;
	}

	public InheritedMetadataBehaviors getInheritedMetadataBehaviors() {
		return inheritedMetadataBehaviors;
	}

	public Metadata getInheritance() {
		return inheritance;
	}

	public boolean isDefaultRequirement() {
		return defaultRequirement;
	}

	public DataEntry getDataEntry() {
		return dataEntry;
	}

	public String getDataStoreType() {
		return dataStoreType;
	}

	public boolean isMultivalue() {
		return getInheritedMetadataBehaviors().isMultivalue();
	}

	public Boolean isMarkedForMigrationToMultivalue() {
		return inheritedMetadataBehaviors.markedForMigrationToMultivalue;
	}

	public boolean isMultiLingual() {
		return getInheritedMetadataBehaviors().isMultiLingual();
	}

	public boolean isUndeletable() {
		return getInheritedMetadataBehaviors().isUndeletable();
	}

	public boolean isSystemReserved() {
		return getInheritedMetadataBehaviors().isSystemReserved();
	}

	public boolean isUnmodifiable() {
		return getInheritedMetadataBehaviors().isUnmodifiable();
	}

	public boolean isUniqueValue() {
		return getInheritedMetadataBehaviors().isUniqueValue();
	}

	public boolean isChildOfRelationship() {
		return getInheritedMetadataBehaviors().isChildOfRelationship();
	}

	public boolean isTaxonomyRelationship() {
		return getInheritedMetadataBehaviors().isTaxonomyRelationship();
	}

	public Integer getMaxLength() {
		return getInheritedMetadataBehaviors().getMaxLength();
	}

	public String getMeasurementUnit() {
		return getInheritedMetadataBehaviors().getMeasurementUnit();
	}

	public boolean isSearchable() {
		return getInheritedMetadataBehaviors().isSearchable();
	}

	public MetadataTransiency getTransiency() {
		return getInheritedMetadataBehaviors().getTransiency();
	}

	public boolean isSortable() {
		return getInheritedMetadataBehaviors().isSortable();
	}

	public boolean isEssential() {
		return getInheritedMetadataBehaviors().isEssential();
	}

	public boolean isCacheIndex() {
		return getInheritedMetadataBehaviors().isCacheIndex();
	}

	public boolean isEssentialInSummary() {
		return getInheritedMetadataBehaviors().isEssentialInSummary();
	}

	public boolean isAvailableInSummary() {
		return getInheritedMetadataBehaviors().isAvailableInSummary();
	}

	public boolean isEncrypted() {
		return getInheritedMetadataBehaviors().isEncrypted();
	}

	public boolean isSchemaAutocomplete() {
		return getInheritedMetadataBehaviors().isSchemaAutocomplete();
	}

	public boolean isRelationshipProvidingSecurity() {
		return getInheritedMetadataBehaviors().isRelationshipProvidingSecurity();
	}

	public Set<String> getCustomAttributes() {
		return getInheritedMetadataBehaviors().getCustomAttributes();
	}

	public boolean isIncreasedDependencyLevel() {
		return getInheritedMetadataBehaviors().isReverseDependency();
	}

	public boolean isDependencyOfAutomaticMetadata() {
		return getInheritedMetadataBehaviors().isDependencyOfAutomaticMetadata();
	}

	public StringSortFieldNormalizer getSortFieldNormalizer() {
		return hasNormalizedSortField() ? new DefaultStringSortFieldNormalizer() : null;
	}

	private Metadata cachedSortMetadata;

	public Metadata getSortMetadata() {
		if (cachedSortMetadata == null) {
			String dataStoreCode = this.getDataStoreCode().replace("_s", "_sort_s");
			String schemaCode = this.getCode().replace("_" + this.getLocalCode(), "");
			cachedSortMetadata = new Metadata(this.id, schemaCode, dataStoreCode, STRING, this.isMultivalue(),
					this.isMultiLingual());
		}
		return cachedSortMetadata;
	}

	public Metadata getTypeAndMultivalueMigrationMetadata() {
		if (isTypeAndMultivalueMigrationPossible()) {
			MetadataValueType correctedMigrateToType = this.getMarkedForMigrationToType() != null ?
													   this.getMarkedForMigrationToType() : this.getType();
			boolean correctedMigrateToMutlivalue = this.isMarkedForMigrationToMultivalue() != null ?
												   Boolean.TRUE.equals(this.isMarkedForMigrationToMultivalue()) : this.isMultivalue();

			String newDataStoreCodeExtension = SolrDataStoreTypesUtils.getTypeOrMutlivalueExtension(correctedMigrateToType.name(), correctedMigrateToMutlivalue);

			return new Metadata(id, localCode, getCode(), getCollectionId(), collection, typeId, getLabels(), enabled,
					inheritedMetadataBehaviors, type, allowedReferences, defaultRequirement, dataEntry, recordMetadataValidators,
					newDataStoreCodeExtension, accessRestriction, structureFactory, enumClass, defaultValue, inputMask, populateConfigs,
					encryptionServicesFactory, duplicable, customParameter);
		}

		return null;
	}

	private boolean isTypeAndMultivalueMigrationPossible() {
		return RecordUtils.isMetadataValueTypeMigrationSupported(this.getType(), this.getMarkedForMigrationToType())
			   && RecordUtils.isMetadataMultivalueMigrationSupported(this.isMultivalue(), this.isMarkedForMigrationToMultivalue());
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dataEntry", "structureFactory", "encryptionServicesFactory", "schema", "cachedSortMetadata");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dataEntry", "recordMetadataValidators", "structureFactory",
				"encryptionServicesFactory", "schema", "cachedSortMetadata");
	}

	@Override
	public String toString() {
		return localCode;
	}

	public Set<RecordMetadataValidator<?>> getValidators() {
		return recordMetadataValidators;
	}

	public String getCollection() {
		return collection;

	}

	public final boolean isLocalCode(String code) {
		return getLocalCode().equals(code);
	}

	public MetadataAccessRestriction getAccessRestrictions() {
		return accessRestriction;
	}

	public StructureFactory getStructureFactory() {
		return structureFactory;
	}

	public Class<? extends Enum<?>> getEnumClass() {
		return enumClass;
	}

	public static Metadata newDummyMetadata(short id, String schemaCode, String localCode, MetadataValueType type,
											boolean multivalue,
											boolean multiLingual) {
		return new Metadata(id, schemaCode, localCode, type, multivalue, multiLingual);
	}

	public static Metadata newGlobalMetadata(short id, String dataStoreCode, MetadataValueType type, boolean multivalue,
											 boolean multiLingual) {
		return new Metadata(id, "global_default", dataStoreCode, type, multivalue, multiLingual);
	}

	public Metadata getSearchableMetadataWithLanguage(String language) {
		String schemaCode = code.replace("_" + localCode, "");
		return new Metadata(id, schemaCode, getDataStoreCode().replace("_s", "_t") + "_" + language, type, isMultivalue(),
				isMultiLingual());
	}

	public boolean isSameLocalCode(Metadata metadata) {
		return metadata != null && localCode.equals(metadata.getLocalCode());
	}

	public boolean isSameLocalCodeThanAny(Metadata... metadatas) {
		for (Metadata metadata : metadatas) {
			if (localCode.equals(metadata.getLocalCode())) {
				return true;
			}
		}

		return false;
	}

	public boolean isSameLocalCodeIn(String... metadatasLocalCodes) {
		for (String metadataLocalCode : metadatasLocalCodes) {
			if (localCode.equals(metadataLocalCode)) {
				return true;
			}
		}

		return false;
	}

	public String getSchemaCode() {
		return new SchemaUtils().getSchemaCode(this);
	}

	public String getSchemaTypeCode() {
		return schemaTypeCode;
	}

	public MetadataPopulateConfigs getPopulateConfigs() {
		return populateConfigs;
	}

	public boolean hasCode(String otherCode) {
		return new SchemaUtils().hasSameTypeAndLocalCode(code, otherCode);
	}

	public Factory<EncryptionServices> getEncryptionServicesFactory() {
		return encryptionServicesFactory;
	}

	public Metadata getAnalyzedField(String languageCode) {
		return Schemas.getSearchableMetadata(this, languageCode);
	}

	public Metadata getSecondaryLanguageField(String languageCode) {
		return Schemas.getSecondaryLanguageMetadata(this, languageCode);
	}

	public boolean hasNormalizedSortField() {
		boolean globalMetadataWithNormalizedSortField =
				CODE.getLocalCode().equals(getLocalCode()) || TITLE.getLocalCode().equals(getLocalCode());
		boolean isIdentifier = IDENTIFIER.getDataStoreCode().equals(getDataStoreCode());
		return (isSortable() || globalMetadataWithNormalizedSortField) && (type == STRING || type == REFERENCE || isSeparatedStructure()) && !isMultivalue()
			   && !isIdentifier;
	}

	public Metadata getSortField() {
		return hasNormalizedSortField() ? getSortMetadata() : null;
	}

	public boolean isSameValueThan(Metadata otherMetadata) {
		boolean sameValue = type == otherMetadata.type &&
							isMultivalue() == otherMetadata.isMultivalue();

		if (sameValue && otherMetadata.type == MetadataValueType.REFERENCE) {
			sameValue = allowedReferences.equals(otherMetadata.getAllowedReferences());
		}

		if (sameValue && otherMetadata.type == MetadataValueType.ENUM) {
			sameValue = enumClass.equals(otherMetadata.getEnumClass());
		}

		if (sameValue && otherMetadata.type == MetadataValueType.STRUCTURE) {
			sameValue = structureFactory.getClass().equals(otherMetadata.getStructureFactory().getClass());
		}

		return sameValue;
	}

	public String getInputMask() {
		return inputMask;
	}

	public boolean hasSameCode(Metadata metadata) {
		return localCode.equals(metadata.getLocalCode());
	}

	public boolean isDuplicable() {
		return duplicable;
	}

	public boolean isMarkedForDeletion() {
		return inheritedMetadataBehaviors.isMarkedForDeletion();
	}

	public String getInheritanceCode() {
		return inheritanceCode;
	}

	public boolean isGlobal() {
		return global;
	}

	public Byte getCollectionId() {
		return collectionId;
	}

	public Short getTypeId() {
		return typeId;
	}

	public MetadataSchema getSchema() {
		return schema;
	}

	public MetadataSchemaType getSchemaType() {
		return schema.getSchemaType();
	}

	public boolean isStoredInSummaryCache() {
		return inheritance == null ? SchemaUtils.isSummary(this) : SchemaUtils.isSummary(inheritance);

	}

	public boolean isSame(Metadata otherMetadata) {
		if (otherMetadata == null) {
			return false;
		}

		if (id == 0 || otherMetadata.id == 0) {
			return isSameLocalCode(otherMetadata);

		} else {
			return id == otherMetadata.id;
		}
	}

	public boolean isCombinedStructure() {
		return (type == STRUCTURE && structureFactory instanceof CombinedStructureFactory) || type == CONTENT;
	}

	public boolean isSeparatedStructure() {
		return (type == STRUCTURE && structureFactory instanceof SeparatedStructureFactory);
	}
}
