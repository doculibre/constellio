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
package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.records.calculators.UserTitleCalculator;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.schemas.calculators.AllAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.InheritedAuthorizationsCalculator;
import com.constellio.model.services.schemas.calculators.ParentPathCalculator;
import com.constellio.model.services.schemas.calculators.PathCalculator;
import com.constellio.model.services.schemas.calculators.PrincipalPathCalculator;
import com.constellio.model.services.schemas.calculators.TokensCalculator;

public class CommonMetadatasBuilder {

	public void addCommonMetadatasToNewSchema(MetadataSchemaBuilder defaultSchema, MetadataSchemaTypesBuilder types) {

		String typeCode = new SchemaUtils().getSchemaTypeCode(defaultSchema.getCode());
		boolean isCollectionUserOrGroupSchema = typeCode.equals(Collection.SCHEMA_TYPE) || typeCode.equals(User.SCHEMA_TYPE)
				|| typeCode.equals(Group.SCHEMA_TYPE);

		addIdMetadata(defaultSchema);
		addSchemaCodeMetadata(defaultSchema);
		addPathMetadata(defaultSchema);
		addParentPathMetadata(defaultSchema);
		addAuthorizationsMetadata(defaultSchema);
		addRemovedAuthorizationsMetadata(defaultSchema);
		addInheritedAuthorizationsMetadata(defaultSchema);
		addDetachedAuthorizationsMetadata(defaultSchema);
		addAllAuthorizationsMetadata(defaultSchema);
		addTokensMetadata(defaultSchema);
		addLogicallyDeletedStatus(defaultSchema);
		addPrincipalPathMetadata(defaultSchema);

		try {
			if (!isCollectionUserOrGroupSchema) {
				MetadataSchemaTypeBuilder userSchemaType = types.getSchemaType(User.SCHEMA_TYPE);
				addCreatedBy(defaultSchema, userSchemaType);
				addModifiedBy(defaultSchema, userSchemaType);
			}
		} catch (NoSuchSchemaType e) {
			// OK
		}
		addCreatedOn(defaultSchema);
		addModifiedOn(defaultSchema);
		addTitle(defaultSchema);
		addFollowers(defaultSchema);
		addPreviousIdMetadata(defaultSchema);
	}

	private void addPreviousIdMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder previousSystemId = defaultSchema.createSystemReserved("legacyIdentifier");
		previousSystemId.setUnmodifiable(true).setUniqueValue(true).setDefaultRequirement(true)
				.setSearchable(true).setType(MetadataValueType.STRING);
	}

	private void addTitle(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder title = defaultSchema.createUndeletable("title").setType(MetadataValueType.STRING).setSearchable(true)
				.setSchemaAutocomplete(true);
		if (defaultSchema.getCode().equals(User.DEFAULT_SCHEMA)) {
			title.defineDataEntry().asCalculated(UserTitleCalculator.class);
		}
	}

	private void addFollowers(MetadataSchemaBuilder defaultSchema) {
		defaultSchema.createSystemReserved("followers").setType(MetadataValueType.STRING).setMultivalue(true).setSearchable(true);
	}

	private void addModifiedOn(MetadataSchemaBuilder defaultSchema) {
		defaultSchema.createSystemReserved("modifiedOn").setType(MetadataValueType.DATE_TIME).setSortable(true);
	}

	private void addCreatedOn(MetadataSchemaBuilder defaultSchema) {
		defaultSchema.createSystemReserved("createdOn").setType(MetadataValueType.DATE_TIME).setSortable(true);
	}

	private void addModifiedBy(MetadataSchemaBuilder defaultSchema, MetadataSchemaTypeBuilder userSchemaType) {
		defaultSchema.createSystemReserved("modifiedBy").setType(MetadataValueType.REFERENCE).defineReferencesTo(userSchemaType);
	}

	private void addCreatedBy(MetadataSchemaBuilder defaultSchema, MetadataSchemaTypeBuilder userSchemaType) {
		defaultSchema.createSystemReserved("createdBy").setType(MetadataValueType.REFERENCE).defineReferencesTo(userSchemaType);
	}

	void addSchemaCodeMetadata(MetadataSchemaBuilder defaultSchema) {

		MetadataBuilder schemaCode = defaultSchema.createSystemReserved("schema");
		schemaCode.setUniqueValue(true).setDefaultRequirement(true).setType(MetadataValueType.STRING);
	}

	void addIdMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder id = defaultSchema.createSystemReserved("id");
		id.setUnmodifiable(true).setUniqueValue(true).setDefaultRequirement(true).setSearchable(true)
				.setType(MetadataValueType.STRING).setSortable(true);
	}

	void addPathMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder path = defaultSchema.createSystemReserved("path");
		path.setType(MetadataValueType.STRING).setMultivalue(true).defineDataEntry()
				.asCalculated(PathCalculator.class);
	}

	void addParentPathMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder path = defaultSchema.createSystemReserved("parentpath");
		path.setType(MetadataValueType.STRING).setMultivalue(true).defineDataEntry()
				.asCalculated(ParentPathCalculator.class);
	}

	void addAuthorizationsMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("authorizations");
		metadata.setMultivalue(true).setType(MetadataValueType.STRING);
	}

	void addPrincipalPathMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("principalpath");
		metadata.setType(MetadataValueType.STRING).defineDataEntry()
				.asCalculated(PrincipalPathCalculator.class);
	}

	void addRemovedAuthorizationsMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("removedauthorizations");
		metadata.setMultivalue(true).setType(MetadataValueType.STRING);
	}

	void addInheritedAuthorizationsMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("inheritedauthorizations");
		metadata.setSystemReserved(true).setEnabled(true).setType(MetadataValueType.STRING)
				.setMultivalue(true);

		if (!defaultSchema.getSchemaTypeBuilder().getCode().equals(Group.SCHEMA_TYPE)) {
			metadata.defineDataEntry().asCalculated(InheritedAuthorizationsCalculator.class);
		}
	}

	void addDetachedAuthorizationsMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("detachedauthorizations");
		metadata.setSystemReserved(true).setEnabled(true).setType(MetadataValueType.BOOLEAN);
	}

	void addAllAuthorizationsMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("allauthorizations");
		metadata.setSystemReserved(true).setEnabled(true).setType(MetadataValueType.STRING)
				.setMultivalue(true).defineDataEntry().asCalculated(AllAuthorizationsCalculator.class);
	}

	void addTokensMetadata(MetadataSchemaBuilder defaultSchema) {
		MetadataBuilder metadata = defaultSchema.createSystemReserved("tokens");
		metadata.setSystemReserved(true).setEnabled(true).setType(MetadataValueType.STRING)
				.setMultivalue(true).defineDataEntry().asCalculated(TokensCalculator.class);
	}

	void addLogicallyDeletedStatus(MetadataSchemaBuilder defaultSchema) {
		defaultSchema.createSystemReserved("deleted").setType(MetadataValueType.BOOLEAN);
	}

}
