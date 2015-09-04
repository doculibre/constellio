/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
	public void givenFolderA01WhenBuildingLeftFolderModelThenGetLeftFolderModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(3);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);

		assertThat(model.isPrintBorders()).isTrue();

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(1);

		LabelsReportLabel label = model.getLabelsReportLabels().get(0);

		checkFolderA01LeftFolderModel(label.getFields());

	}

	private void checkFolderA01LeftFolderModel(List<LabelsReportField> fields) {
		assertThat(fields.size()).isEqualTo(6);
		LabelsReportField categoryCodeField = fields.get(0);
		LabelsReportField referenceCategoryCodeField = getReferenceField("X110");
		assertThat(categoryCodeField).isEqualToComparingOnlyGivenFields(referenceCategoryCodeField,
				labelReportFieldsToCheck);

		LabelsReportField idField = fields.get(1);
		//		LabelsReportField referenceIdField = getReferenceField("                                                    A01");
		//		assertThat(idField).isEqualToComparingOnlyGivenFields(referenceIdField, labelReportFieldsToCheck);
		LabelsReportField referenceIdField = getReferenceField("A01");
		assertThat(idField).isEqualToComparingOnlyGivenFields(referenceIdField, labelReportFieldsToCheck);

		LabelsReportField titleField = fields.get(2);
		LabelsReportField referenceTitleField = getReferenceField("Abeille");
		assertThat(titleField).isEqualToComparingOnlyGivenFields(referenceTitleField, labelReportFieldsToCheck);

		LabelsReportField copyStatusCodeField = fields.get(4);
		LabelsReportField referenceCopyStatusCodeField = getReferenceField("P");
		assertThat(copyStatusCodeField).isEqualToComparingOnlyGivenFields(referenceCopyStatusCodeField,
				labelReportFieldsToCheck);

		LabelsReportField referenceOpenDateField = getReferenceField("2000-10-04");
		assertThat(referenceOpenDateField).isEqualToComparingOnlyGivenFields(referenceOpenDateField,
				labelReportFieldsToCheck);
	}

	private LabelsReportField getReferenceField(String value) {
		LabelsReportField referenceField = new LabelsReportField();

		referenceField.setValue(value);

		return referenceField;
	}

	@Test
	public void givenFolderA01WhenBuildingRightFolderModelThenGetRightFolderModel() {
		List<String> folderIds = new ArrayList<>();
		folderIds.addAll(Arrays.asList("A01"));
		int startPosition = 1;
		int copies = 1;
		LabelTemplate labelTemplate = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE).get(0);

		LabelsReportModel model = presenter.build(folderIds, startPosition, copies, labelTemplate);

		assertThat(model.getLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);

		assertThat(model.isPrintBorders()).isTrue();

		assertThat(model.getLabelsReportLabels().size()).isEqualTo(1);

		LabelsReportLabel label = model.getLabelsReportLabels().get(0);

		checkFolderA01RightFolderModel(label.getFields());

	}

	private void checkFolderA01RightFolderModel(List<LabelsReportField> fields) {
		assertThat(fields.size()).isEqualTo(6);
		LabelsReportField idField = fields.get(0);
		LabelsReportField referenceIdField = getReferenceField("A01");
		assertThat(idField).isEqualToComparingOnlyGivenFields(referenceIdField, labelReportFieldsToCheck);

		LabelsReportField categoryCodeField = fields.get(1);
		//		LabelsReportField referenceCategoryCodeField = getReferenceField(
		//				"                                                   X110");
		LabelsReportField referenceCategoryCodeField = getReferenceField("X110");
		assertThat(categoryCodeField).isEqualToComparingOnlyGivenFields(referenceCategoryCodeField,
				labelReportFieldsToCheck);

		LabelsReportField titleField = fields.get(2);
		LabelsReportField referenceTitleField = getReferenceField("Abeille");
		assertThat(titleField).isEqualToComparingOnlyGivenFields(referenceTitleField, labelReportFieldsToCheck);

		LabelsReportField copyStatusCodeField = fields.get(4);
		LabelsReportField referenceCopyStatusCodeField = getReferenceField("P");
		assertThat(copyStatusCodeField).isEqualToComparingOnlyGivenFields(referenceCopyStatusCodeField,
				labelReportFieldsToCheck);

		LabelsReportField referenceOpenDateField = getReferenceField("2000-10-04");
		assertThat(referenceOpenDateField).isEqualToComparingOnlyGivenFields(referenceOpenDateField,
				labelReportFieldsToCheck);
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
		givenSpecialTemplate();
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

	private void givenSpecialTemplate()
			throws Exception {
		ConfigManager configManager = getDataLayerFactory().getConfigManager();

		String filename = "special.xml";
		String path = LABELS_TEMPLATES_FOLDER + "/" + filename;
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(getTestResourceFile(filename));
		configManager.add(path, document);
		assertThat(configManager.exist(path)).isTrue();
		assertThat(configManager.list(LABELS_TEMPLATES_FOLDER)).hasSize(1);
	}

}