package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

public class RecordIdListBasedOnIncludeExcludeMetadatasBuilderAcceptanceTest extends ConstellioTest {

	private static final String INCLUDED_IN_SIP_EXPORT_METADATA = "USRincludedInSIPExport";
	private static final String EXCLUDED_FROM_SIP_EXPORT_METADATA = "USRexcludedFromSIPExport";

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private RMSchemasRecordsServices rm;
	private RecordIdListBasedOnIncludeExcludeMetadatasBuilder rmSIPExportService;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule()
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		rmSIPExportService = new RecordIdListBasedOnIncludeExcludeMetadatasBuilder(getAppLayerFactory());
	}

	@Test
	public void givenFolderIncldedInSIPExportWhenGetIncludedIdsThenParentAndChildAreIncluded()
			throws RecordServicesException {
		createSIPExportMetadatas();
		Transaction tx = new Transaction();
		Folder parentFolder = createFolder("zeParentFolderId", null, false, false);
		Folder folder = createFolder("zeFolderId", parentFolder.getId(), true, false);
		Folder childFolder = createFolder("zeChildFolderId", folder.getId(), false, false);
		tx.addAll(parentFolder, folder, childFolder);
		rm.executeTransaction(tx);

		assertThat(rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA))
				.containsExactly("zeParentFolderId", "zeFolderId", "zeChildFolderId");
	}

	@Test
	public void givenFolderIncldedInSIPExportWhenGetIncludedIdsThenParentIncludedNoMatterTheStatus()
			throws RecordServicesException {
		createSIPExportMetadatas();
		Transaction tx = new Transaction();
		Folder parentFolder = createFolder("zeParentFolderId", null, false, true);
		Folder folder = createFolder("zeFolderId", parentFolder.getId(), true, false);
		Folder childFolder = createFolder("zeChildFolderId", folder.getId(), false, false);
		tx.addAll(parentFolder, folder, childFolder);
		rm.executeTransaction(tx);

		assertThat(rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA))
				.containsExactly("zeParentFolderId", "zeFolderId", "zeChildFolderId");
	}

	@Test
	public void givenFolderIncldedInSIPExportWhenGetIncludedIdsThenOnlyParentIAndChildArencluded()
			throws RecordServicesException {
		createSIPExportMetadatas();
		Transaction tx = new Transaction();
		Folder parentFolder = createFolder("zeParentFolderId", null, false, true);
		Document documentInParentFolder = createDocument("zeDocumentInParentFolderId", parentFolder.getId(), false, true);
		Folder folder = createFolder("zeFolderId", parentFolder.getId(), true, false);
		Folder childFolder = createFolder("zeChildFolderId", folder.getId(), false, false);
		Document documentInChildFolder = createDocument("zeDocumentInChildFolderId", childFolder.getId(), false, false);
		tx.addAll(parentFolder, documentInParentFolder, folder, childFolder, documentInChildFolder);
		rm.executeTransaction(tx);

		assertThat(rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA))
				.containsExactly("zeParentFolderId", "zeFolderId", "zeChildFolderId");
	}

	@Test
	public void givenFolderIncldedInSIPExportWithExcludedChildWhenGetIncludedIdsThenOnlyParentIncludedNoMatterTheStatus()
			throws RecordServicesException {
		createSIPExportMetadatas();
		Transaction tx = new Transaction();
		Folder parentFolder = createFolder("zeParentFolderId", null, false, true);
		Document documentInParentFolder = createDocument("zeDocumentInParentFolderId", parentFolder.getId(), false, true);
		Folder folder = createFolder("zeFolderId", parentFolder.getId(), true, false);
		Folder childFolder = createFolder("zeChildFolderId", folder.getId(), false, true);
		Document documentInChildFolder = createDocument("zeDocumentInChildFolderId", childFolder.getId(), false, false);
		tx.addAll(parentFolder, documentInParentFolder, folder, childFolder, documentInChildFolder);
		rm.executeTransaction(tx);

		assertThat(rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA))
				.containsExactly("zeParentFolderId", "zeFolderId");
	}


	private Folder createFolder(String id, String parentFolder, boolean includedInSipExport,
								boolean excludedFromSipExport) {
		Folder folder = rm.newFolderWithId(id).setOpenDate(LocalDate.now()).setTitle("Ze folder")
				.set(INCLUDED_IN_SIP_EXPORT_METADATA, includedInSipExport).set(EXCLUDED_FROM_SIP_EXPORT_METADATA, excludedFromSipExport);
		if (parentFolder == null) {
			folder.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X13)
					.setRetentionRuleEntered(records.ruleId_1);
		} else {
			folder.setParentFolder(parentFolder);
		}
		return folder;
	}

	private Document createDocument(String id, String folder, boolean includedInSipExport,
									boolean excludedFromSipExport) {
		Document document = rm.newDocumentWithId(id).setTitle("Ze document").setFolder(folder)
				.set(INCLUDED_IN_SIP_EXPORT_METADATA, includedInSipExport).set(EXCLUDED_FROM_SIP_EXPORT_METADATA, excludedFromSipExport);
		return document;
	}

	private void createSIPExportMetadatas() {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(Folder.DEFAULT_SCHEMA).create(INCLUDED_IN_SIP_EXPORT_METADATA).setType(MetadataValueType.BOOLEAN);
				types.getSchema(Document.DEFAULT_SCHEMA).create(INCLUDED_IN_SIP_EXPORT_METADATA).setType(MetadataValueType.BOOLEAN);
				types.getSchema(Folder.DEFAULT_SCHEMA).create(EXCLUDED_FROM_SIP_EXPORT_METADATA).setType(MetadataValueType.BOOLEAN);
				types.getSchema(Document.DEFAULT_SCHEMA).create(EXCLUDED_FROM_SIP_EXPORT_METADATA).setType(MetadataValueType.BOOLEAN);
			}
		});
	}

	@Test
	public void givenAFolderIncludedInSIPExportWithDocumentNotExcludedFromSIPExportAndFolderExcludedFromSIPExportWhenExportAllFoldersAndDocumentThenExporParentFolderAndDocument()
			throws IOException, RecordServicesException {
		File tempFolder = newTempFolder();
		createSIPExportMetadatas();

		Transaction tx = new Transaction();
		tx.add(records.getFolder_A01().set(INCLUDED_IN_SIP_EXPORT_METADATA, true));
		tx.add(records.getFolder_A04().set(INCLUDED_IN_SIP_EXPORT_METADATA, true));
		rm.executeTransaction(tx);

		SearchServices searchServices = getAppLayerFactory().getModelLayerFactory().newSearchServices();
		List<String> documentIds = searchServices.searchRecordIds(new LogicalSearchQuery(from(rm.document.schemaType()).where(rm.document.folder()).isIn(asList(records.getFolder_A01().getId(), records.getFolder_A01().getId()))));
		StringBuilder stringBuilder = new StringBuilder();
		int size = documentIds.size();
		int index = 1;
		for (String documentId : documentIds) {
			stringBuilder.append(documentId);
			if (index < size) {
				stringBuilder.append(", ");
			}
		}

		RMCollectionExportSIPBuilder builder = new RMCollectionExportSIPBuilder(zeCollection, getAppLayerFactory(), tempFolder);
		List<String> includedIds = rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA);


		builder.exportAllFoldersAndDocuments(new ProgressInfo(), rmSIPExportService.getFilter(includedIds));

		assertThat(new File(tempFolder, "info").list()).containsOnly("failedFolderExport.txt", "exportedFolders.txt", "exportedDocuments.txt");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "failedFolderExport.txt"))).isEmpty();

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedFolders.txt"))).contains("A01, A04");

		assertThat(contentOf(new File(tempFolder, "info" + File.separator + "exportedDocuments.txt"))).contains(stringBuilder.toString());
	}

	@Test
	public void whenFolderHasIncludeAndExcludeThenAllHierarchyExcluded()
			throws RecordServicesException {
		createSIPExportMetadatas();
		Transaction tx = new Transaction();
		Folder parentFolder = createFolder("zeParentFolderId", null, false, false);
		Folder folder = createFolder("zeFolderId", parentFolder.getId(), true, true);
		Folder childFolder = createFolder("zeChildFolderId", folder.getId(), false, false);
		tx.addAll(parentFolder, folder, childFolder);
		rm.executeTransaction(tx);

		assertThat(rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA))
				.isEmpty();
	}

	@Test
	public void whenSubFolderHasIncludeAndExcludeThenAllHierarchyExcluded()
			throws RecordServicesException {
		createSIPExportMetadatas();
		Transaction tx = new Transaction();
		Folder parentFolder = createFolder("zeParentFolderId", null, true, true);
		Folder folder = createFolder("zeFolderId", parentFolder.getId(), false, false);
		Folder childFolder = createFolder("zeChildFolderId", folder.getId(), false, false);
		tx.addAll(parentFolder, folder, childFolder);
		rm.executeTransaction(tx);

		assertThat(rmSIPExportService.getIncludedIds(zeCollection, INCLUDED_IN_SIP_EXPORT_METADATA, INCLUDED_IN_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA, EXCLUDED_FROM_SIP_EXPORT_METADATA))
				.isEmpty();
	}

}
