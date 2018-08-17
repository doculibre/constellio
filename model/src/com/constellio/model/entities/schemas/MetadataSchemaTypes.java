package com.constellio.model.entities.schemas;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCodeFormat;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.data.dao.services.records.DataStore.RECORDS;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class MetadataSchemaTypes implements Serializable {

	private static final String DEFAULT = "default";

	private static final String UNDERSCORE = "_";

	private final int version;

	private final List<MetadataSchemaType> schemaTypes;
	private final Map<String, MetadataSchemaType> schemaTypesMap;

	private final List<String> schemaTypesSortedByDependency;
	private List<String> referenceDefaultValues;
	private MetadataList searchableMetadatas;
	private final Set<String> typeParentOfOtherTypes;

	private final List<Language> languages;

	private final MetadataNetwork metadataNetwork;

	private final CollectionInfo collectionInfo;

	public MetadataSchemaTypes(CollectionInfo collectionInfo, int version, List<MetadataSchemaType> schemaTypes,
							   List<String> schemaTypesSortedByDependency, List<String> referenceDefaultValues,
							   List<Language> languages,
							   MetadataNetwork metadataNetwork) {
		super();
		this.version = version;
		this.schemaTypes = Collections.unmodifiableList(schemaTypes);
		this.schemaTypesSortedByDependency = schemaTypesSortedByDependency;
		this.referenceDefaultValues = referenceDefaultValues;
		this.searchableMetadatas = getSearchableMetadatas(schemaTypes);
		this.schemaTypesMap = toUnmodifiableMap(schemaTypes);
		this.languages = Collections.unmodifiableList(languages);
		this.typeParentOfOtherTypes = buildTypeParentOfOtherTypes(schemaTypes);
		this.metadataNetwork = metadataNetwork;
		this.collectionInfo = collectionInfo;
	}

	public CollectionInfo getCollectionInfo() {
		return collectionInfo;
	}

	private MetadataList getSearchableMetadatas(List<MetadataSchemaType> schemaTypes) {
		MetadataList searchableMetadatas = new MetadataList();
		Set<String> searchableMetadatasDataStoreCodes = new HashSet<>();
		for (MetadataSchemaType schemaType : schemaTypes) {
			for (MetadataSchema schema : schemaType.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getInheritance() == null && metadata.isSearchable()
						&& !searchableMetadatasDataStoreCodes.contains(metadata.getDataStoreCode())) {
						searchableMetadatasDataStoreCodes.add(metadata.getDataStoreCode());
						searchableMetadatas.add(metadata);
					}
				}
			}
		}
		return searchableMetadatas.unModifiable();
	}

	private Set<String> buildTypeParentOfOtherTypes(List<MetadataSchemaType> schemaTypes) {

		Set<String> typeParentOfOtherTypes = new HashSet<>();

		for (MetadataSchemaType type : schemaTypes) {
			secondFor:
			for (MetadataSchemaType anotherType : schemaTypes) {
				for (Metadata metadata : anotherType.getAllMetadatas()) {
					if (metadata.getType() == REFERENCE && metadata.isChildOfRelationship()
						&& metadata.getAllowedReferences().isAllowed(type)) {
						typeParentOfOtherTypes.add(type.getCode());
						break secondFor;
					}
				}
			}
		}

		return Collections.unmodifiableSet(typeParentOfOtherTypes);

	}

	private Map<String, MetadataSchemaType> toUnmodifiableMap(List<MetadataSchemaType> schemaTypes) {
		Map<String, MetadataSchemaType> types = new HashMap<>();
		for (MetadataSchemaType type : schemaTypes) {
			types.put(type.getCode(), type);
		}
		return Collections.unmodifiableMap(types);
	}

	public MetadataNetwork getMetadataNetwork() {
		return metadataNetwork;
	}

	public String getCollection() {
		return collectionInfo.getCode();
	}

	public int getVersion() {
		return version;
	}

	public List<Language> getLanguages() {
		return languages;
	}

	public List<MetadataSchemaType> getSchemaTypesWithCode(List<String> codes) {
		List<MetadataSchemaType> types = new ArrayList<>();

		for (String code : codes) {
			types.add(getSchemaType(code));
		}

		return types;
	}

	public List<Metadata> getSearchableMetadatas() {
		return searchableMetadatas;
	}

	public List<MetadataSchemaType> getRecordsDataStoreSchemaTypes() {
		List<MetadataSchemaType> returnedSchemaTypes = new ArrayList<>();

		for (MetadataSchemaType schemaType : schemaTypes) {
			if (RECORDS.equals(schemaType.getDataStore())) {
				returnedSchemaTypes.add(schemaType);
			}
		}

		return returnedSchemaTypes;
	}

	public List<MetadataSchemaType> getSchemaTypes() {
		return schemaTypes;
	}

	public MetadataSchemaType getSchemaType(String schemaTypeCode) {
		MetadataSchemaType schemaType = schemaTypesMap.get(schemaTypeCode);

		if (schemaType == null) {
			throw new MetadataSchemasRuntimeException.NoSuchSchemaType(schemaTypeCode);
		}

		return schemaType;
	}

	public MetadataSchema getDefaultSchema(String schemaTypeCode) {
		MetadataSchemaType schemaType = getSchemaType(schemaTypeCode);
		return schemaType.getDefaultSchema();
	}

	public MetadataSchema getSchema(String code) {
		if (code == null) {
			throw new IllegalArgumentException("Code required");
		}
		String[] parsedCode = SchemaUtils.underscoreSplitWithCache(code);

		String typeCode;
		String schemaCode;
		if (parsedCode.length == 2) {
			typeCode = parsedCode[0];
			schemaCode = parsedCode[1];
		} else {
			throw new InvalidCodeFormat(code);
		}

		MetadataSchemaType schemaType = getSchemaType(typeCode);
		MetadataSchema schema = null;
		if (schemaCode.equals(DEFAULT)) {
			schema = schemaType.getDefaultSchema();
		} else {
			schema = schemaType.getCustomSchema(schemaCode);
		}
		return schema;

	}

	public Metadata getMetadata(String code) {
		String[] parsedCode = SchemaUtils.underscoreSplitWithCache(code);

		String typeCode;
		if (parsedCode.length == 3) {
			typeCode = parsedCode[0];
		} else {
			throw new InvalidCodeFormat(code);
		}

		MetadataSchemaType schemaType = getSchemaType(typeCode);
		return schemaType.getMetadata(code);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "metadataNetwork");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "metadataNetwork");
	}

	@Override
	public String toString() {
		return "MetadataSchemaTypes [version=" + version + ", schemaTypes=" + schemaTypes + "]";
	}

	public MetadataList getMetadatas(List<String> metadataCompleteCodes) {
		List<Metadata> metadatas = new ArrayList<>();

		for (String metadataCompleteCode : metadataCompleteCodes) {
			metadatas.add(getMetadata(metadataCompleteCode));
		}

		return new MetadataList(metadatas).unModifiable();
	}

	public List<String> getSchemaTypesSortedByDependency() {
		return schemaTypesSortedByDependency;
	}

	public MetadataList getAllMetadatas() {
		MetadataList metadatas = new MetadataList();
		for (MetadataSchemaType schemaType : schemaTypes) {
			metadatas.addAll(schemaType.getAllMetadatas());
		}
		return metadatas.unModifiable();
	}

	public MetadataList getAllMetadatasIncludingThoseWithInheritance() {
		MetadataList metadatas = new MetadataList();
		for (MetadataSchemaType schemaType : schemaTypes) {
			metadatas.addAll(schemaType.getAllMetadatasIncludingThoseWithInheritance());
		}
		return metadatas.unModifiable();
	}

	public MetadataList getAllContentMetadatas() {
		return getAllMetadatas().onlyWithType(MetadataValueType.CONTENT);
	}

	public boolean hasMetadata(String metadataCode) {

		String schemaCode = new SchemaUtils().getSchemaCode(metadataCode);
		if (!hasSchema(schemaCode)) {
			return false;
		}

		try {
			getMetadata(metadataCode);
			return true;
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			return false;
		}
	}

	public List<String> getReferenceDefaultValues() {
		return referenceDefaultValues;
	}

	public List<MetadataSchemaType> getSchemaTypesWithCodesSortedByDependency(Set<String> returnedTypeCodes) {
		List<MetadataSchemaType> returnedMetadataSchemaTypes = new ArrayList<>();
		for (String typeCode : getSchemaTypesSortedByDependency()) {
			if (returnedTypeCodes.contains(typeCode)) {
				MetadataSchemaType type = getSchemaType(typeCode);
				returnedMetadataSchemaTypes.add(type);
			}
		}

		return returnedMetadataSchemaTypes;
	}

	public List<MetadataSchemaType> getSchemaTypesSortedByLabelsOfLanguage(final Language language) {
		List<MetadataSchemaType> types = new ArrayList<>(schemaTypes);
		Collections.sort(types, new Comparator<MetadataSchemaType>() {
			@Override
			public int compare(MetadataSchemaType o1, MetadataSchemaType o2) {
				return o1.getLabel(language).compareTo(o2.getLabel(language));
			}
		});
		return Collections.unmodifiableList(types);
	}

	public boolean hasType(String schemaType) {
		return schemaTypesMap.containsKey(schemaType);
	}

	public boolean hasSchema(String schemaCode) {
		try {
			getSchema(schemaCode);
			return true;
		} catch (MetadataSchemasRuntimeException.NoSuchSchema e) {
			return false;

		} catch (MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return false;

		}
	}

	public List<Metadata> getAllMetadataIncludingInheritedOnes() {
		List<Metadata> metadatas = new ArrayList<>();
		for (MetadataSchemaType schemaType : schemaTypes) {
			metadatas.addAll(schemaType.getAllMetadataIncludingInheritedOnes());
		}
		return metadatas;
	}

	public boolean isRecordTypeMetadata(Metadata metadata) {
		if ("type".equals(metadata.getCode()) || metadata.getType() == REFERENCE) {
			MetadataSchema referencedSchema = getDefaultSchema(metadata.getReferencedSchemaType());
			return referencedSchema.hasMetadataWithCode("linkedSchema");
		}
		return false;
	}

	public Set<String> getTypeParentOfOtherTypes() {
		return typeParentOfOtherTypes;
	}
}