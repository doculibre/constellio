package com.constellio.sdk.dev.tools;

import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Test;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;

@MainTest
public class PrintRMSchemasAcceptTest extends ConstellioTest {

	private static List<String> restrictedTypes = asList(Collection.SCHEMA_TYPE, Event.SCHEMA_TYPE, WorkflowTask.SCHEMA_TYPE);

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
				System.out.println("\n\n## " + type.getCode() + " '" + type.getLabel(Language.French) + "' ");
				for (Metadata metadata : type.getAllMetadatas().onlyManuals()) {
					if (!restrictedMetadatasCode.contains(metadata.getLocalCode())) {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append(metadata.getLocalCode() + " '" + metadata.getLabel(Language.French) + "' : ");
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
