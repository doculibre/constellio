package com.constellio.app.modules.rm.reports.model.search;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.ReportVO;
import com.constellio.app.ui.entities.ReportedMetadataVO;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ReportTestUtils {
	private MetadataSchemaTypes types;
	private RecordServices recordServices;
	private ReportServices reportServices;
	private final String folderSchemaType = Folder.SCHEMA_TYPE;
	private String expectedFolderTitle_A01 = "title1";
	private String expectedFolderTitle_A02 = "title2";
	private String expectedFolderTitle_A03 = "title3";
	private String expectedFolderDescription_A01 = "description1";
	private String expectedFolderDescription_A03 = "description3";
	private String expectedFolderCreator_A01;
	private String expectedFolderDescription_A02 = null;
	private String expectedFolderCreator_A02;
	private String folderTitleMetadataCode;
	private String folderCreatedByMetadataCode;
	private String folderKeywordsMetadataCode;
	private String folderBorrowedMetadataCode;
	private String folderParentFolderMetadataCode;
	private String folderDescriptionMetadataCode;
	private ModelLayerFactory modelLayerFactory;
	private String zeCollection;

	public ReportTestUtils(ModelLayerFactory modelLayerFactory, String zeCollection, RMTestRecords records) {
		this.modelLayerFactory = modelLayerFactory;
		this.zeCollection = zeCollection;
		reportServices = new ReportServices(modelLayerFactory, zeCollection);
		recordServices = modelLayerFactory.newRecordServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		types = metadataSchemasManager.getSchemaTypes(zeCollection);

		expectedFolderCreator_A01 = records.getChuckNorris().getId();
		expectedFolderCreator_A02 = records.getBob_userInAC().getId();

		MetadataSchema defaultFolderSchema = types.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
		folderTitleMetadataCode = defaultFolderSchema.getMetadata(Folder.TITLE).getCode();
		folderCreatedByMetadataCode = defaultFolderSchema.getMetadata(Schemas.CREATED_BY.getCode()).getCode();
		folderDescriptionMetadataCode = defaultFolderSchema.getMetadata(Folder.DESCRIPTION).getCode();
		folderKeywordsMetadataCode = defaultFolderSchema.getMetadata(Folder.KEYWORDS).getCode();
		folderBorrowedMetadataCode = defaultFolderSchema.getMetadata(Folder.BORROWED).getCode();
		folderParentFolderMetadataCode = defaultFolderSchema.getMetadata(Folder.PARENT_FOLDER).getCode();
	}

	public String getExpectedFolderTitle_A01() {
		return expectedFolderTitle_A01;
	}

	public String getExpectedFolderTitle_A02() {
		return expectedFolderTitle_A02;
	}

	public String getExpectedFolderDescription_A01() {
		return expectedFolderDescription_A01;
	}

	public String getExpectedFolderCreator_A01() {
		return expectedFolderCreator_A01;
	}

	public String getExpectedFolderDescription_A02() {
		return expectedFolderDescription_A02;
	}

	public String getExpectedFolderCreator_A02() {
		return expectedFolderCreator_A02;
	}

	public String getFolderTitleMetadataCode() {
		return folderTitleMetadataCode;
	}

	public String getFolderCreatedByMetadataCode() {
		return folderCreatedByMetadataCode;
	}

	public String getFolderDescriptionMetadataCode() {
		return folderDescriptionMetadataCode;
	}

	public void addDefaultReport(String title) {
		//With two metadata : title and createdBy
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(title);
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(folderSchemaType);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 0));

		reportedMetadataList.add(new ReportedMetadata(folderCreatedByMetadataCode, 1));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public void addDefaultReportWithMultivalue(String title) {
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(title);
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(folderSchemaType);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 0));
		reportedMetadataList.add(new ReportedMetadata(folderCreatedByMetadataCode, 1));
		reportedMetadataList.add(new ReportedMetadata(folderKeywordsMetadataCode, 2));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public void addDefaultReportWithBoolean(String title) {
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(title);
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(folderSchemaType);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 0));
		reportedMetadataList.add(new ReportedMetadata(folderCreatedByMetadataCode, 1));
		reportedMetadataList.add(new ReportedMetadata(folderBorrowedMetadataCode, 2));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public void addDefaultReportWithRichText(String title) {
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(title);
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(folderSchemaType);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 0));
		reportedMetadataList.add(new ReportedMetadata(folderCreatedByMetadataCode, 1));
		reportedMetadataList.add(new ReportedMetadata(reportSchema.getCode() + "_richText", 2));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public void validateDefaultReport(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderTitleMetadataCode).getLabel(Language.French),
				types.getMetadata(folderCreatedByMetadataCode).getLabel(Language.French));
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(2);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(2);
		assertThat(result1.get(0)).isEqualTo(expectedFolderTitle_A01);
		assertThat(result1.get(1)).isEqualTo("Chuck Norris");
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(2);
		assertThat(result2.get(0)).isEqualTo(expectedFolderTitle_A02);
		assertThat(result2.get(1)).isEqualTo("Bob 'Elvis' Gratton");
	}

	public void validateDefaultReportWithMultivalue(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderTitleMetadataCode).getLabel(Language.French),
				types.getMetadata(folderCreatedByMetadataCode).getLabel(Language.French),
				"Mots-clés");
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(2);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(3);
		assertThat(result1.get(0)).isEqualTo(expectedFolderTitle_A01);
		assertThat(result1.get(1)).isEqualTo("Chuck Norris");
		assertThat(result1.get(2)).isEqualTo(asList("mot1", "mot2", "mot3"));
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(3);
		assertThat(result2.get(0)).isEqualTo(expectedFolderTitle_A02);
		assertThat(result2.get(1)).isEqualTo("Bob 'Elvis' Gratton");
		assertThat(result2.get(2)).isEqualTo(null);
	}

	public void validateDefaultReportWithRichText(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderTitleMetadataCode).getLabel(Language.French),
				types.getMetadata(folderCreatedByMetadataCode).getLabel(Language.French),
				"richText");
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(2);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(3);
		assertThat(result1.get(0)).isEqualTo(expectedFolderTitle_A01);
		assertThat(result1.get(1)).isEqualTo("Chuck Norris");
		assertThat(result1.get(2)).isEqualTo("Ceci est un test pour l'université.\n" +
											 "\n" +
											 "\n" +
											 "\n" +
											 "1\n" +
											 "2\n" +
											 "2.1\n" +
											 "A\n" +
											 "B\n" +
											 "B.A\n\n");
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(3);
		assertThat(result2.get(0)).isEqualTo(expectedFolderTitle_A02);
		assertThat(result2.get(1)).isEqualTo("Bob 'Elvis' Gratton");
		assertThat(result2.get(2)).isEqualTo(null);
	}

	public void validateDefaultReportWithBoolean(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderTitleMetadataCode).getLabel(Language.French),
				types.getMetadata(folderCreatedByMetadataCode).getLabel(Language.French),
				types.getMetadata(folderBorrowedMetadataCode).getLabel(Language.French));
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(2);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(3);
		assertThat(result1.get(0)).isEqualTo(expectedFolderTitle_A01);
		assertThat(result1.get(1)).isEqualTo("Chuck Norris");
		assertThat(result1.get(2)).isEqualTo("Oui");
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(3);
		assertThat(result2.get(0)).isEqualTo(expectedFolderTitle_A02);
		assertThat(result2.get(1)).isEqualTo("Bob 'Elvis' Gratton");
		assertThat(result2.get(2)).isEqualTo("Non");
	}

	public void validateUserReportTitles(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderDescriptionMetadataCode).getLabel(Language.French),
				types.getMetadata(folderTitleMetadataCode).getLabel(Language.French));
	}

	public void addUserReport(String reportTitle, String username) {
		//With two metadata : linearSize and title
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(reportTitle);
		report.setUsername(username);
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(folderSchemaType);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 1));

		reportedMetadataList.add(new ReportedMetadata(folderDescriptionMetadataCode, 0));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public void validateUserReportWithSelectedFolders(SearchResultReportModel model) {
		validateUserReportTitles(model);
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(2);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(2);
		assertThat(result1.get(0)).isEqualTo(expectedFolderDescription_A01);
		assertThat(result1.get(1)).isEqualTo(expectedFolderTitle_A01);
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(2);
		assertThat(result2.get(1)).isEqualTo(expectedFolderTitle_A02);
		assertThat(result2.get(0)).isEqualTo(expectedFolderDescription_A02);
	}

	public void validateUserReportWithAllQueryFolders(SearchResultReportModel model) {
		validateUserReportTitles(model);
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(3);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(2);
		assertThat(result1.get(0)).isEqualTo(expectedFolderDescription_A01);
		assertThat(result1.get(1)).isEqualTo(expectedFolderTitle_A01);
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(2);
		assertThat(result2.get(1)).isEqualTo(expectedFolderTitle_A02);
		assertThat(result2.get(0)).isEqualTo(expectedFolderDescription_A02);
		List<Object> result3 = content.get(2);
		assertThat(result3.size()).isEqualTo(2);
		assertThat(result3.get(1)).isEqualTo(expectedFolderTitle_A03);
		assertThat(result3.get(0)).isEqualTo(expectedFolderDescription_A03);
	}

	public void validateUserReport(Report report, String userName) {
		assertThat(report.getUsername()).isEqualTo(userName);
		assertThat(report.getLinesCount()).isEqualTo(1);
		assertThat(report.getColumnsCount()).isEqualTo(2);
		assertThat(report.getSchemaTypeCode()).isEqualTo(folderSchemaType);
		List<ReportedMetadata> reportedMetadata = report.getReportedMetadata();
		assertThat(reportedMetadata.size()).isEqualTo(2);
		assertThat(reportedMetadata.get(0).getMetadataCode()).isEqualTo(folderTitleMetadataCode);
		assertThat(reportedMetadata.get(0).getXPosition()).isEqualTo(1);
		assertThat(reportedMetadata.get(1).getMetadataCode()).isEqualTo(folderDescriptionMetadataCode);
		assertThat(reportedMetadata.get(1).getXPosition()).isEqualTo(0);
	}

	public void validateDefaultReport(Report report) {
		assertThat(report.getUsername()).isNull();
		assertThat(report.getLinesCount()).isEqualTo(1);
		assertThat(report.getColumnsCount()).isEqualTo(2);
		assertThat(report.getSchemaTypeCode()).isEqualTo(folderSchemaType);
		List<ReportedMetadata> reportedMetadata = report.getReportedMetadata();
		assertThat(reportedMetadata.size()).isEqualTo(2);
		assertThat(reportedMetadata.get(0).getMetadataCode()).isEqualTo(folderTitleMetadataCode);
		assertThat(reportedMetadata.get(0).getXPosition()).isEqualTo(0);
		assertThat(reportedMetadata.get(1).getMetadataCode()).isEqualTo(folderCreatedByMetadataCode);
		assertThat(reportedMetadata.get(1).getXPosition()).isEqualTo(1);
	}

	public void validateDefaultReport(ReportVO report) {
		assertThat(report.getUser()).isNull();
		assertThat(report.getSchemaTypeCode()).isEqualTo(folderSchemaType);
		List<ReportedMetadataVO> reportedMetadata = report.getReportedMetadataVOList();
		assertThat(reportedMetadata.size()).isEqualTo(2);
		assertThat(reportedMetadata.get(0).getMetadataCode()).isEqualTo(folderTitleMetadataCode);
		assertThat(reportedMetadata.get(0).getXPosition()).isEqualTo(0);
		assertThat(reportedMetadata.get(1).getMetadataCode()).isEqualTo(folderCreatedByMetadataCode);
		assertThat(reportedMetadata.get(1).getXPosition()).isEqualTo(1);
	}

	public void validateDefaultReport(List<MetadataVO> metadataList) {
		assertThat(metadataList.size()).isEqualTo(2);
		assertThat(metadataList.get(0).getCode()).isEqualTo(folderTitleMetadataCode);
		assertThat(metadataList.get(1).getCode()).isEqualTo(folderCreatedByMetadataCode);
	}

	public void disableAUserReportMetadata() {
		//disable linearSize
		MetadataSchemaTypesBuilder typesBuilder = modelLayerFactory.getMetadataSchemasManager().modify(zeCollection);
		MetadataSchemaTypeBuilder metadataSchemaTypeBuilder = typesBuilder.getSchemaType(folderSchemaType);
		metadataSchemaTypeBuilder.getDefaultSchema().getMetadata(folderDescriptionMetadataCode).setEssentialInSummary(false)
				.setEnabled(false);
		try {
			modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	public void validateUserReportWithDisabledMetadata(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderDescriptionMetadataCode).getLabel(Language.French), types.getMetadata(folderTitleMetadataCode).getLabel(Language.French));
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(2);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(2);
		assertThat(result1.get(0)).isEqualTo(expectedFolderDescription_A01);
		assertThat(result1.get(1)).isEqualTo(expectedFolderTitle_A01);
		List<Object> result2 = content.get(1);
		assertThat(result2.size()).isEqualTo(2);
		assertThat(result2.get(0)).isEqualTo(expectedFolderDescription_A02);
		assertThat(result2.get(1)).isEqualTo(expectedFolderTitle_A02);
	}

	public void addDocumentDefaultReport(String title) {
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(title);
		report.setColumnsCount(3);
		report.setLinesCount(1);
		report.setSchemaTypeCode(Document.SCHEMA_TYPE);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 0));

		reportedMetadataList.add(new ReportedMetadata(folderCreatedByMetadataCode, 1));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public String getExpectedFolderDescription_A03() {
		return expectedFolderDescription_A03;
	}

	public String getExpectedFolderTitle_A03() {
		return expectedFolderTitle_A03;
	}

	public void addDefaultReportWithReference(String title) {
		MetadataSchema reportSchema = types.getSchemaType(Report.SCHEMA_TYPE).getDefaultSchema();
		Report report = new Report(recordServices.newRecordWithSchema(reportSchema), types);
		report.setTitle(title);
		report.setColumnsCount(2);
		report.setLinesCount(1);
		report.setSchemaTypeCode(folderSchemaType);
		List<ReportedMetadata> reportedMetadataList = new ArrayList<>();

		reportedMetadataList.add(new ReportedMetadata(folderTitleMetadataCode, 0));
		reportedMetadataList.add(new ReportedMetadata(folderCreatedByMetadataCode, 1));
		reportedMetadataList.add(new ReportedMetadata(folderParentFolderMetadataCode, 2));

		report.setReportedMetadata(reportedMetadataList);
		reportServices.addUpdate(report);
	}

	public void validateDefaultReportWithReference(SearchResultReportModel model) {
		List<String> titles = model.getColumnsTitles();
		assertThat(titles).containsOnly(types.getMetadata(folderTitleMetadataCode).getLabel(Language.French),
				types.getMetadata(folderCreatedByMetadataCode).getLabel(Language.French),
				types.getMetadata(folderParentFolderMetadataCode).getLabel(Language.French));
		List<List<Object>> content = model.getResults();
		assertThat(content.size()).isEqualTo(1);
		List<Object> result1 = content.get(0);
		assertThat(result1.size()).isEqualTo(3);
		assertThat(result1.get(0)).isEqualTo(expectedFolderTitle_A01);
		assertThat(result1.get(1)).isEqualTo("Chuck Norris");
		assertThat(result1.get(2)).isEqualTo("title2");
	}
}
