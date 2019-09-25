package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class RMDocumentMigrationTo8_0_1_2_AcceptanceTest extends ConstellioTest {

	private final static String STREAM_NAME = "RMDocumentMigrationTo8_0_1_2_AcceptanceTest";

	@Test
	public void givenSystemIn8_0_1_thenMigrated() throws Exception {
		givenSystemIn8_0_1();


		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		givenConfig(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, false);
		givenConfig(ConstellioEIMConfigs.ENABLE_THUMBNAIL_GENERATION, true);
		Thread.sleep(2000);
		getModelLayerFactory().newRecordServices().flush();

		ContentManager contentManager = new ContentManager(getModelLayerFactory());
		LogicalSearchQuery query = new LogicalSearchQuery(from(rm.document.schemaType())
				.where(Schemas.MARKED_FOR_PREVIEW_CONVERSION).isTrue());
		while (searchServices.hasResults(query)) {
			try {
				contentManager.convertPendingContentForPreview();
			} catch (Throwable t) {
				//Possible
			}
			getModelLayerFactory().newRecordServices().flush();
		}


		List<Document> documentsWithContent = rm.wrapDocuments(searchServices
				.search(new LogicalSearchQuery(from(rm.document.schemaType()).where(rm.document.content()).isNotNull())));

		ContentDao contentDao = getModelLayerFactory().getDataLayerFactory().getContentsDao();
		for (Document document : documentsWithContent) {
			String hash = document.getContent().getCurrentVersion().getHash();
			try (InputStream inputStream = contentDao.getContentInputStream(hash + ".thumbnail", STREAM_NAME)) {
				assertThat(inputStream).isNotNull();
			}
		}
	}

	private void givenSystemIn8_0_1() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_8_0_1_withThumbnailGenerationActivated.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
