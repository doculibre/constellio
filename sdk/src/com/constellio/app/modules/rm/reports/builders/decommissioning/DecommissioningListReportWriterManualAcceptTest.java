package com.constellio.app.modules.rm.reports.builders.decommissioning;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListReportModel.DecommissioningListReportModel_Folder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class DecommissioningListReportWriterManualAcceptTest extends ReportBuilderTestFramework {

	DecommissioningListReportModel model;

	RMSchemasRecordsServices rm;
	MetadataSchemaTypes types;
	RMTestRecords records = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records).withFoldersAndContainersOfEveryStatus()
		);

		types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenBuildEmptyDecommissioningListReportThenOk() {
		model = new DecommissioningListReportModel();
		build(new DecommissioningListReportWriter(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDecommissioningListReportThenOk() {
		model = newDecommissioningListReportModel();
		build(new DecommissioningListReportWriter(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	private DecommissioningListReportModel newDecommissioningListReportModel() {

		DecommissioningListReportModel model = new DecommissioningListReportModel();

		Folder folderA01 = rm.getFolder(records.folder_A01);
		Folder folderA02 = rm.getFolder(records.folder_A02);
		Folder folderA03 = rm.getFolder(records.folder_A03);
		List<Folder> folders = new ArrayList<>();
		List<DecommissioningListReportModel_Folder> foldersModel = new ArrayList<>();
		folders.add(folderA01);
		folders.add(folderA02);
		folders.add(folderA03);
		for (Folder folder : folders) {
			Category category = rm.getCategory(folder.getCategory());
			RetentionRule retentionRule = rm.getRetentionRule(folder.getRetentionRule());
			String categoryCodeTitle = category.getCode() + " - " + category.getTitle();
			String retentionRuleCodeTitle = retentionRule.getCode() + " - " + retentionRule.getTitle();
			DecommissioningListReportModel_Folder folderModel = new DecommissioningListReportModel_Folder(folder.getId(),
					folder.getTitle(), retentionRuleCodeTitle, categoryCodeTitle);
			foldersModel.add(folderModel);
		}
		model.setFolders(foldersModel);
		model.setDecommissioningListTitle("Title de la liste de dossiers à fermer");
		model.setDecommissioningListType("Dossiers à fermer");
		model.setDecommissioningListAdministrativeUnitCodeAndTitle(" U10-Unit10");

		return model;
	}

}