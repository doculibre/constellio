/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.entities.schemas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.InvalidCodeFormat;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class MetadataSchemaTypes {

	private static final String DEFAULT = "default";

	private static final String UNDERSCORE = "_";

	private final int version;

	private final String collection;

	private final List<MetadataSchemaType> schemaTypes;

	private final List<String> schemaTypesSortedByDependency;
	private List<String> referenceDefaultValues;

	public MetadataSchemaTypes(String collection, int version, List<MetadataSchemaType> schemaTypes,
			List<String> schemaTypesSortedByDependency, List<String> referenceDefaultValues) {
		super();
		this.version = version;
		this.collection = collection;
		this.schemaTypes = Collections.unmodifiableList(schemaTypes);
		this.schemaTypesSortedByDependency = schemaTypesSortedByDependency;
		this.referenceDefaultValues = referenceDefaultValues;
	}

	public String getCollection() {
		return collection;
	}

	public int getVersion() {
		return version;
	}

	public List<MetadataSchemaType> getSchemaTypesWithCode(List<String> codes) {
		List<MetadataSchemaType> types = new ArrayList<>();

		for (String code : codes) {
			types.add(getSchemaType(code));
		}

		return types;
	}

	public List<MetadataSchemaType> getSchemaTypes() {
		return schemaTypes;
	}

	public MetadataSchemaType getSchemaType(String schemaTypeCode) {

		for (MetadataSchemaType schemaType : schemaTypes) {
			if (schemaTypeCode.equals(schemaType.getCode())) {
				return schemaType;
			}
		}
		throw new MetadataSchemasRuntimeException.NoSuchSchemaType(schemaTypeCode);
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

	public List<Metadata> getMetadatas(List<String> metadataCompleteCodes) {
		List<Metadata> metadatas = new ArrayList<>();

		for (String metadataCompleteCode : metadataCompleteCodes) {
			metadatas.add(getMetadata(metadataCompleteCode));
		}

		return Collections.unmodifiableList(metadatas);
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
}
