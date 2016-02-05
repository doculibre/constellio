package com.constellio.sdk.dev.tools;

import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Test;

import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;

@MainTest
public class PrintSchemaTypesDiagramAcceptTest extends ConstellioTest {

	private static List<String> restrictedTypes = asList(Collection.SCHEMA_TYPE, Event.SCHEMA_TYPE, WorkflowTask.SCHEMA_TYPE,
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
