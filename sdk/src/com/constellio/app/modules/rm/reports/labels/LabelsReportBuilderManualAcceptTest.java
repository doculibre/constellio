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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.constellio.app.modules.rm.reports.builders.labels.LabelsReportBuilder;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportField;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportFont;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLabel;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportModel;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;

public class LabelsReportBuilderManualAcceptTest extends ReportBuilderTestFramework {

	@Test
	public void given2ColumnsAnd5RowsOfLabelsThenSheetIsCorrectlyDivided() {

		LabelsReportModel model = new LabelsReportModel();

		LabelsReportLayout labelsReportLayout = LabelsReportLayout.AVERY_5159;
		model.setLayout(labelsReportLayout);
		model.setColumnsNumber(30);
		model.setRowsNumber(10);

		LabelsReportField labelsReportField1 = new LabelsReportField();
		labelsReportField1.positionX = 3;
		labelsReportField1.positionY = 3;
		labelsReportField1.height = 2;
		labelsReportField1.width = 10;
		labelsReportField1.setValue("Value of the first field");
		labelsReportField1.setFont(new LabelsReportFont().setSize(8.0f).setBold(true).setItalic(true));
		LabelsReportField labelsReportField2 = new LabelsReportField();
		labelsReportField2.positionX = 3;
		labelsReportField2.positionY = 6;
		labelsReportField2.height = 2;
		labelsReportField2.width = 10;
		labelsReportField2.setValue("Value of the second field");
		LabelsReportField labelsReportField3 = new LabelsReportField();
		labelsReportField3.positionX = 16;
		labelsReportField3.positionY = 3;
		labelsReportField3.height = 2;
		labelsReportField3.width = 4;
		labelsReportField3.setValue("Word.");
		List<LabelsReportField> fields = Arrays.asList(labelsReportField3, labelsReportField2, labelsReportField1);

		LabelsReportLabel sticker = new LabelsReportLabel(fields);

		List<LabelsReportLabel> stickers = Arrays
				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker,
						sticker, sticker, sticker, sticker, sticker);

		model.setLabelsReportLabels(stickers);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

	@Test
	public void given2ColumnsAnd10RowsOfLabelsThenSheetIsCorrectlyDivided() {

		LabelsReportModel model = new LabelsReportModel();

		LabelsReportLayout labelsReportLayout = LabelsReportLayout.AVERY_5161;
		model.setLayout(labelsReportLayout);
		model.setColumnsNumber(30);
		model.setRowsNumber(10);

		LabelsReportField labelsReportField1 = new LabelsReportField();
		labelsReportField1.positionX = 3;
		labelsReportField1.positionY = 3;
		labelsReportField1.height = 2;
		labelsReportField1.width = 10;
		labelsReportField1.setValue("Value of the first field");
		LabelsReportField labelsReportField2 = new LabelsReportField();
		labelsReportField2.positionX = 3;
		labelsReportField2.positionY = 6;
		labelsReportField2.height = 2;
		labelsReportField2.width = 10;
		labelsReportField2.setValue("Value of the second field");
		LabelsReportField labelsReportField3 = new LabelsReportField();
		labelsReportField3.positionX = 16;
		labelsReportField3.positionY = 3;
		labelsReportField3.height = 2;
		labelsReportField3.width = 4;
		labelsReportField3.setValue("Word.");
		List<LabelsReportField> fields = Arrays.asList(labelsReportField3, labelsReportField2, labelsReportField1);

		LabelsReportLabel sticker = new LabelsReportLabel(fields);

		List<LabelsReportLabel> stickers = Arrays
				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker,
						sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker);

		model.setLabelsReportLabels(stickers);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

}
