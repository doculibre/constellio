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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataBuilderTest extends ConstellioTest {

	static final String COLLECTION = "zeUltimateCollection";
	static final String CODE_DEFAULT_METADATA = "codeMetadata";
	static final String DEFAULT_SCHEMA_CODE = "codeSchema_default";
	static final String CUSTOM_SCHEMA_CODE = "codeSchema_codeSchema";
	static final MetadataValueType VALUE_TYPE = MetadataValueType.NUMBER;
	static final boolean ENABLE = true;
	static final boolean DISABLE = false;

	@Mock MetadataSchemaTypesBuilder typesBuilder;
	@Mock TaxonomiesManager taxonomiesManager;
	DataStoreTypesFactory typesFactory = new FakeDataStoreTypeFactory();
	MetadataBuilder metadataWithInheritanceBuilder, metadataWithoutInheritanceBuilder, inheritedMetadataBuilder;
	MetadataBuilder anotherSchemaMetadataBuilder, referenceOtherSchemaMetadataBuilder;
	Metadata metadataWithInheritance, metadataWithoutInheritance, inheritedMetadata;

	MetadataSchemaTypeBuilder schemaTypeBuilder, anotherSchemaTypeBuilder;
	MetadataSchemaBuilder schemaBuilder, anotherSchemaBuilder;

	String anotherSchemaTypeCompleteCode;
	String anotherSchemaCompleteCode;
	String anotherSchemaMetadataCompleteCode;

	@Before
	public void setup() {
		when(typesBuilder.getSchemaType(anyString())).thenThrow(NoSuchSchemaType.class);
		schemaTypeBuilder = MetadataSchemaTypeBuilder.createNewSchemaType(COLLECTION, "codeSchema", typesBuilder);
		schemaBuilder = schemaTypeBuilder.getDefaultSchema();

		anotherSchemaTypeBuilder = MetadataSchemaTypeBuilder.createNewSchemaType(COLLECTION, "anotherSchemaType", typesBuilder);
		anotherSchemaBuilder = anotherSchemaTypeBuilder.getDefaultSchema();
		anotherSchemaMetadataBuilder = MetadataBuilder.createMetadataWithoutInheritance("anotherSchemaMetadata",
				anotherSchemaBuilder).setType(MetadataValueType.STRING);

		anotherSchemaTypeCompleteCode = anotherSchemaTypeBuilder.getCode();
		anotherSchemaCompleteCode = anotherSchemaBuilder.getCode();
		anotherSchemaMetadataCompleteCode = anotherSchemaMetadataBuilder.getCode();

		metadataWithoutInheritanceBuilder = MetadataBuilder.createMetadataWithoutInheritance(CODE_DEFAULT_METADATA,
				schemaBuilder);
		inheritedMetadataBuilder = metadataWithoutInheritanceBuilder;
		metadataWithInheritanceBuilder = MetadataBuilder.createCustomMetadataFromDefault(inheritedMetadataBuilder, "codeSchema");

		referenceOtherSchemaMetadataBuilder = MetadataBuilder.createMetadataWithoutInheritance("ref", schemaBuilder);
		referenceOtherSchemaMetadataBuilder.defineReferences().set(anotherSchemaTypeBuilder);

	}

	protected void build() {
		metadataWithoutInheritance = metadataWithoutInheritanceBuilder.buildWithoutInheritance(typesFactory, taxonomiesManager);
		inheritedMetadata = metadataWithoutInheritance;
		metadataWithInheritance = metadataWithInheritanceBuilder.buildWithInheritance(inheritedMetadata);
	}

	protected void buildAndModify() {
		Metadata inheritedMetadata = metadataWithoutInheritanceBuilder.buildWithoutInheritance(typesFactory, taxonomiesManager);
		Metadata metadataWithInheritance = metadataWithInheritanceBuilder.buildWithInheritance(inheritedMetadata);
		metadataWithoutInheritanceBuilder = MetadataBuilder.modifyMetadataWithoutInheritance(inheritedMetadata);
		inheritedMetadataBuilder = metadataWithoutInheritanceBuilder;
		metadataWithInheritanceBuilder = MetadataBuilder.modifyMetadataWithInheritance(metadataWithInheritance,
				inheritedMetadataBuilder);
	}

}
