package com.constellio.model.entities.schemas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class MetadataSchema implements Serializable {

	private static final String UNDERSCORE = "_";

	private final String localCode;

	private final String code;

	private final String collection;

	private Map<Language, String> labels;

	private final MetadataList metadatas;

	private final Boolean undeletable;

	private final boolean inTransactionLog;

	private final Set<RecordValidator> schemaValidators;

	private final Map<String, Metadata> indexByLocalCode;

	private final Map<String, Metadata> indexByCode;

	private MetadataSchemaCalculatedInfos calculatedInfos;

	public MetadataSchema(String localCode, String code, String collection, Map<Language, String> labels,
			List<Metadata> metadatas,
			Boolean undeletable, boolean inTransactionLog, Set<RecordValidator> schemaValidators,
			MetadataSchemaCalculatedInfos calculatedInfos) {
		super();
		this.localCode = localCode;
		this.code = code;
		this.collection = collection;
		this.labels = new HashMap<>(labels);
		this.inTransactionLog = inTransactionLog;
		this.metadatas = new MetadataList(metadatas).unModifiable();
		this.undeletable = undeletable;
		this.schemaValidators = schemaValidators;
		this.calculatedInfos = calculatedInfos;
		this.indexByLocalCode = Collections.unmodifiableMap(new SchemaUtils().buildIndexByLocalCode(metadatas));
		this.indexByCode = Collections.unmodifiableMap(new SchemaUtils().buildIndexByCode(metadatas));
	}

	public String getLocalCode() {
		return localCode;
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
			String localCode = new SchemaUtils().getLocalCode(metadataCode, code);

			return indexByLocalCode.get(localCode) != null;
		} catch (MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType e) {
			return false;
		}
	}

	public Metadata get(String metadataCode) {
		return getMetadata(metadataCode);
	}

	public Metadata getMetadata(String metadataCode) {

		if (metadataCode.endsWith("PId")) {
			metadataCode = metadataCode.substring(0, metadataCode.length() - 3);
		}

		if (metadataCode.endsWith("Id")) {
			metadataCode = metadataCode.substring(0, metadataCode.length() - 2);
		}

		Metadata metadata = indexByLocalCode.get(metadataCode);

		if (metadata == null) {
			metadata = indexByCode.get(metadataCode);
		}

		if (metadata == null) {
			throw new MetadataSchemasRuntimeException.NoSuchMetadata(metadataCode);
		} else {
			return metadata;
		}
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
		return HashCodeBuilder.reflectionHashCode(this, "schemaValidators", "calculatedInfos");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "schemaValidators", "calculatedInfos");
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
}
