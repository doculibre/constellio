package com.constellio.sdk.dev.tools;

import com.constellio.model.entities.schemas.MetadataNetworkLink;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;

@MainTest
public class PrintRMMetadatasNetwork2AcceptTest extends ConstellioTest {

	private static List<String> restrictedMetadata = asList("manualTokens", "tokens",
			"deleted", "visibleInTrees", "path", "id", "allauthorizations", "allRemovedAuths", "detachedauthorizations");

	@Test
	@MainTestDefaultStart
	public void printMetadatas()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule();

		System.out.println("@startuml");

		SchemaUtils utils = new SchemaUtils();
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		for (MetadataNetworkLink link : types.getMetadataNetwork().getLinks()) {

			if (restrictedMetadata.contains(link.getFromMetadata().getLocalCode()) ||
				restrictedMetadata.contains(link.getToMetadata().getLocalCode())) {
				continue;
			}

			String from = link.getFromMetadata().getCode();
			String to = link.getToMetadata().getCode();

			int fromLevel = 1 + link.getLevel() + (link.getFromMetadata().isIncreasedDependencyLevel() ? 1 : 0);
			int toLevel = 1 + link.getLevel();

			if (fromLevel > 1) {
				from += "@" + fromLevel;
			}

			if (toLevel > 1) {
				to += "@" + toLevel;
			}

			System.out.println("[" + to + "]->" + "[" + from + "]");
		}

		System.out.println("@enduml");
	}
}
