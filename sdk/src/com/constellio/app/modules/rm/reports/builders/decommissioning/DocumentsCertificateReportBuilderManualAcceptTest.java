package com.constellio.app.modules.rm.reports.builders.decommissioning;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel.DocumentsCertificateReportModel_Document;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

/**
 * Created by Patrick on 2016-01-15.
 */
public class DocumentsCertificateReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	DocumentsCertificateReportModel model;

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
	public void whenBuildEmptyDocumentsCertificateReportThenOk() {
		model = new DocumentsCertificateReportModel();
		model.setCellBorder(true);
		model.setDestructionDate(TimeProvider.getLocalDate().toString());
		model.setCertificateCreationDate(TimeProvider.getLocalDate().toString());
		model.setHash("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=");
		build(new DocumentsCertificateReportBuilder(model,
				getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildTestDocumentsCertificateReportThenOk() {
		model = newTestCertificateReportModel();
		model.setDestructionDate(TimeProvider.getLocalDate().toString());
		model.setCertificateCreationDate(TimeProvider.getLocalDate().toString());
		model.setHash("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=");
		build(new DocumentsCertificateReportBuilder(model, getModelLayerFactory().getFoldersLocator()));
	}

	@Test
	public void whenBuildDocumentsCertificateReportThenOk() {
		model = newCertificateReportModel();
		model.setDestructionDate(TimeProvider.getLocalDate().toString());
		model.setCertificateCreationDate(TimeProvider.getLocalDate().toString());
		model.setHash("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=");
		build(new DocumentsCertificateReportBuilder(model, getModelLayerFactory().getFoldersLocator()));
	}

	private DocumentsCertificateReportModel newTestCertificateReportModel() {

		DocumentsCertificateReportModel model = newCertificateReportModel();
		List<DocumentsCertificateReportModel_Document> documents = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			DocumentsCertificateReportModel_Document document = new DocumentsCertificateReportModel_Document();
			document.setFilename("filename" + i + ".ext");
			document.setFolder("folder " + i);
			document.setId("00000" + i);
			document.setTitle("document title " + i);
			document.setMd5("T+4zq4cGP/tXkdJp/qz1WVWYhoQ=" + i);
			document.setRetentionRuleCode("rule" + i);
			document.setPrincipalCopyRetentionRule("888-3-d" + i);
			documents.add(document);
		}

		model.setDocuments(documents);

		return model;
	}

	private DocumentsCertificateReportModel newCertificateReportModel() {

		DocumentsCertificateReportModel model = new DocumentsCertificateReportModel();

		DecommissioningList decommissioningList = rm.getDecommissioningList("list02");

		List<DocumentsCertificateReportModel_Document> documentsModel = new ArrayList<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition = from(types.getSchemaType(Document.SCHEMA_TYPE))
				.where(rm.documentParentFolder())
				.isIn(decommissioningList.getFolders());
		query.setCondition(condition);
		List<Record> documentsRecords = searchServices.search(query);
		List<Document> documents = rm.wrapDocuments(documentsRecords);

		for (Document document : documents) {
			DocumentsCertificateReportModel_Document documentModel = new DocumentsCertificateReportModel_Document();
			String filename = "";
			try {
				filename = document.getContent().getCurrentVersion().getFilename();
			} catch (Exception e) {
			}
			documentModel.setFilename(filename);
			documentModel.setFolder(rm.getFolder(document.getFolder()).getTitle());
			documentModel.setId(document.getId());
			documentModel.setTitle(document.getTitle());
			String hash = "";
			try {
				hash = document.getContent().getCurrentVersion().getHash();
			} catch (Exception e) {
			}
			documentModel.setMd5(hash);
			documentModel
					.setRetentionRuleCode(rm.getRetentionRule(rm.getFolder(document.getFolder()).getRetentionRule()).getCode());
			documentModel.setPrincipalCopyRetentionRule(rm.getFolder(document.getFolder()).getMainCopyRule().getCode());
			documentsModel.add(documentModel);

		}

		model.setDocuments(documentsModel);

		return model;
	}
}