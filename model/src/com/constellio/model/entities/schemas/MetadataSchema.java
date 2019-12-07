package com.constellio.model.entities.schemas;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MetadataSchema implements Serializable {

	private static final String UNDERSCORE = "_";

	private final short id;

	private final String localCode;

	private final String code;

	private Map<Language, String> labels;

	private final MetadataList metadatas;

	private final Boolean undeletable;

	private final boolean inTransactionLog;

	private final Set<RecordValidator> schemaValidators;

	private final Map<String, Metadata> indexByDataStoreCode;

	private final Map<String, Metadata> indexByLocalCode;

	private final Map<String, Metadata> indexByCode;

	private final Map<Short, Metadata> indexById;

	private MetadataSchemaCalculatedInfos calculatedInfos;

	private final boolean active;

	private final String dataStore;

	private final CollectionInfo collectionInfo;

	private List<Metadata> summaryMetadatas;

	private List<Metadata> cacheIndexMetadatas;

	private boolean hasEagerTransientMetadata;

	private final short typeId;

	private List<Metadata> referencesToSummaryCachedType;

	private MetadataSchemaType schemaType;

	public MetadataSchema(short typeId, short id, String localCode, String code, CollectionInfo collectionInfo,
						  Map<Language, String> labels,
						  List<Metadata> metadatas,
						  Boolean undeletable, boolean inTransactionLog, Set<RecordValidator> schemaValidators,
						  MetadataSchemaCalculatedInfos calculatedInfos, String dataStore, boolean active,
						  ConstellioEIMConfigs configs) {
		super();
		this.typeId = typeId;
		this.id = id;
		this.localCode = localCode;
		this.code = code;
		this.labels = new HashMap<>(labels);
		this.inTransactionLog = inTransactionLog;
		this.metadatas = new MetadataList(metadatas).unModifiable();
		Set<Short> uniqueIds = metadatas.stream().map(Metadata::getId).collect(Collectors.toSet());
		if (uniqueIds.size() != metadatas.size()) {
			throw new IllegalStateException("Multiple metadatas with same id");
		}
		this.undeletable = undeletable;
		this.schemaValidators = schemaValidators;
		this.calculatedInfos = calculatedInfos;
		this.indexByLocalCode = Collections.unmodifiableMap(new SchemaUtils().buildIndexByLocalCode(metadatas));
		this.indexByCode = Collections.unmodifiableMap(new SchemaUtils().buildIndexByCode(metadatas));
		this.indexByDataStoreCode = Collections.unmodifiableMap(new SchemaUtils().buildIndexByDatastoreCode(metadatas));
		this.indexById = Collections.unmodifiableMap(new SchemaUtils().buildIndexById(metadatas));
		this.dataStore = dataStore;
		this.active = active;
		this.collectionInfo = collectionInfo;
		this.summaryMetadatas = new SchemaUtils().buildListOfSummaryMetadatas(metadatas, configs);
		this.cacheIndexMetadatas = new SchemaUtils().buildListOfCacheIndexMetadatas(metadatas);
		this.hasEagerTransientMetadata = metadatas.stream().anyMatch((m) -> m.getTransiency() == MetadataTransiency.TRANSIENT_EAGER);
		for (Metadata metadata : metadatas) {
			metadata.setBuiltSchema(this);
		}

	}


	public void setBuiltSchemaType(MetadataSchemaType schemaType) {
		if (this.schemaType != null) {
			throw new IllegalStateException("Schematype already");
		}
		this.schemaType = schemaType;
	}

	public List<Metadata> getSummaryMetadatas() {
		return summaryMetadatas;
	}

	public boolean hasEagerTransientMetadata() {
		return hasEagerTransientMetadata;
	}

	public short getId() {
		return id;
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getCode() {
		return code;
	}

	public String getCollection() {
		return collectionInfo.getCode();
	}

	public CollectionInfo getCollectionInfo() {
		return collectionInfo;
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getFrenchLabel() {
		return labels.get(Language.French);
	}

	public String getLabel(Language language) {
		return labels.get(language);
	}

	public MetadataList getMetadatas() {
		return metadatas;
	}

	public Set<RecordValidator> getValidators() {
		return schemaValidators;
	}

	public Boolean isUndeletable() {
		return undeletable;
	}

	public boolean hasMetadataWithCode(String metadataCode) {
		try {
			String localCode = new SchemaUtils().getLocalCodeFromMetadataCode(metadataCode);
			return indexByLocalCode.get(localCode) != null;
		} catch (MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType | MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchema e) {
			return false;
		}
	}

	public Metadata get(String metadataCode) {
		return getMetadata(metadataCode);
	}

	public Metadata getMetadataByDatastoreCode(String dataStoreCode) {
		return indexByDataStoreCode.get(dataStoreCode);
	}

	public Metadata getMetadataById(Short id) {
		return indexById.get(id);
	}


	public Metadata getMetadata(String metadataCode) {

		if (metadataCode.endsWith("PId")) {
			metadataCode = metadataCode.substring(0, metadataCode.length() - 3);
		}

		if (metadataCode.endsWith("Id")) {
			metadataCode = metadataCode.substring(0, metadataCode.length() - 2);
		}

		metadataCode = StringUtils.substringBefore(metadataCode, ".");

		String localCode = metadataCode;
		if (localCode.contains("_")) {
			String[] codes = SchemaUtils.underscoreSplitWithCache(metadataCode);
			localCode = codes[codes.length - 1];
		}

		Metadata metadata = indexByLocalCode.get(localCode);

		if (metadata == null) {
			metadata = indexByCode.get(metadataCode);
		}

		if (metadata == null) {
			throw new MetadataSchemasRuntimeException.NoSuchMetadata(metadataCode);
		} else {
			return metadata;
		}
	}

	public boolean metadataExists(String code) {
		return indexByCode.get(code) != null;
	}

	public List<Metadata> getAutomaticMetadatas() {
		return calculatedInfos.getAutomaticMetadatas();
	}

	public List<Metadata> getEagerTransientMetadatas() {
		return calculatedInfos.getEagerTransientMetadatas();
	}

	public List<Metadata> getLazyTransientMetadatas() {
		return calculatedInfos.getLazyTransientMetadatas();
	}

	public List<Metadata> getTaxonomyRelationshipReferences(List<Taxonomy> taxonomies) {
		List<Metadata> returnedMetadata = new ArrayList<>();

		for (Taxonomy taxonomy : taxonomies) {
			returnedMetadata.addAll(getTaxonomyRelationshipReferences(taxonomy));
		}

		return returnedMetadata;
	}

	public List<Metadata> getTaxonomyRelationshipReferences(Taxonomy taxonomy) {
		List<Metadata> returnedMetadata = new ArrayList<>();

		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(code);
		if (!taxonomy.getSchemaTypes().contains(schemaTypeCode)) {
			for (Metadata metadata : metadatas) {
				if (metadata.isTaxonomyRelationship() && metadata.getType() == MetadataValueType.REFERENCE) {
					String referencedType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
					if (taxonomy.getSchemaTypes().contains(referencedType) && metadata.isTaxonomyRelationship()) {
						returnedMetadata.add(metadata);
					}
				}
			}
		}

		return returnedMetadata;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "schemaValidators", "calculatedInfos", "schemaType");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "schemaValidators", "calculatedInfos", "schemaType");
	}

	@Override
	public String toString() {
		return localCode;
	}

	public List<Metadata> getParentReferences() {
		return metadatas.onlyParentReferences();
	}

	public List<Metadata> getNonParentReferences() {
		return metadatas.onlyNonParentReferences();
	}

	public Map<String, Metadata> getIndexByLocalCode() {
		return indexByLocalCode;
	}

	public boolean isInTransactionLog() {
		return inTransactionLog;
	}

	public List<RecordPreparationStep> getPreparationSteps() {
		return calculatedInfos.getRecordPreparationSteps();
	}

	public List<Metadata> getContentMetadatasForPopulate() {
		return calculatedInfos.getContentMetadatasForPopulate();
	}

	public String getDataStore() {
		return dataStore;
	}

	public List<Metadata> getCacheIndexMetadatas() {
		return cacheIndexMetadatas;
	}

	public boolean hasMultilingualMetadatas() {
		boolean multilingualMetadatas = false;
		for (Metadata metadata : metadatas) {
			multilingualMetadatas |= metadata.isMultiLingual();
		}
		return multilingualMetadatas;
	}

	public boolean isActive() {
		return active;
	}

	public MetadataSchemaType getSchemaType() {
		return schemaType;
	}
}
