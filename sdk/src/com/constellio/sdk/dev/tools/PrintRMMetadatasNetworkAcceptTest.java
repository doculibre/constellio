package com.constellio.sdk.dev.tools;

import com.constellio.model.entities.records.wrappers.*;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@MainTest
public class PrintRMMetadatasNetworkAcceptTest extends ConstellioTest {

	private static List<String> restrictedTypes = asList(Collection.SCHEMA_TYPE, Event.SCHEMA_TYPE, WorkflowTask.SCHEMA_TYPE,
			User.SCHEMA_TYPE, Group.SCHEMA_TYPE);

	@Test
	@MainTestDefaultStart
	public void printMetadatas()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule();

		System.out.println("@startuml");

		SchemaUtils utils = new SchemaUtils();
		for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaTypes()) {

			boolean hasCalculator = false;

			if (!restrictedTypes.contains(type.getCode())) {
				for (Metadata metadata : type.getAllMetadatas().onlyNotGlobals()) {

					if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {

						if (!hasCalculator) {
							System.out.println("namespace " + type.getCode() + "{");
							hasCalculator = true;
						}

						Set<String> localDependencies = utils.getLocalDependencies(metadata, type.getAllMetadatas());

						System.out.println("class " + metadata.getLocalCode() + " {");
						for (String localDependency : localDependencies) {
							if (localDependency != null) {
								System.out.println("-" + localDependency);
							}
						}
						System.out.println("}");
					}

				}
			}
			if (hasCalculator) {
				System.out.println("}");
			}
		}

		System.out.println("@enduml");
	}
}
