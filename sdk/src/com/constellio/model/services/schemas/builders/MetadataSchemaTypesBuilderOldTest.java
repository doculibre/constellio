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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataSchemaTypesBuilderOldTest extends ConstellioTest {

	static final String UNDERSCORE = "_";
	static final String DEFAULT = "default";
	static final String INEXISTENT_CODE = "inexistentCode";
	static final String FOLDER = "folder";
	static int VERSION = 1;

	@Mock DataStoreTypesFactory typesFactory;
	MetadataSchemaTypesBuilder typesBuilder;
	MetadataSchemaTypesBuilder typesBuilder2;
	MetadataSchemaTypeBuilder folderTypeBuilder;
	MetadataSchemaTypeBuilder newTypeBuilder;
	MetadataSchemaTypes schemaTypes;
	MetadataSchemaBuilder employeeFolderSchemaBuilder;
	MetadataBuilder employeeName;
	@Mock TaxonomiesManager taxonomiesManager;

	@Before
	public void setup()
			throws Exception {
		typesBuilder = MetadataSchemaTypesBuilder.createWithVersion(zeCollection, VERSION);
		folderTypeBuilder = typesBuilder.createNewSchemaType(FOLDER);
		schemaTypes = typesBuilder.build(typesFactory, taxonomiesManager);
		typesBuilder2 = MetadataSchemaTypesBuilder.modify(schemaTypes);
		folderTypeBuilder.getDefaultSchema().create("zetitle");
		employeeFolderSchemaBuilder = folderTypeBuilder.createCustomSchema("employeeFolder");
		employeeName = employeeFolderSchemaBuilder.create("employeeName");
	}

	@Test
	public void whenCreateWithVersionThenItsIsCreatedWithVersion()
			throws Exception {

		assertThat(typesBuilder.getVersion()).isEqualTo(VERSION);
	}

	@Test
	public void whenCreateNewSchemaTypeThenItIsCanBeRetrieved()
			throws Exception {

		assertThat(typesBuilder.getSchemaType(FOLDER)).isEqualTo(folderTypeBuilder);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.SchemaTypeExistent.class)
	public void whenCreateNewSchemaTypeWithAExistentSchemaThenThrowException()
			throws Exception {

		folderTypeBuilder = typesBuilder.createNewSchemaType(FOLDER);
	}

	@Test(expected = MetadataSchemaTypesBuilderRuntimeException.NoSuchSchemaType.class)
	public void whenGetAInexistentSchemaTypeThenThrowException()
			throws Exception {

		folderTypeBuilder = typesBuilder.getSchemaType(INEXISTENT_CODE);
	}

	@Test
	public void whenGetOrCreateSchemaTypeThenItIsCanBeRetrieved()
			throws Exception {

		newTypeBuilder = typesBuilder.getOrCreateNewSchemaType(INEXISTENT_CODE);

		assertThat(typesBuilder.getSchemaType(FOLDER)).isEqualTo(folderTypeBuilder);
		assertThat(typesBuilder.getSchemaType(INEXISTENT_CODE)).isEqualTo(newTypeBuilder);
	}

	@Test
	public void whenModifyThenNewSchemaTypesBuilderIsReturned()
			throws Exception {

		assertThat(typesBuilder2.getVersion()).isEqualTo(schemaTypes.getVersion());
	}

	@Test
	public void whenBuildModifySchemaTypesThenNewSchemaTypesIsBuildWithNewVersion()
			throws Exception {

		assertThat(typesBuilder2.build(typesFactory, taxonomiesManager).getVersion()).isEqualTo(schemaTypes.getVersion() + 1);
	}

	@Test
	public void whenGetSchemaThenItIsReturned() {

		assertThat(typesBuilder.getSchema(employeeFolderSchemaBuilder.getCode())).isNotNull();
	}

}
