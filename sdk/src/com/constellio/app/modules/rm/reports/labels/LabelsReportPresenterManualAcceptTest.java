package com.constellio.app.modules.rm.reports.labels;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManager;
import com.constellio.app.modules.rm.reports.builders.labels.LabelsReportBuilder;
import com.constellio.app.modules.rm.reports.model.labels.ImageLabelsReportField;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportField;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLabel;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportModel;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportPresenter;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class LabelsReportPresenterManualAcceptTest extends ReportBuilderTestFramework {
	private String[] labelReportFieldsToCheck = { "value" };

	private RMTestRecords records = new RMTestRecords(zeCollection);
	private LabelsReportPresenter presenter;
	private LabelTemplateManager labelTemplateManager;
	private static final String LABELS_TEMPLATES_FOLDER = "labelTemplates";
	private RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		recordServices = getModelLayerFactory().newRecordServices();

		presenter = new LabelsReportPresenter(zeCollection, getModelLayerFactory());

		labelTemplateManager = getAppLayerFactory().getLabelTemplateManager();

	}

	@Test
	public void givenFolderA01WhenBuildingLeftFolder5159ModelThenGetLeftFolderModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(3);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);

		assertThat(model.isPrintBorders()).isFalse();

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(1);

		LabelsReportLabel label = model.getLabelsReportLabels().get(0);

		checkFolderA01LeftFolder5159Model(label.getFields());

	}

	private void checkFolderA01LeftFolder5159Model(List<LabelsReportField> fields) {
		assertThat(fields.size()).isEqualTo(5);
		LabelsReportField categoryCodeField = fields.get(0);
		LabelsReportField referenceCategoryCodeField = getReferenceField("X110");
		assertThat(categoryCodeField).isEqualToComparingOnlyGivenFields(referenceCategoryCodeField,
				labelReportFieldsToCheck);

		LabelsReportField idField = fields.get(1);
		//		LabelsReportField referenceIdField = getReferenceField("                                                    A01");
		//		assertThat(idField).isEqualToComparingOnlyGivenFields(referenceIdField, labelReportFieldsToCheck);
		assertThat(idField).isInstanceOf(ImageLabelsReportField.class);
		assertThat(idField.getValue()).endsWith(".png");

		LabelsReportField titleField = fields.get(2);
		LabelsReportField referenceTitleField = getReferenceField("Abeille");
		assertThat(titleField).isEqualToComparingOnlyGivenFields(referenceTitleField, labelReportFieldsToCheck);

		LabelsReportField copyStatusCodeField = fields.get(3);
		LabelsReportField referenceCopyStatusCodeField = getReferenceField("P");
		assertThat(copyStatusCodeField).isEqualToComparingOnlyGivenFields(referenceCopyStatusCodeField,
				labelReportFieldsToCheck);

		LabelsReportField openDateField = fields.get(4);
		LabelsReportField referenceOpenDateField = getReferenceField("2000-10-04");
		assertThat(openDateField).isEqualToComparingOnlyGivenFields(referenceOpenDateField,
				labelReportFieldsToCheck);
	}

	private LabelsReportField getReferenceField(String value) {
		LabelsReportField referenceField = new LabelsReportField();

		referenceField.setValue(value);

		return referenceField;
	}

	@Test
	public void givenFolderA01WhenBuildingRightFolder5159ModelThenGetRightFolderModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);

		assertThat(model.isPrintBorders()).isFalse();

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(1);

		LabelsReportLabel label = model.getLabelsReportLabels().get(0);

		checkFolderA01RightFolder5159Model(label.getFields());

	}

	private void checkFolderA01RightFolder5159Model(List<LabelsReportField> fields) {
		assertThat(fields.size()).isEqualTo(5);
		LabelsReportField idField = fields.get(0);
		assertThat(idField).isInstanceOf(ImageLabelsReportField.class);
		assertThat(idField.getValue()).endsWith(".png");

		LabelsReportField categoryCodeField = fields.get(1);
		//		LabelsReportField referenceCategoryCodeField = getReferenceField(
		//				"                                                   X110");
		LabelsReportField referenceCategoryCodeField = getReferenceField("X110");
		assertThat(categoryCodeField).isEqualToComparingOnlyGivenFields(referenceCategoryCodeField,
				labelReportFieldsToCheck);

		LabelsReportField titleField = fields.get(2);
		LabelsReportField referenceTitleField = getReferenceField("Abeille");
		assertThat(titleField).isEqualToComparingOnlyGivenFields(referenceTitleField, labelReportFieldsToCheck);

		LabelsReportField copyStatusCodeField = fields.get(3);
		LabelsReportField referenceCopyStatusCodeField = getReferenceField("P");
		assertThat(copyStatusCodeField).isEqualToComparingOnlyGivenFields(referenceCopyStatusCodeField,
				labelReportFieldsToCheck);

		LabelsReportField openDateField = fields.get(4);
		LabelsReportField referenceOpenDateField = getReferenceField("2000-10-04");
		assertThat(openDateField).isEqualToComparingOnlyGivenFields(referenceOpenDateField,
				labelReportFieldsToCheck);
	}

	@Test
	public void given1StickerWhenBuildingRight5161ModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(1);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1StickerWhenBuildingLeft5161ModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(4);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1StickerWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given2DifferentStickersWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A02", "A03"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(1);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given2IdenticalStickersWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A04"));
		int startPosition = 1;
		int copies = 2;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1StickerStartingAtPosition2WhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A05"));
		int startPosition = 2;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(1);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(2);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1StickerStartingAtPosition3WhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A06"));
		int startPosition = 3;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(3);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given2StickerStartingAtSecondPositionWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A07", "A08"));
		int startPosition = 2;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(1);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(3);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given3StickerStartingAtSecondPositionWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A07", "A08", "A01"));
		int startPosition = 2;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(1);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(4);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given14StickersWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A09"));
		int startPosition = 1;
		int copies = 14;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(14);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1StickerStartingAtPosition13StickersWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A10"));
		int startPosition = 14;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(1);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(14);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given2StickerStartingAtPosition13StickersWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A11", "A12"));
		int startPosition = 14;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(15);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given20StickersWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01", "A02", "A03", "A04", "A05", "A06", "A07", "A08", "A09", "A10", "A11",
				"A12", "A13", "A14", "A15", "A16", "A17", "A18", "A19", "A20"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(20);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void givenNonExistentStickerWhenBuildingModelThenGetAppropriateModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A00"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		//Has an empty sticker
		assertThat(model.getLabelsReportLabels()).hasSize(1);
		assertThat(model.getLabelsReportLabels().get(0).getFields()).isEmpty();

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1ContainerWhenBuildingLabelThenGetAppropriateLabel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("bac01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(ContainerRecord.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(1);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given2ContainerWhenBuildingLabelThenGetAppropriateLabel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("bac01"));
		int startPosition = 1;
		int copies = 2;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(ContainerRecord.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(2);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void given1ContainerStartingAtPosition2WhenBuildingLabelThenGetAppropriateLabel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("bac01"));
		int startPosition = 2;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(ContainerRecord.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(2);

		build(new LabelsReportBuilder(model));

	}

	@Test
	public void givenSpecialTemplateThenOk()
			throws Exception {
		String filename = "special.xml";
		givenFileNameTemplate(filename);
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("C30"));
		String title =
				"15-14464 Distribuition massive de bacs pour la collecte des matières organiques - Devis technique - Formulaire soumission 2015 - 2016 "
						+ "15-14464 Distribuition massive de bacs pour la collecte des matières organiques - Devis technique - Formulaire soumission 2015 - 2016"
						+ "15-14464 Distribuition massive de bacs pour la collecte des matières organiques - Devis technique - Formulaire soumission 2015 - 2016"
						+ "15-14464 Distribuition massive de bacs pour la collecte des matières organiques - Devis technique - Formulaire soumission 2015 - 2016"
						+ "15-14464 Distribuition massive de bacs pour la collecte des matières organiques - Devis technique - Formulaire soumission 2015 - 2016";
		Folder folder30 = records.getFolder_C30()
				.setTitle(title);
		Category category = records.getCategory_X110().setDescription("Analyse des besoins");
		recordServices.update(folder30.getWrappedRecord());
		recordServices.update(category.getWrappedRecord());
		recordServices.flush();

		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.getLabelTemplate("special.xml");

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		build(new LabelsReportBuilder(model));
	}

	@Test
	public void givenAnotherSpecialTemplateThenOk()
			throws Exception {
		String filename = "special2.xml";
		givenFileNameTemplate(filename);
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("C30"));
		String title =
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		Folder folder30 = records.getFolder_C30()
				.setTitle(title).setUniformSubdivisionEntered(records.subdivId_1);
		Category category = records.getCategory_X110().setCode("S4 100").setTitle("Demande d'enquête");

		recordServices.update(folder30.getWrappedRecord());
		recordServices.update(category.getWrappedRecord());
		recordServices.flush();

		int startPosition = 1;
		int copies = 10;
		LabelTemplate labelTemplate = labelTemplateManager.getLabelTemplate("special2.xml");

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

	@Test
	public void givenSpecial3TemplateThenOk()
			throws Exception {

		String filename = "special3.xml";
		givenFileNameTemplate(filename);
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("C30"));
		String title =
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		Folder folder30 = records.getFolder_C30()
				.setTitle(title)
				.setUniformSubdivisionEntered(records.subdivId_1);
		Category category = records.getCategory_X110().setCode("S4 100").setTitle("Demande d'enquête");

		recordServices.update(folder30.getWrappedRecord());
		recordServices.update(category.getWrappedRecord());
		recordServices.flush();

		int startPosition = 1;
		int copies = 14;
		LabelTemplate labelTemplate = labelTemplateManager.getLabelTemplate("special3.xml");

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

	private void givenFileNameTemplate(String filename)
			throws Exception {
		ConfigManager configManager = getDataLayerFactory().getConfigManager();

		String path = LABELS_TEMPLATES_FOLDER + "/" + filename;
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(getTestResourceFile(filename));
		configManager.add(path, document);
		assertThat(configManager.exist(path)).isTrue();
		assertThat(configManager.list(LABELS_TEMPLATES_FOLDER)).hasSize(1);
	}

}