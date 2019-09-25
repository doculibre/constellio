package com.constellio.app.modules.rm.reports.decommissioning;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListXLSDetailedReportParameters;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListXLSDetailedReportWriter;
import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListXLSDetailedReportPresenter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailStatus;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DecommissioningListXLSDetailedReportWriterManualAcceptTest extends ReportBuilderTestFramework {
	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	RMSchemasRecordsServices rm;

	Locale locale;

	@Before
	public void setUp() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTest(users)
		);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		locale = Locale.FRENCH;
	}

	@Test
	public void whenBuildReportThenOk() throws Exception {
		createDecomListTest();
		createReportTest();

		DecommissioningListXLSDetailedReportParameters parameters =
				new DecommissioningListXLSDetailedReportParameters("test01",
						DecommissioningList.SCHEMA_TYPE, zeCollection, users.adminIn(zeCollection),
						"ABC", "DEF");

		DecommissioningListXLSDetailedReportPresenter presenter =
				new DecommissioningListXLSDetailedReportPresenter(getAppLayerFactory(), locale, zeCollection, parameters);

		build(new DecommissioningListXLSDetailedReportWriter(presenter.build(), presenter.getLocale()));
	}

	private void createDecomListTest() throws Exception {
		DecommissioningList decomList = rm.newDecommissioningListWithId("test01");
		decomList.setTitle("Title test");
		decomList.setDecommissioningListType(DecommissioningListType.FOLDERS_TO_TRANSFER);
		decomList.setDescription("Description test");
		decomList.setAdministrativeUnit(records.unitId_10a);
		decomList.setCreatedOn(LocalDateTime.now());
		decomList.setCreatedBy(users.aliceIn(zeCollection));
		decomList.setModifiedOn(LocalDateTime.now());
		decomList.setOriginArchivisticStatus(OriginStatus.ACTIVE);
		decomList.setApprovalDate(LocalDate.now());
		decomList.setApprovalUser(users.charlesIn(zeCollection));


		Comment com1 = new Comment();
		com1.setUser(users.edouardLechatIn(zeCollection));
		com1.setDateTime(LocalDateTime.now());
		com1.setMessage("Comment #1 test");

		Comment com2 = new Comment();
		com2.setUser(users.dakotaLIndienIn(zeCollection));
		com2.setDateTime(LocalDateTime.now());
		com2.setMessage("Comment #2 test");

		Comment com3 = new Comment();
		com3.setUser(users.robinIn(zeCollection));
		com3.setDateTime(LocalDateTime.now());
		com3.setMessage("Comment #3 test");

		decomList.setComments(Arrays.asList(com1, com2, com3));


		DecomListValidation valid1 = new DecomListValidation();
		valid1.setUserId(users.bobIn(zeCollection).getId());
		valid1.setRequestDate(LocalDate.now());
		valid1.setValidationDate(LocalDate.now());

		DecomListValidation valid2 = new DecomListValidation();
		valid2.setUserId(users.chuckNorrisIn(zeCollection).getId());
		valid2.setRequestDate(LocalDate.now());
		valid2.setValidationDate(LocalDate.now());

		decomList.setValidations(Arrays.asList(valid1, valid2));


		Category cat = rm.newCategoryWithId("Y").setCode("Y")
				.setTitle("Y categorie").setTitle(Locale.ENGLISH, "Y category");

		Folder folder1 = rm.newFolder().setCategoryEntered(cat).setAdministrativeUnitEntered(records.unitId_10)
				.setRetentionRuleEntered(records.ruleId_2).setCopyStatusEntered(CopyType.PRINCIPAL)
				.setTitle("Number 1 folder").setOpenDate(LocalDate.now());

		Folder folder2 = rm.newFolder().setCategoryEntered(cat).setAdministrativeUnitEntered(records.unitId_10)
				.setRetentionRuleEntered(records.ruleId_2).setCopyStatusEntered(CopyType.PRINCIPAL)
				.setTitle("Number 2 folder").setOpenDate(LocalDate.now());

		Folder folder3 = rm.newFolder().setCategoryEntered(cat).setAdministrativeUnitEntered(records.unitId_10)
				.setRetentionRuleEntered(records.ruleId_2).setCopyStatusEntered(CopyType.PRINCIPAL)
				.setTitle("Number 3 folder").setOpenDate(LocalDate.now());

		DecomListFolderDetail detail1 = new DecomListFolderDetail(folder1, FolderDetailStatus.INCLUDED);
		DecomListFolderDetail detail2 = new DecomListFolderDetail(folder2, FolderDetailStatus.INCLUDED);
		DecomListFolderDetail detail3 = new DecomListFolderDetail(folder3, FolderDetailStatus.EXCLUDED);

		decomList.setFolderDetails(Arrays.asList(detail1, detail2, detail3));


		Transaction tr = new Transaction();
		tr.addAll(cat, folder1, folder2, folder3, decomList);
		rm.executeTransaction(tr);
	}

	private void createReportTest()
	{
		RecordServices recordServices = getAppLayerFactory().getModelLayerFactory().newRecordServices();
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		ReportServices reportServices = new ReportServices(getModelLayerFactory(), zeCollection);
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);

		report.setTitle("ABC");
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(Folder.SCHEMA_TYPE);

		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();
		reportedMetadataList.add(new ReportedMetadata(Schemas.IDENTIFIER.getCode(), 0));
		reportedMetadataList.add(new ReportedMetadata(Schemas.TITLE.getCode(), 1));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.category().getCode(), 2));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.retentionRule().getCode(), 3));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.mediaType().getCode(), 4));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.linearSize().getCode(), 5));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.container().getCode(), 6));
		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);

		report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle("DEF");
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(Folder.SCHEMA_TYPE);

		reportedMetadataList = new ArrayList<>();
		reportedMetadataList.add(new ReportedMetadata(Schemas.IDENTIFIER.getCode(), 0));
		reportedMetadataList.add(new ReportedMetadata(Schemas.TITLE.getCode(), 1));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.category().getCode(), 2));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.retentionRule().getCode(), 3));
		reportedMetadataList.add(new ReportedMetadata(rm.folder.mediaType().getCode(), 4));
		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}
}