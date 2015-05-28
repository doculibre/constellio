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
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class MetadataSchema {

	private static final String UNDERSCORE = "_";

	private final String localCode;

	private final String code;

	private final String collection;

	private final String label;

	private final List<Metadata> automaticMetadatas;

	private final MetadataList metadatas;

	private final Boolean undeletable;

	private final Set<RecordValidator> schemaValidators;

	private final Map<String, Metadata> indexByAtomicCode;

	public MetadataSchema(String localCode, String code, String collection, String label, List<Metadata> metadatas,
			Boolean undeletable, Set<RecordValidator> schemaValidators, List<Metadata> automaticMetadatas) {
		super();
		this.localCode = localCode;
		this.code = code;
		this.collection = collection;
		this.label = label;
		this.metadatas = new MetadataList(metadatas).unModifiable();
		this.undeletable = undeletable;
		this.schemaValidators = schemaValidators;
		this.automaticMetadatas = automaticMetadatas;
		this.indexByAtomicCode = Collections.unmodifiableMap(new SchemaUtils().buildIndexByLocalCode(metadatas));
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

	public String getLabel() {
		return label;
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
		String localCode = new SchemaUtils().getLocalCode(metadataCode, code);

		return indexByAtomicCode.get(localCode) != null;
	}

	public Metadata getMetadata(String metadataCode) {

		String localCode = new SchemaUtils().getLocalCode(metadataCode, code);

		Metadata metadata = indexByAtomicCode.get(localCode);
		if (metadata == null) {
			throw new MetadataSchemasRuntimeException.NoSuchMetadata(localCode);
		} else {
			return metadata;
		}
	}

	public List<Metadata> getAutomaticMetadatas() {
		return automaticMetadatas;
	}

	public List<Metadata> getTaxonomyRelationshipReferences(List<Taxonomy> taxonomies) {
		List<Metadata> returnedMetadata = new ArrayList<>();

		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(code);
		for (Taxonomy taxonomy : taxonomies) {
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
		}

		return returnedMetadata;
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
		return localCode;
	}

	public List<Metadata> getParentReferences() {
		return metadatas.onlyParentReferences();
	}

	public List<Metadata> getNonParentReferences() {
		return metadatas.onlyNonParentReferences();
	}

	public Map<String, Metadata> getIndexByAtomicCode() {
		return indexByAtomicCode;
	}

}
