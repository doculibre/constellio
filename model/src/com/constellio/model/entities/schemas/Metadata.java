package com.constellio.model.entities.schemas;

import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.schemas.builders.ClassListBuilder.combine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.utils.Factory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.sort.DefaultStringSortFieldNormalizer;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.schemas.SchemaUtils;

public class Metadata implements DataStoreField {

	final Metadata inheritance;

	final String collection;

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

	final String dataStoreCode;
	final String inheritanceCode;

	final boolean global;

	Metadata(String localCode, MetadataValueType type, boolean multivalue) {
		this("global_default", localCode, type, multivalue, false);
	}

	Metadata(String localCode, MetadataValueType type, boolean multivalue, boolean multiLingual) {
		this("global_default", localCode, type, multivalue, multiLingual);
	}

	Metadata(String schemaCode, String datastoreCode, MetadataValueType type, boolean multivalue, boolean multiLingual) {
		this.inheritance = null;

		this.enabled = false;
		this.collection = null;
		this.type = type;
		this.allowedReferences = null;
		this.inheritedMetadataBehaviors = new InheritedMetadataBehaviors(false, multivalue, false, false, false, false, false,
				false, false, false, false, false, false, multiLingual, false, new HashSet<String>(), false);
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
		this.structureFactory = null;
		this.enumClass = null;
		this.defaultValue = multivalue ? Collections.emptyList() : null;
		this.populateConfigs = new MetadataPopulateConfigs();
		this.duplicable = false;
		this.dataStoreCode = computeDataStoreCode();
		this.inheritanceCode = computeInheritanceCode();
		this.global = computeIsGlobal();

	}

	public final String computeInheritanceCode() {
		if (getInheritance() == null) {
			return getCode();
		} else {
			String[] parts = SchemaUtils.underscoreSplitWithCache(getCode());
			return parts[0] + "_default_" + parts[2];
		}
	}

	private String computeDataStoreCode() {
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

	public final boolean computeIsGlobal() {
		return Schemas.isGlobalMetadata(getLocalCode());
	}

	public Metadata(String localCode, String code, String collection, Map<Language, String> labels, Boolean enabled,
			InheritedMetadataBehaviors inheritedMetadataBehaviors, MetadataValueType type,
			AllowedReferences allowedReferences, Boolean defaultRequirement, DataEntry dataEntry,
			Set<RecordMetadataValidator<?>> recordMetadataValidators, String dataStoreType,
			MetadataAccessRestriction accessRestriction, StructureFactory structureFactory, Class<? extends Enum<?>> enumClass,
			Object defaultValue, String inputMask, MetadataPopulateConfigs populateConfigs,
			Factory<EncryptionServices> encryptionServices, Boolean duplicatbale) {
		super();

		this.inheritance = null;
		this.localCode = localCode;
		this.code = code;
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
		this.dataStoreCode = computeDataStoreCode();
		this.inheritanceCode = computeInheritanceCode();
		this.global = computeIsGlobal();
	}

	public Metadata(Metadata inheritance, Map<Language, String> labels, boolean enabled, boolean defaultRequirement, String code,
			Set<RecordMetadataValidator<?>> recordMetadataValidators, Object defaultValue, String inputMask,
			MetadataPopulateConfigs populateConfigs, boolean duplicable) {
		super();

		this.localCode = inheritance.getLocalCode();
		this.code = code;
		this.collection = inheritance.collection;
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
		this.dataStoreCode = computeDataStoreCode();
		this.inheritanceCode = computeInheritanceCode();
		this.global = computeIsGlobal();
	}

	public String getCode() {
		return code;
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getDataStoreCode() {
		return dataStoreCode;
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

	public String getReferencedSchemaType() {
		return getAllowedReferences().getTypeWithAllowedSchemas();
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

	public boolean isSearchable() {
		return getInheritedMetadataBehaviors().isSearchable();
	}

	public boolean isSortable() {
		return getInheritedMetadataBehaviors().isSortable();
	}

	public boolean isEssential() {
		return getInheritedMetadataBehaviors().isEssential();
	}

	public boolean isEssentialInSummary() {
		return getInheritedMetadataBehaviors().isEssentialInSummary();
	}

	public boolean isEncrypted() {
		return getInheritedMetadataBehaviors().isEncrypted();
	}

	public boolean isSchemaAutocomplete() {
		return getInheritedMetadataBehaviors().isSchemaAutocomplete();
	}

	public Set<String> getCustomAttributes() {
		return getInheritedMetadataBehaviors().getCustomAttributes();
	}

	public boolean isIncreasedDependencyLevel() {
		return getInheritedMetadataBehaviors().isReverseDependency();
	}

	public StringSortFieldNormalizer getSortFieldNormalizer() {
		return hasNormalizedSortField() ? new DefaultStringSortFieldNormalizer() : null;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dataEntry", "structureFactory", "encryptionServicesFactory");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dataEntry", "recordMetadataValidators", "structureFactory",
				"encryptionServicesFactory");
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

	public static Metadata newDummyMetadata(String schemaCode, String localCode, MetadataValueType type, boolean multivalue,
			boolean multiLingual) {
		return new Metadata(schemaCode, localCode, type, multivalue, multiLingual);
	}

	public static Metadata newGlobalMetadata(String dataStoreCode, MetadataValueType type, boolean multivalue,
			boolean multiLingual) {
		return new Metadata("global_default", dataStoreCode, type, multivalue, multiLingual);
	}

	public Metadata getSearchableMetadataWithLanguage(String language) {
		String schemaCode = code.replace("_" + localCode, "");
		return new Metadata(schemaCode, getDataStoreCode().replace("_s", "_t") + "_" + language, type, isMultivalue(),
				isMultiLingual());
	}

	public boolean isSameLocalCode(Metadata metadata) {
		return localCode.equals(metadata.getLocalCode());
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

	public boolean hasNormalizedSortField() {
		boolean globalMetadataWithNormalizedSortField =
				CODE.getLocalCode().equals(getLocalCode()) || TITLE.getLocalCode().equals(getLocalCode());
		boolean isIdentifier = IDENTIFIER.getDataStoreCode().equals(getDataStoreCode());
		return (isSortable() || globalMetadataWithNormalizedSortField) && type == STRING && !isMultivalue() && !isIdentifier;
	}

	public Metadata getSortField() {
		return hasNormalizedSortField() ? Schemas.getSortMetadata(this) : null;
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
}
