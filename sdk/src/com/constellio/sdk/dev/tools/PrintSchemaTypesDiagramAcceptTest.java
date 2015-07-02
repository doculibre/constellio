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
package com.constellio.sdk.dev.tools;

import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Test;

import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.Task;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;

@MainTest
public class PrintSchemaTypesDiagramAcceptTest extends ConstellioTest {

	private static List<String> restrictedTypes = asList(Collection.SCHEMA_TYPE, Event.SCHEMA_TYPE, Task.SCHEMA_TYPE,
			User.SCHEMA_TYPE, Group.SCHEMA_TYPE);

	@Test
	@MainTestDefaultStart
	public void printSchemas()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule();

		for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaTypes()) {
			if (!restrictedTypes.contains(type.getCode())) {
				System.out.println("[" + type.getCode() + "]");
				for (Metadata metadata : type.getAllMetadatas().onlyWithType(MetadataValueType.REFERENCE).onlyManuals()) {
					String referencedType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();

					String cardinality;
					if (metadata.isMultivalue()) {
						if (metadata.isDefaultRequirement()) {
							cardinality = "1..*";
						} else {
							cardinality = "0..*";
						}
					} else {
						if (metadata.isDefaultRequirement()) {
							cardinality = "1";
						} else {
							cardinality = "0..1";
						}
					}

					if (!restrictedTypes.contains(referencedType)) {
						System.out.println("[" + type.getCode() + "]->" + cardinality + "[" + referencedType + "]");
					}
				}
			}
		}

	}

}
