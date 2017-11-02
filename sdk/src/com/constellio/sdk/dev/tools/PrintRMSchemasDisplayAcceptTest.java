package com.constellio.sdk.dev.tools;

import static java.util.Arrays.asList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;

@MainTest
public class PrintRMSchemasDisplayAcceptTest extends ConstellioTest {

	private static List<String> restrictedTypes = asList(Collection.SCHEMA_TYPE, Event.SCHEMA_TYPE, WorkflowTask.SCHEMA_TYPE);

	private static List<String> restrictedMetadatasCode = asList("followers", "path");

	private static List<String> typesVisibleInSearch = asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, Task.SCHEMA_TYPE,
			ContainerRecord.SCHEMA_TYPE, StorageSpace.SCHEMA_TYPE);

	@Test
	@MainTestDefaultStart
	public void printSchemas()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule();

		System.out.println("_Généré avec com.constellio.sdk.dev.tools.PrintRMSchemasAcceptTest_\n\n");
		System.out.println("Utiliser http://yuml.me pour générer le diagramme.");

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		for (MetadataSchemaType type : getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaTypes()) {
			if (!restrictedTypes.contains(type.getCode())) {
				System.out.println("\n\n## " + type.getCode() + " '" + type.getLabel(Language.French) + "' ");

				for (MetadataSchema schema : type.getAllSchemas()) {
					SchemaDisplayConfig schemaDisplayConfig = schemasDisplayManager.getSchema(zeCollection, schema.getCode());

					System.out.println("\n### " + schema.getCode() + " '" + schema.getLabel(Language.French) + "' ");
					System.out.println("Display : " + StringUtils.join(schemaDisplayConfig.getDisplayMetadataCodes(), ", "));
					System.out.println("Form : " + StringUtils.join(schemaDisplayConfig.getFormMetadataCodes(), ", "));
					System.out.println("Search : " + StringUtils.join(schemaDisplayConfig.getSearchResultsMetadataCodes(), ", "));
					System.out.println("Table : " + StringUtils.join(schemaDisplayConfig.getTableMetadataCodes(), ", "));

					for (Metadata metadata : type.getAllMetadatas()) {
						if (!restrictedMetadatasCode.contains(metadata.getLocalCode())) {
							MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager
									.getMetadata(zeCollection, metadata.getCode());

							if (typesVisibleInSearch.contains(type.getCode())) {
								System.out.println(metadata.getCode() + " : " + metadataDisplayConfig.getInputType().name() + " "
										+ metadataDisplayConfig.getDisplayType().name()
										+ (metadataDisplayConfig.isVisibleInAdvancedSearch() ? " visibleInAdvancedSearch" : "")
										+ (metadataDisplayConfig.isHighlight() ? " highlighted" : "")
								);
							} else {
								System.out.println(metadata.getCode() + " : " + metadataDisplayConfig.getInputType().name() + " "
										+ metadataDisplayConfig.getDisplayType().name() + " "
								);
							}

						}
					}
				}

			}
		}

	}
}
