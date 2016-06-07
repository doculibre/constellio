package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.CannotGetMetadatasOfAnotherSchemaType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException;

public class MetadataSchemaType {

	public static final String DEFAULT = "default";

	private final String code;

	private final String collection;

	private final Map<Language, String> labels;

	private final MetadataSchema defaultSchema;

	private final List<MetadataSchema> customSchemas;

	private final Map<String, Metadata> metadatasByAtomicCode;

	private final boolean security;

	private final boolean inTransactionLog;

	private final Boolean undeletable;
	private Collection<? extends Metadata> allMetadatas;

	public MetadataSchemaType(String code, String collection, Map<Language, String> labels, List<MetadataSchema> customSchemas,
			MetadataSchema defaultSchema, Boolean undeletable, boolean security, boolean inTransactionLog) {
		super();
		this.code = code;
		this.collection = collection;
		this.labels = Collections.unmodifiableMap(labels);
		this.customSchemas = Collections.unmodifiableList(customSchemas);
		this.defaultSchema = defaultSchema;
		this.undeletable = undeletable;
		this.security = security;
		this.inTransactionLog = inTransactionLog;
		this.metadatasByAtomicCode = Collections.unmodifiableMap(new SchemaUtils().buildMetadataByLocalCodeIndex(
				customSchemas, defaultSchema));
	}

	public String getCollection() {
		return collection;
	}

	public String getCode() {
		return code;
	}

	public Map<Language, String> getLabels() {
		return labels;
	}

	public String getLabel(Language language) {
		return labels.get(language);
	}

	public boolean isInTransactionLog() {
		return inTransactionLog;
	}

	public List<MetadataSchema> getSchemas() {
		return customSchemas;
	}

	public MetadataSchema getDefaultSchema() {
		return defaultSchema;
	}

	public List<MetadataSchema> getAllSchemas() {
		List<MetadataSchema> allSchemas = new ArrayList<>();
		allSchemas.addAll(customSchemas);
		allSchemas.add(defaultSchema);
		return allSchemas;
	}

	public MetadataSchema getCustomSchema(String code) {
		for (MetadataSchema metadataSchema : customSchemas) {
			if (metadataSchema.getLocalCode().equals(code) || metadataSchema.getCode().equals(code)) {
				return metadataSchema;
			}
		}
		throw new MetadataSchemasRuntimeException.NoSuchSchema(code);
	}

	public Metadata getMetadata(String metadataCode) {

		String[] parsedCode = SchemaUtils.underscoreSplitWithCache(metadataCode);

		String typeCode = parsedCode[0];
		String schemaCode = parsedCode[1];
		String localMetadataCode = parsedCode[2];

		if (typeCode.equals("global")) {
			typeCode = code;
		}

		if (!code.contains(typeCode)) {
			throw new CannotGetMetadatasOfAnotherSchemaType(typeCode, code);
		}

		Metadata metadata;
		if (schemaCode.contains(DEFAULT)) {
			metadata = getDefaultSchema().getMetadata(localMetadataCode);
		} else {
			metadata = getCustomSchema(schemaCode).getMetadata(localMetadataCode);
		}
		if (metadata == null) {
			throw new MetadataSchemaTypesBuilderRuntimeException.NoSuchMetadata(localMetadataCode);
		} else {
			return metadata;
		}
	}

	public List<Metadata> getAutomaticMetadatas() {
		List<Metadata> automaticMetadatas = new ArrayList<>();

		automaticMetadatas.addAll(defaultSchema.getAutomaticMetadatas());

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customAutomaticMetadata : customSchema.getAutomaticMetadatas()) {
				if (customAutomaticMetadata.getInheritance() == null) {
					automaticMetadatas.add(customAutomaticMetadata);
				}
			}
		}

		return Collections.unmodifiableList(automaticMetadatas);
	}

	public List<Metadata> getCalculatedMetadatas() {
		List<Metadata> metadatas = new ArrayList<>();
		for (Metadata metadata : getAutomaticMetadatas()) {
			if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
				metadatas.add(metadata);
			}
		}
		return metadatas;
	}

	public List<Metadata> getAllNonParentReferences() {
		List<Metadata> referenceMetadatas = new ArrayList<>();

		referenceMetadatas.addAll(defaultSchema.getNonParentReferences());

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customReferenceMetadata : customSchema.getNonParentReferences()) {
				if (customReferenceMetadata.getInheritance() == null) {
					referenceMetadatas.add(customReferenceMetadata);
				}
			}
		}

		return Collections.unmodifiableList(referenceMetadatas);
	}

	public List<Metadata> getAllParentReferences() {
		List<Metadata> parentReferenceMetadatas = new ArrayList<>();

		parentReferenceMetadatas.addAll(defaultSchema.getParentReferences());

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customParentReferenceMetadata : customSchema.getParentReferences()) {
				if (customParentReferenceMetadata.getInheritance() == null) {
					parentReferenceMetadatas.add(customParentReferenceMetadata);
				}
			}
		}

		return Collections.unmodifiableList(parentReferenceMetadatas);
	}

	public List<Metadata> getAllReferencesToTaxonomySchemas(List<Taxonomy> taxonomies) {
		List<Metadata> taxonomyReferencesMetadatas = new ArrayList<>();

		taxonomyReferencesMetadatas.addAll(defaultSchema.getTaxonomyRelationshipReferences(taxonomies));

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customParentReferenceMetadata : customSchema.getTaxonomyRelationshipReferences(taxonomies)) {
				if (customParentReferenceMetadata.getInheritance() == null) {
					taxonomyReferencesMetadatas.add(customParentReferenceMetadata);
				}
			}
		}

		return Collections.unmodifiableList(taxonomyReferencesMetadatas);
	}

	public List<Metadata> getTaxonomySchemasMetadataWithChildOfRelationship(List<Taxonomy> taxonomies) {
		List<Metadata> returnedMetadatas = new ArrayList<>();

		for (Taxonomy taxonomy : taxonomies) {
			if (taxonomy.getSchemaTypes().contains(code)) {
				for (Metadata metadata : getAllParentReferences()) {
					if (metadata.isChildOfRelationship()) {
						String typeCode = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
						if (taxonomy.getSchemaTypes().contains(typeCode)) {
							returnedMetadatas.add(metadata);
						}

					}
				}
			}
		}

		return Collections.unmodifiableList(returnedMetadatas);
	}

	public MetadataSchema getSchema(String codeOrCode) {
		MetadataSchema schema = null;
		if (codeOrCode.contains("_")) {
			schema = getSchemaWithCompleteCode(codeOrCode);
		} else {
			schema = getSchemaWithLocalCode(codeOrCode);
		}
		if (schema == null) {
			throw new MetadataSchemasRuntimeException.NoSuchSchema(codeOrCode);
		} else {
			return schema;
		}
	}

	private MetadataSchema getSchemaWithLocalCode(String code) {
		return "default".equals(code) ? getDefaultSchema() : getCustomSchema(code);
	}

	private MetadataSchema getSchemaWithCompleteCode(String schemaCode) {
		String[] parsedCode = SchemaUtils.underscoreSplitWithCache(schemaCode);
		String type = parsedCode[0];
		if (!type.equals(code)) {
			throw new CannotGetMetadatasOfAnotherSchemaType(type, code);
		}

		String schemaLocalCode = parsedCode[1];
		return getSchemaWithLocalCode(schemaLocalCode);
	}

	public Boolean isUndeletable() {
		return undeletable;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "MetadataSchemaType [code=" + code + ", label=" + labels + ", defaultSchema=" + defaultSchema
				+ ", customSchemas=" + customSchemas + ", undeletable=" + undeletable + "]";
	}

	public List<MetadataSchema> getCustomSchemas() {
		return Collections.unmodifiableList(customSchemas);
	}

	public Metadata getMetadataWithAtomicCode(String localCode) {
		try {
			return metadatasByAtomicCode.get(localCode);
		} catch (RuntimeException e) {
			throw new MetadataSchemasRuntimeException.NoSuchMetadataWithAtomicCode(localCode, e);
		}

	}

	public Metadata getMetadataWithDataStoreCode(String dataStoreCode) {
		String localCode = new SchemaUtils().getLocalCodeFromDataStoreCode(dataStoreCode);
		Metadata metadata = metadatasByAtomicCode.get(localCode);
		if (metadata == null) {
			throw new MetadataSchemasRuntimeException.NoSuchMetadataWithDatastoreCodeInSchemaType(dataStoreCode, code);
		}
		return metadata;
	}

	public MetadataList getAllMetadatas() {
		MetadataList metadatas = new MetadataList();

		metadatas.addAll(defaultSchema.getMetadatas());

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customParentReferenceMetadata : customSchema.getMetadatas()) {
				if (customParentReferenceMetadata.getInheritance() == null) {
					metadatas.add(customParentReferenceMetadata);
				}
			}
		}

		return metadatas.unModifiable();
	}

	public MetadataList getAllMetadatasIncludingThoseWithInheritance() {
		MetadataList metadatas = new MetadataList();

		metadatas.addAll(defaultSchema.getMetadatas());

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customParentReferenceMetadata : customSchema.getMetadatas()) {
				metadatas.add(customParentReferenceMetadata);
			}
		}

		return metadatas.unModifiable();
	}

	public boolean hasSecurity() {
		return security;
	}

	public List<Metadata> getAllMetadataIncludingInheritedOnes() {
		List<Metadata> metadatas = new ArrayList<>();

		metadatas.addAll(defaultSchema.getMetadatas());

		for (MetadataSchema customSchema : customSchemas) {
			for (Metadata customParentReferenceMetadata : customSchema.getMetadatas()) {
				metadatas.add(customParentReferenceMetadata);
			}
		}

		return metadatas;
	}
}
