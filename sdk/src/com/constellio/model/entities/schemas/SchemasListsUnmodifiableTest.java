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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.sdk.tests.ConstellioTest;

public class SchemasListsUnmodifiableTest extends ConstellioTest {

	@Mock Metadata metadata1;
	@Mock Metadata metadata2;
	@Mock MetadataSchema schema1;
	@Mock MetadataSchema schema2;
	@Mock MetadataSchema defaultSchema;

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingMetadatasListInSchemaThenExceptionThrown() {
		List<Metadata> metadatas = Arrays.asList(metadata1, metadata2);
		Set<RecordValidator> validators = new HashSet<RecordValidator>();
		MetadataSchema schema = new MetadataSchema("aCode", "aCode", "zeCollection", "aLabel", metadatas, false, validators,
				new ArrayList<Metadata>());

		schema.getMetadatas().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingSchemasListInSchemaTypeThenExceptionThrown() {
		List<MetadataSchema> schemas = Arrays.asList(schema1, schema2);
		MetadataSchemaType schemaType = new MetadataSchemaType("aCode", "zeCollection", "aLabel", schemas, defaultSchema, false,
				true);

		schemaType.getSchemas().clear();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void whenClearingAllowedSchemasListsInAllowedReferencesThenExceptionThrown() {
		Set<String> schemas = new HashSet<String>(Arrays.asList("aSchema", "anotherSchema"));
		AllowedReferences allowedReferences = new AllowedReferences(null, schemas);
		allowedReferences.getAllowedSchemas().add("yetAnotherSchema");
	}
}
