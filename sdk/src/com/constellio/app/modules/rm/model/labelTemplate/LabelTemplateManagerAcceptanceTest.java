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
package com.constellio.app.modules.rm.model.labelTemplate;

import static org.assertj.core.api.Assertions.assertThat;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate.SchemaType;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldHorizontalAlignment;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplateField.LabelTemplateFieldVerticalAlignment;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.itextpdf.text.FontFactory;

public class LabelTemplateManagerAcceptanceTest extends ConstellioTest {

	private static final String LABELS_TEMPLATES_FOLDER = "labelTemplates";
	LabelTemplateManager labelTemplateManager;

	@Before
	public void setUp()
			throws Exception {

		givenValidExtesionFileInFolder();

		labelTemplateManager = getAppLayerFactory().getLabelTemplateManager();
	}

	private void addCustomLabelTemplates()
			throws Exception {
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document labelTemplate = jdomBuilder.build(getTestResourceFile("customTemplate.xml"));
		getDataLayerFactory().getConfigManager().add("/labelTemplates/custom.xml", labelTemplate);
	}

	@After
	public void givenInvalidExtesionFileInFolder() {
		ConfigManager configManager = getDataLayerFactory().getConfigManager();
		for (String filename : configManager.list(LABELS_TEMPLATES_FOLDER)) {
			assertThat(configManager
					.rename(LABELS_TEMPLATES_FOLDER + "/" + filename, LABELS_TEMPLATES_FOLDER + "/" + filename + ".bkp"))
					.isTrue();
		}
	}

	@Test
	public void whenNoFilesInLabelTemplateFolderThenReturnDefaultLabel()
			throws Exception {
		givenInvalidExtesionFileInFolder();

		assertThat(labelTemplateManager.listTemplates(SchemaType.FOLDER.name())).hasSize(2);
	}

	@Test
	public void whenListTemplatesThenOk()
			throws Exception {
		assertThat(labelTemplateManager.listTemplates(SchemaType.FOLDER.name())).hasSize(2);
	}

	//TODO Thiago @Test
	public void givenDocumentWhenGetTemplateThenOk()
			throws Exception {

		labelTemplateManager.listTemplates(SchemaType.FOLDER.name());
		assertThat(labelTemplateManager.listTemplates(SchemaType.FOLDER.name())).hasSize(2);
		LabelTemplate template = labelTemplateManager.getLabelTemplate("folderTemplate1.xml");

		assertThat(template.getKey()).isEqualTo("folderTemplate1");
		assertThat(template.getName()).isEqualTo("nameTemplate1");
		assertThat(template.getColumns()).isEqualTo(2);
		assertThat(template.getLines()).isEqualTo(7);
		assertThat(template.getLabelsReportLayout()).isEqualTo(LabelsReportLayout.AVERY_5159);
		assertThat(template.getSchemaType()).isEqualTo(SchemaType.FOLDER);
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

	//

	private void givenValidExtesionFileInFolder() {
		ConfigManager configManager = getDataLayerFactory().getConfigManager();
		for (String filename : configManager.list(LABELS_TEMPLATES_FOLDER)) {
			String newFilename = filename.replace(".bkp", "");
			configManager
					.rename(LABELS_TEMPLATES_FOLDER + "/" + filename, LABELS_TEMPLATES_FOLDER + "/" + newFilename);
		}
	}
}