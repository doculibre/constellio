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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;

public class Metadata implements DataStoreField {

	final Metadata inheritance;

	final String collection;

	final String localCode;

	final String code;

	final String label;

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

	Metadata(String localCode, MetadataValueType type, boolean multivalue) {
		this("global_default", localCode, type, multivalue);
	}

	Metadata(String schemaCode, String datastoreCode, MetadataValueType type, boolean multivalue) {
		this.inheritance = null;

		this.label = null;
		this.enabled = false;
		this.collection = null;
		this.type = type;
		this.allowedReferences = null;
		this.inheritedMetadataBehaviors = new InheritedMetadataBehaviors(false, multivalue, false, false, false, false, false,
				false, false, false, false, true);
		this.defaultRequirement = false;
		this.dataEntry = null;
		this.accessRestriction = new MetadataAccessRestriction();

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

		this.recordMetadataValidators = null;
		this.structureFactory = null;
		this.enumClass = null;
		this.defaultValue = multivalue ? Collections.emptyList() : null;
	}

	public Metadata(String localCode, String code, String collection, String label, Boolean enabled,
			InheritedMetadataBehaviors inheritedMetadataBehaviors, MetadataValueType type,
			AllowedReferences allowedReferences, Boolean defaultRequirement, DataEntry dataEntry,
			Set<RecordMetadataValidator<?>> recordMetadataValidators, String dataStoreType,
			MetadataAccessRestriction accessRestriction, StructureFactory structureFactory, Class<? extends Enum<?>> enumClass,
			Object defaultValue) {
		super();

		this.inheritance = null;
		this.localCode = localCode;
		this.code = code;
		this.collection = collection;
		this.label = label;
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

	}

	public Metadata(Metadata inheritance, String label, boolean enabled, boolean defaultRequirement, String code,
			Set<RecordMetadataValidator<?>> recordMetadataValidators, Object defaultValue) {
		super();

		this.localCode = inheritance.getLocalCode();
		this.code = code;
		this.collection = inheritance.collection;
		this.inheritance = inheritance;
		this.label = label;
		this.enabled = enabled;
		this.type = inheritance.getType();
		this.allowedReferences = inheritance.getAllowedReferences();
		this.inheritedMetadataBehaviors = inheritance.getInheritedMetadataBehaviors();
		this.defaultRequirement = defaultRequirement;
		this.dataEntry = inheritance.getDataEntry();
		this.dataStoreType = inheritance.getDataStoreType();
		this.accessRestriction = inheritance.getAccessRestrictions();
		this.recordMetadataValidators = new HashSet<RecordMetadataValidator<?>>(inheritance.recordMetadataValidators);
		this.recordMetadataValidators.addAll(recordMetadataValidators);
		this.structureFactory = inheritance.structureFactory;
		this.enumClass = inheritance.enumClass;
		this.defaultValue = defaultValue;
	}

	public String getCode() {
		return code;
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getDataStoreCode() {
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

	public String getLabel() {
		return label;
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

	public boolean isWriteNullValues() {
		return getInheritedMetadataBehaviors().isWriteNullValues();
	}

	public boolean isSchemaAutocomplete() {
		return getInheritedMetadataBehaviors().isSchemaAutocomplete();
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dataEntry");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dataEntry", "recordMetadataValidators");
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

	public static Metadata newDummyMetadata(String schemaCode, String localCode, MetadataValueType type, boolean multivalue) {
		return new Metadata(schemaCode, localCode, type, multivalue);
	}

	public Metadata getSearchableMetadataWithLanguage(String language) {
		String schemaCode = code.replace("_" + localCode, "");
		return new Metadata(schemaCode, getDataStoreCode().replace("_s", "_t") + "_" + language, type, isMultivalue());
	}

}
