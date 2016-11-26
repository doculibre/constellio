package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.model.decommissioning.FoldersCertificateReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.FoldersCertificateReportModel.FoldersCertificateReportModel_Folder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

/**
 * Created by Patrick on 2016-01-15.
 */
public class FoldersCertificateReportWriterManualAcceptTest extends ReportBuilderTestFramework {
	FoldersCertificateReportModel model;

	RMSchemasRecordsServices rm;
	MetadataSchemaTypes types;
	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	DecommissioningService decommissioningService;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent()
		);

		searchServices = getModelLayerFactory().newSearchServices();
		decommissioningService = new DecommissioningService(zeCollection, getModelLayerFactory());
		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenBuildEmptyFoldersCertificateReportThenOk() {
		model = new FoldersCertificateReportModel();
		model.setCellBorder(true);
		model.setDestructionDate(TimeProvider.getLocalDate().toString());
		model.setCertificateCreationDate(TimeProvider.getLocalDate().toString());
		model.setHash("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=");
		build(new FoldersCertificateReportWriter(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildTestFoldersCertificateReportThenOk() {
		model = newTestCertificateReportModel();
		model.setDestructionDate(TimeProvider.getLocalDate().toString());
		model.setCertificateCreationDate(TimeProvider.getLocalDate().toString());
		model.setHash("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=");
		build(new FoldersCertificateReportWriter(model, getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildFoldersCertificateReportThenOk() {
		model = newCertificateReportModel();
		model.setDestructionDate(TimeProvider.getLocalDate().toString());
		model.setCertificateCreationDate(TimeProvider.getLocalDate().toString());
		model.setHash("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=");
		build(new FoldersCertificateReportWriter(model, getModelLayerFactory().getFoldersLocator()));
	}

	private FoldersCertificateReportModel newTestCertificateReportModel() {

		FoldersCertificateReportModel model = newCertificateReportModel();
		List<FoldersCertificateReportModel_Folder> foldersModel = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			FoldersCertificateReportModel_Folder folderModel = new FoldersCertificateReportModel_Folder();
			folderModel.setFolder("folder folder folder folder folder folder folder folder folder folder " + i);
			folderModel.setId("00000" + i);
			folderModel.setTitle(
					"Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title Folder title "
							+ i);
			folderModel.setRetentionRuleCode("ruleCode" + i);
			folderModel.setPrincipalCopyRetentionRule("888-3-d" + i);
			foldersModel.add(folderModel);
		}

		model.setFolders(foldersModel);

		return model;
	}

	private FoldersCertificateReportModel newCertificateReportModel() {

		FoldersCertificateReportModel model = new FoldersCertificateReportModel();

		DecommissioningList decommissioningList = rm.getDecommissioningList("list02");

		List<FoldersCertificateReportModel_Folder> foldersModel = new ArrayList<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition = from(types.getSchemaType(Folder.SCHEMA_TYPE))
				.where(Schemas.IDENTIFIER)
				.isIn(decommissioningList.getFolders());
		query.setCondition(condition);
		List<Record> documentsRecords = searchServices.search(query);
		List<Folder> folders = rm.wrapFolders(documentsRecords);

		for (Folder folder : folders) {
			FoldersCertificateReportModel_Folder folderModel = new FoldersCertificateReportModel_Folder();
			String parentFolder = "";
			try {
				parentFolder = rm.getFolder(folder.getParentFolder()).getTitle();
			} catch (Exception e) {
			}
			folderModel.setFolder(parentFolder);
			folderModel.setId(folder.getId());
			folderModel.setTitle(folder.getTitle());
			folderModel
					.setRetentionRuleCode(rm.getRetentionRule(folder.getRetentionRule()).getCode());
			folderModel.setPrincipalCopyRetentionRule(folder.getMainCopyRule().getCode());
			foldersModel.add(folderModel);

		}

		model.setFolders(foldersModel);

		return model;
	}

}