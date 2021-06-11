package com.constellio.model.entities.schemas;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCodeFormat;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.constellio.data.dao.services.records.DataStore.RECORDS;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class MetadataSchemaTypes implements Serializable {

	public static int LIMIT_OF_SCHEMAS__IN_TYPE = 500;
	public static int LIMIT_OF_TYPES_IN_COLLECTION = 500;

	private static final String DEFAULT = "default";

	private static final String UNDERSCORE = "_";

	private final int version;

	private final List<MetadataSchemaType> schemaTypesById;

	private final List<MetadataSchemaType> schemaTypes;

	private final Map<String, MetadataSchemaType> schemaTypesMap;

	private final List<String> schemaTypeCodesSortedByDependency;
	private final List<MetadataSchemaType> schemaTypesSortedByDependency;
	private List<String> referenceDefaultValues;
	private MetadataList searchableMetadatas;
	private final Set<String> typeParentOfOtherTypes;

	private final List<Language> languages;

	private final MetadataNetwork metadataNetwork;

	private final CollectionInfo collectionInfo;
	private final Map<String, List<MetadataSchemaType>> classifiedSchemaTypes;
	private final Map<String, List<MetadataSchemaType>> classifiedSchemaTypesIncludingSelf;


	public MetadataSchemaTypes(CollectionInfo collectionInfo, int version, List<MetadataSchemaType> schemaTypes,
							   List<String> schemaTypeCodesSortedByDependency, List<String> referenceDefaultValues,
							   List<Language> languages,
							   MetadataNetwork metadataNetwork) {
		super();
		this.version = version;
		this.schemaTypes = Collections.unmodifiableList(schemaTypes);
		this.schemaTypeCodesSortedByDependency = schemaTypeCodesSortedByDependency;
		this.referenceDefaultValues = referenceDefaultValues;
		this.searchableMetadatas = getSearchableMetadatas(schemaTypes);
		this.schemaTypesMap = toUnmodifiableMap(schemaTypes);
		this.schemaTypesSortedByDependency = buildSchemaTypesSortedByDependency(schemaTypeCodesSortedByDependency, schemaTypesMap);
		this.languages = Collections.unmodifiableList(languages);
		this.typeParentOfOtherTypes = buildTypeParentOfOtherTypes(schemaTypes);
		this.schemaTypesById = buildTypesById(schemaTypes);
		this.classifiedSchemaTypes = buildClassifiedSchemaTypes(schemaTypesSortedByDependency, false);
		this.classifiedSchemaTypesIncludingSelf = buildClassifiedSchemaTypes(schemaTypesSortedByDependency, true);
		this.metadataNetwork = metadataNetwork;
		this.collectionInfo = collectionInfo;
		for (MetadataSchemaType schemaType : schemaTypes) {
			schemaType.setBuiltSchemaTypes(this);
		}
	}

	private List<MetadataSchemaType> buildSchemaTypesSortedByDependency(List<String> schemaTypeCodesSortedByDependency,
																		Map<String, MetadataSchemaType> schemaTypesMap) {

		List<MetadataSchemaType> schemaTypes = new ArrayList<>();

		for (String code : schemaTypeCodesSortedByDependency) {
			schemaTypes.add(schemaTypesMap.get(code));
		}

		return schemaTypes;
	}

	private static Map<String, List<MetadataSchemaType>> buildClassifiedSchemaTypes(
			List<MetadataSchemaType> schemaTypes, boolean includingSelf) {
		KeyListMap<String, MetadataSchemaType> classifiedSchemaTypes = new KeyListMap<>();
		KeyListMap<String, String> classifiedSchemaTypesCodes = new KeyListMap<>();

		for (MetadataSchemaType type : schemaTypes) {
			for (MetadataSchemaType anotherType : schemaTypes) {
				if (includingSelf || type != anotherType) {
					for (Metadata metadata : anotherType.getDefaultSchema().getMetadatas()) {
						if ((metadata.isTaxonomyRelationship() || metadata.isChildOfRelationship())
							&& type.getCode().equals(metadata.getReferencedSchemaTypeCode())) {
							if (!classifiedSchemaTypesCodes.contains(type.getCode(), anotherType.getCode())) {
								classifiedSchemaTypesCodes.add(type.getCode(), anotherType.getCode());
								classifiedSchemaTypes.add(type.getCode(), anotherType);
							}

						}
					}


				} else {
					//TODO Retirer cette passe de l'ours
					for (Metadata metadata : anotherType.getDefaultSchema().getMetadatas()) {
						if ((metadata.isChildOfRelationship()) && type.getCode().equals(metadata.getReferencedSchemaTypeCode())
							&& type.getCode().equals("folder")) {
							if (!classifiedSchemaTypesCodes.contains(type.getCode(), anotherType.getCode())) {
								classifiedSchemaTypesCodes.add(type.getCode(), anotherType.getCode());
								classifiedSchemaTypes.add(type.getCode(), anotherType);
							}
						}
					}

				}
			}
		}

		return classifiedSchemaTypes.getNestedMap();
	}


	private List<MetadataSchemaType> buildTypesById(List<MetadataSchemaType> schemaTypes) {
		MetadataSchemaType[] types = new MetadataSchemaType[LIMIT_OF_TYPES_IN_COLLECTION];

		for (MetadataSchemaType type : schemaTypes) {
			types[type.getId()] = type;
		}

		return Collections.unmodifiableList(Arrays.asList(types));
	}

	public List<MetadataSchemaType> getClassifiedSchemaTypesIn(String schemaTypeCode) {
		List<MetadataSchemaType> types = classifiedSchemaTypes.get(schemaTypeCode);
		return types == null ? Collections.emptyList() : new ArrayList<>(types);
	}

	public List<MetadataSchemaType> getClassifiedSchemaTypesIncludingSelfIn(String schemaTypeCode) {
		List<MetadataSchemaType> types = classifiedSchemaTypesIncludingSelf.get(schemaTypeCode);
		return types == null ? Collections.emptyList() : new ArrayList<>(types);
	}

	public CollectionInfo getCollectionInfo() {
		return collectionInfo;
	}

	public static MetadataList getSearchableMetadatas(List<MetadataSchemaType> schemaTypes) {
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

	public List<MetadataSchemaType> getSchemaTypesInDisplayOrder() {
		//TODO : Improve this ugly coupling!!!
		List<MetadataSchemaType> schemaTypesInDisplayOrder = new ArrayList<>();

		MetadataSchemaType folderSchemaType = null;
		MetadataSchemaType documentSchemaType = null;
		MetadataSchemaType taskSchemaType = null;

		for (MetadataSchemaType type : getSchemaTypes()) {
			switch (type.getCode()) {
				case "folder":
					folderSchemaType = type;

					break;
				case "document":
					documentSchemaType = type;

					break;
				case "userTask":
					taskSchemaType = type;

					break;
				default:
					schemaTypesInDisplayOrder.add(type);
					break;
			}
		}

		if (folderSchemaType != null) {
			schemaTypesInDisplayOrder.add(folderSchemaType);
		}

		if (documentSchemaType != null) {
			schemaTypesInDisplayOrder.add(documentSchemaType);
		}

		if (taskSchemaType != null) {
			schemaTypesInDisplayOrder.add(taskSchemaType);
		}

		return schemaTypesInDisplayOrder;
	}

	public MetadataSchemaType getSchemaType(String schemaTypeCode) {
		MetadataSchemaType schemaType = schemaTypesMap.get(schemaTypeCode);

		if (schemaType == null) {
			throw new MetadataSchemasRuntimeException.NoSuchSchemaType(schemaTypeCode);
		}

		return schemaType;
	}

	public MetadataSchemaType getSchemaType(short typeId) {
		MetadataSchemaType schemaType = schemaTypesById.get(typeId);

		if (schemaType == null) {
			throw new MetadataSchemasRuntimeException.NoSuchSchemaType(typeId);
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

	public List<String> getSchemaTypesCodesSortedByDependency() {
		return schemaTypeCodesSortedByDependency;
	}

	public List<MetadataSchemaType> getSchemaTypesSortedByDependency() {
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

	@Deprecated
	public List<MetadataSchemaType> getSchemaTypesWithCodesSortedByDependency(Set<String> returnedTypeCodes) {
		List<MetadataSchemaType> returnedMetadataSchemaTypes = new ArrayList<>();
		for (MetadataSchemaType type : getSchemaTypesSortedByDependency()) {
			if (returnedTypeCodes.contains(type.getCode())) {
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
		if ("type".equals(metadata.getCode()) || (metadata.getType() == REFERENCE && !metadata.isMultivalue())) {
			MetadataSchema referencedSchema = getDefaultSchema(metadata.getReferencedSchemaTypeCode());
			return referencedSchema.hasMetadataWithCode("linkedSchema");
		}
		return false;
	}

	public Set<String> getTypeParentOfOtherTypes() {
		return typeParentOfOtherTypes;
	}

	public Stream<MetadataSchemaType> streamTypes() {
		return getSchemaTypes().stream();

	}

	public MetadataSchema getSchemaOf(Record record) {
		return schemaTypesMap.get(record.getTypeCode()).getSchema(record.getSchemaCode());
	}
}