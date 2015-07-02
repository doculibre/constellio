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
import com.constellio.model.entities.records.wrappers.Task;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;

@MainTest
public class PrintRMSchemasAcceptTest extends ConstellioTest {

	private static List<String> restrictedTypes = asList(Collection.SCHEMA_TYPE, Event.SCHEMA_TYPE, Task.SCHEMA_TYPE);

	private static List<String> restrictedMetadatasCode = asList("followers", "id", "schema", "legacyIdentifier",
			"removedauthorizations", "detachedauthorizations", "authorizations", "deleted");

	@Test
	@MainTestDefaultStart
	public void printSchemas()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule();

		System.out.println("_Généré avec com.constellio.sdk.dev.tools.PrintRMSchemasAcceptTest_\n\n");
		System.out.println("Utiliser http://yuml.me pour générer le diagramme.");

		for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaTypes()) {
			if (!restrictedTypes.contains(type.getCode())) {
				System.out.println("\n\n## " + type.getCode() + " '" + type.getLabel() + "' ");
				for (Metadata metadata : type.getAllMetadatas().onlyManuals()) {
					if (!restrictedMetadatasCode.contains(metadata.getLocalCode())) {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append(metadata.getLocalCode() + " '" + metadata.getLabel() + "' : ");
						stringBuilder.append(metadata.getType().name().toLowerCase());

						if (metadata.isMultivalue()) {
							stringBuilder.append(" multivalued");
						}

						if (metadata.getType() == MetadataValueType.REFERENCE) {
							stringBuilder.append(" to " + metadata.getAllowedReferences().getTypeWithAllowedSchemas());
						}

						if (metadata.getEnumClass() != null) {
							stringBuilder.append(" of type " + metadata.getEnumClass().getSimpleName());
						}
						System.out.println("* " + stringBuilder.toString());
					}
				}
			}
		}

	}
}
