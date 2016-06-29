package com.constellio.app.modules.rm.model.labelTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldHorizontalAlignment;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldVerticalAlignment;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateManagerRuntimeException.LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.itextpdf.text.FontFactory;

public class LabelTemplateManagerAcceptanceTest extends ConstellioTest {

	private static final String LABELS_TEMPLATES_FOLDER = "/labelTemplates";
	LabelTemplateManager labelTemplateManager;
	ConfigManager configManager;

	@Before
	public void setUp()
			throws Exception {

		configManager = getDataLayerFactory().getConfigManager();
		labelTemplateManager = getAppLayerFactory().getLabelTemplateManager();
	}

	@Test
	public void givenNoLabelTemplateWhenListTemplatesThenReturnDefaultLabels()
			throws Exception {

		List<LabelTemplate> labelTemplates = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates).hasSize(8);

		assertThat(labelTemplates.get(0).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(0).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(0).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(0).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5159");
		assertThat(labelTemplates.get(0).getName()).isEqualTo("Code de plan justifié à droite (Avery 5159)");
		assertThat(labelTemplates.get(0).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);
		assertThat(labelTemplates.get(0).getFields()).hasSize(5);

		assertThat(labelTemplates.get(1).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(1).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(1).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(1).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5161");
		assertThat(labelTemplates.get(1).getName()).isEqualTo("Code de plan justifié à droite (Avery 5161)");
		assertThat(labelTemplates.get(1).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5161);
		assertThat(labelTemplates.get(1).getFields()).hasSize(5);

		assertThat(labelTemplates.get(2).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(2).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(2).getLines()).isEqualTo(10);
		assertThat(labelTemplates.get(2).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5162");
		assertThat(labelTemplates.get(2).getName()).isEqualTo("Code de plan justifié à droite (Avery 5162)");
		assertThat(labelTemplates.get(2).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5162);
		assertThat(labelTemplates.get(2).getFields()).hasSize(5);

		assertThat(labelTemplates.get(3).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(3).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(3).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(3).getKey()).isEqualTo("FOLDER_RIGHT_AVERY_5163");
		assertThat(labelTemplates.get(3).getName()).isEqualTo("Code de plan justifié à droite (Avery 5163)");
		assertThat(labelTemplates.get(3).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5163);
		assertThat(labelTemplates.get(3).getFields()).hasSize(5);

		assertThat(labelTemplates.get(4).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(4).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(4).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(4).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5159");
		assertThat(labelTemplates.get(4).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5159)");
		assertThat(labelTemplates.get(4).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);
		assertThat(labelTemplates.get(4).getFields()).hasSize(5);

		assertThat(labelTemplates.get(5).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(5).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(5).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(5).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5161");
		assertThat(labelTemplates.get(5).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5161)");
		assertThat(labelTemplates.get(5).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5161);
		assertThat(labelTemplates.get(5).getFields()).hasSize(5);

		assertThat(labelTemplates.get(6).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(6).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(6).getLines()).isEqualTo(10);
		assertThat(labelTemplates.get(6).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5162");
		assertThat(labelTemplates.get(6).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5162)");
		assertThat(labelTemplates.get(6).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5162);
		assertThat(labelTemplates.get(6).getFields()).hasSize(5);

		assertThat(labelTemplates.get(7).getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates.get(7).getColumns()).isEqualTo(30);
		assertThat(labelTemplates.get(7).getLines()).isEqualTo(11);
		assertThat(labelTemplates.get(7).getKey()).isEqualTo("FOLDER_LEFT_AVERY_5163");
		assertThat(labelTemplates.get(7).getName()).isEqualTo("Code de plan justifié à gauche (Avery 5163)");
		assertThat(labelTemplates.get(7).getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5163);
		assertThat(labelTemplates.get(7).getFields()).hasSize(5);
	}

	@Test
	public void givenCustomLabelTemplatesWhenListFolderTemplatesThenOk()
			throws Exception {

		givenCustomTemplates();

		List<LabelTemplate> labelTemplates = labelTemplateManager.listTemplates(Folder.SCHEMA_TYPE);
		assertThat(labelTemplates).hasSize(2);
		assertThat(labelTemplates.get(0).getKey()).isEqualTo("folderTemplate1");
		assertThat(labelTemplates.get(1).getKey()).isEqualTo("folderTemplate2");
	}

	@Test
	public void givenCustomLabelTemplatesWhenListTemplatesWithEmptyStringThenOk()
			throws Exception {

		givenCustomTemplates();

		List<LabelTemplate> labelTemplates = labelTemplateManager.listTemplates("");
		assertThat(labelTemplates).hasSize(1);
		assertThat(labelTemplates.get(0).getKey()).isEqualTo("");
	}

	@Test
	public void givenCustomLabelTemplatesWhenListTemplatesWithNullStringThenOk()
			throws Exception {

		givenCustomTemplates();

		List<LabelTemplate> labelTemplates = labelTemplateManager.listTemplates(null);
		assertThat(labelTemplates).hasSize(1);
		assertThat(labelTemplates.get(0).getKey()).isEqualTo("");
	}

	@Test
	public void whenGetTemplateThenOk()
			throws Exception {

		givenCustomTemplates();
		LabelTemplate template = labelTemplateManager.getLabelTemplate("folderTemplate1.xml");

		assertThat(template.getKey()).isEqualTo("folderTemplate1");
		assertThat(template.getName()).isEqualTo("nameTemplate1");
		assertThat(template.getColumns()).isEqualTo(2);
		assertThat(template.getLines()).isEqualTo(7);
		assertThat(template.getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);
		assertThat(template.getSchemaType()).isEqualTo(Folder.SCHEMA_TYPE);
		assertThat(template.getFields()).hasSize(2);

		assertThat(template.getFields().get(0).getFontName()).isEqualTo(FontFactory.COURIER);
		assertThat(template.getFields().get(0).getFontSize()).isEqualTo(10);
		assertThat(template.getFields().get(0).getMaxLength()).isEqualTo(10);
		assertThat(template.getFields().get(0).getHeight()).isEqualTo(5);
		assertThat(template.getFields().get(0).getWidth()).isEqualTo(6);
		assertThat(template.getFields().get(0).getMetadataCode()).isEqualTo("folder_default_title");
		assertThat(template.getFields().get(0).getReferenceMetadataCode()).isEqualTo("");
		assertThat(template.getFields().get(0).getX()).isEqualTo(1);
		assertThat(template.getFields().get(0).getY()).isEqualTo(2);
		assertThat(template.getFields().get(0).getHorizontalAlignment())
				.isEqualTo(LabelTemplateFieldHorizontalAlignment.RIGHT);
		assertThat(template.getFields().get(0).getVerticalAlignment()).isEqualTo(LabelTemplateFieldVerticalAlignment.TOP);

		assertThat(template.getFields().get(1).getFontName()).isEqualTo(FontFactory.COURIER);
		assertThat(template.getFields().get(1).getFontSize()).isEqualTo(10);
		assertThat(template.getFields().get(0).getMaxLength()).isEqualTo(10);
		assertThat(template.getFields().get(1).getHeight()).isEqualTo(5);
		assertThat(template.getFields().get(1).getWidth()).isEqualTo(6);
		assertThat(template.getFields().get(1).getMetadataCode()).isEqualTo("folder_default_administrativeUnitId");
		assertThat(template.getFields().get(1).getReferenceMetadataCode()).isEqualTo("administrativeUnit_default_code");
		assertThat(template.getFields().get(1).getX()).isEqualTo(5);
		assertThat(template.getFields().get(1).getY()).isEqualTo(5);
		assertThat(template.getFields().get(1).getHorizontalAlignment())
				.isEqualTo(LabelTemplateFieldHorizontalAlignment.RIGHT);
		assertThat(template.getFields().get(1).getVerticalAlignment()).isEqualTo(LabelTemplateFieldVerticalAlignment.TOP);
	}

	@Test(expected = LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate.class)
	public void givenInvalidTemplateFileWhenGetTemplateThenException()
			throws Exception {

		givenInvalidTemplateFile();
		labelTemplateManager.getLabelTemplate("invalidTemplate.xml");
	}

	@Test
	public void givenInexistentFilenameWhenGetTemplateThenReturnNull()
			throws Exception {

		this.givenCustomTemplates();
		LabelTemplate template = labelTemplateManager.getLabelTemplate("InexistentFilename.xml");
		assertThat(template).isNull();
	}

	public void givenCustomTemplates()
			throws Exception {

		List<String> filenames = new ArrayList<>();
		String filename1 = "folderTemplate1.xml";
		String filename2 = "folderTemplate2.xml";
		String filename3 = "containerTemplate1.xml";
		filenames.add(filename1);
		filenames.add(filename2);
		filenames.add(filename3);

		for (String filename : filenames) {
			String path = LABELS_TEMPLATES_FOLDER + "/" + filename;
			SAXBuilder saxBuilder = new SAXBuilder();
			Document document = saxBuilder.build(getTestResourceFile(filename));
			configManager.add(path, document);
			assertThat(configManager.exist(path)).isTrue();
		}
		assertThat(configManager.list(LABELS_TEMPLATES_FOLDER)).hasSize(3);
	}

	private void givenInvalidTemplateFile()
			throws Exception {

		String filename = "invalidTemplate.xml";

		String path = LABELS_TEMPLATES_FOLDER + "/" + filename;
		SAXBuilder saxBuilder = new SAXBuilder();
		Document document = saxBuilder.build(getTestResourceFile(filename));
		configManager.add(path, document);
		assertThat(configManager.exist(path)).isTrue();
		assertThat(configManager.list(LABELS_TEMPLATES_FOLDER)).hasSize(1);
	}

}