package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCodeFormat;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class MetadataSchemaTypes {

	private static final String DEFAULT = "default";

	private static final String UNDERSCORE = "_";

	private final int version;

	private final String collection;

	private final List<MetadataSchemaType> schemaTypes;
	private final Map<String, MetadataSchemaType> schemaTypesMap;

	private final List<String> schemaTypesSortedByDependency;
	private List<String> referenceDefaultValues;
	private MetadataList searchableMetadatas;

	private final List<Language> languages;

	public MetadataSchemaTypes(String collection, int version, List<MetadataSchemaType> schemaTypes,
			List<String> schemaTypesSortedByDependency, List<String> referenceDefaultValues, List<Language> languages) {
		super();
		this.version = version;
		this.collection = collection;
		this.schemaTypes = Collections.unmodifiableList(schemaTypes);
		this.schemaTypesSortedByDependency = schemaTypesSortedByDependency;
		this.referenceDefaultValues = referenceDefaultValues;
		this.searchableMetadatas = getAllMetadatas().onlySearchable();
		this.schemaTypesMap = toUnmodifiableMap(schemaTypes);
		this.languages = Collections.unmodifiableList(languages);
	}

	private Map<String, MetadataSchemaType> toUnmodifiableMap(List<MetadataSchemaType> schemaTypes) {
		Map<String, MetadataSchemaType> types = new HashMap<>();
		for (MetadataSchemaType type : schemaTypes) {
			types.put(type.getCode(), type);
		}
		return Collections.unmodifiableMap(types);
	}

	public String getCollection() {
		return collection;
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

	public List<Metadata> getHighlightedMetadatas() {
		return searchableMetadatas;
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
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
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

	public boolean hasType(String schemaType) {
		try {
			getSchemaType(schemaType);
			return true;
		} catch (MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return false;
		}
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
}
