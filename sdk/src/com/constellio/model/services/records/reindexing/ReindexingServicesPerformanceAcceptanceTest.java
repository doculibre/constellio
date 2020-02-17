package com.constellio.model.services.records.reindexing;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.BulkRecordTransactionHandler;
import com.constellio.model.services.records.BulkRecordTransactionHandlerOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.annotations.UiTest;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@SlowTest
@InDevelopmentTest
@UiTest
public class ReindexingServicesPerformanceAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void name() {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records));

		getModelLayerFactory().getRecordsCaches().disableVolatileCache();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();

		BulkRecordTransactionHandler handler = new BulkRecordTransactionHandler(recordServices, SDK_STREAM, new BulkRecordTransactionHandlerOptions()
				.setTransactionOptions(new RecordUpdateOptions().setRecordsFlushing(RecordsFlushing.LATER())));

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getDefaultSchema(Document.SCHEMA_TYPE).getMetadata(Schemas.TITLE_CODE).setSortable(false);
				types.getDefaultSchema(Document.SCHEMA_TYPE).getMetadata(Schemas.CAPTION.getLocalCode()).setSortable(false);

				types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Schemas.TITLE_CODE).setSortable(false);
				types.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Schemas.CAPTION.getLocalCode()).setSortable(false);
			}
		});

		final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		Thread thread = new Thread(() -> {
			List<String> folderIds = new ArrayList<>();

			for (int i = 0; i < 2_000_000; i++) {
				System.out.println(i + " / 2 000 0000");
				Folder folder = rm.newFolder().setTitle("Folder " + i).setCategoryEntered(records.categoryId_X13)
						.setRetentionRuleEntered(records.ruleId_1).setAdministrativeUnitEntered(records.unitId_10a)
						.setOpenDate(LocalDate.now());
				Document document = rm.newDocument().setTitle("Document " + i).setFolder(folder);
				folderIds.add(folder.getId());
				handler.append(asList(folder.getWrappedRecord(), document.getWrappedRecord()));

			}

			recordServices.flushRecords();

			ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
			reindexingServices.reindexCollections(ReindexationMode.RECALCULATE_AND_REWRITE);
		});
		thread.start();
		startApplication();
		waitUntilICloseTheBrowsers();
		try {
			thread.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
