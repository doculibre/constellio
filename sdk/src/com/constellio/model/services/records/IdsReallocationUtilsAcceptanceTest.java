package com.constellio.model.services.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.model.services.records.IdsReallocationUtils.TypeWithIdsToReallocate;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class IdsReallocationUtilsAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void givenDemoThatWithALotOfSpecialIdsThenBuildAccurateReport() throws FileNotFoundException {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());

		List<TypeWithIdsToReallocate> results = IdsReallocationUtils.reallocateScanningSolr(getModelLayerFactory());
		sort(results);
		assertThat(results).extracting("schemaType.code", "idsToReallocateToSequential.size").containsOnly(
				tuple("administrativeUnit", 12), tuple("category", 14), tuple("containerRecord", 19),
				tuple("ddvContainerRecordType", 1), tuple("ddvDocumentType", 10), tuple("decommissioningList", 24),
				tuple("document", 5), tuple("folder", 105), tuple("retentionRule", 5), tuple("storageSpace", 6),
				tuple("uniformSubdivision", 3));
		assertThat(results.get(0).getIdsToReallocateToSequential()).containsOnly("unitId_10", "unitId_10a",
				"unitId_11", "unitId_11b", "unitId_12", "unitId_12b", "unitId_12c", "unitId_20", "unitId_20d",
				"unitId_20e", "unitId_30", "unitId_30c");
		assertThat(results.get(0).getSequentialIdsToReallocateToUUID()).isEmpty();
		assertThat(results.get(0).getOldAndNewIdMapping().keySet()).containsOnly("unitId_10", "unitId_10a",
				"unitId_11", "unitId_11b", "unitId_12", "unitId_12b", "unitId_12c", "unitId_20", "unitId_20d",
				"unitId_20e", "unitId_30", "unitId_30c");

		File file = new File(newTempFolder(), "file.csv");
		IdsReallocationUtils.writeCSVFile(results, file);

		List<TypeWithIdsToReallocate> results2 = IdsReallocationUtils.readCSVFile(getModelLayerFactory(), file);
		sort(results2);
		assertThat(results2).extracting("schemaType.code", "idsToReallocateToSequential.size").containsOnly(
				tuple("administrativeUnit", 12), tuple("category", 14), tuple("containerRecord", 19),
				tuple("ddvContainerRecordType", 1), tuple("ddvDocumentType", 10), tuple("decommissioningList", 24),
				tuple("document", 5), tuple("folder", 105), tuple("retentionRule", 5), tuple("storageSpace", 6),
				tuple("uniformSubdivision", 3));
		assertThat(results2.get(0).getIdsToReallocateToSequential()).containsOnly(results.get(0).getIdsToReallocateToSequential().toArray(new String[0]));
		assertThat(results2.get(0).getOldAndNewIdMapping()).isEqualTo(results.get(0).getOldAndNewIdMapping());


	}

	protected void sort(List<TypeWithIdsToReallocate> results) {
		Collections.sort(results, new Comparator<TypeWithIdsToReallocate>() {
			@Override
			public int compare(TypeWithIdsToReallocate o1, TypeWithIdsToReallocate o2) {

				int collection = o1.getSchemaType().getCollection().compareTo(o2.getSchemaType().getCollection());

				return collection == 0 ? o1.getSchemaType().getCode().compareTo(o2.getSchemaType().getCode()) : collection;
			}
		});
	}
}
