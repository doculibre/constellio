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
		labelsReportField1.positionX = 0;
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

		//		List<LabelsReportLabel> stickers = Arrays
		//				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker,
		//						sticker, sticker, sticker, sticker, sticker);
		List<LabelsReportLabel> stickers = Arrays
				.asList(sticker);

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
		model.setRowsNumber(5);

		LabelsReportField labelsReportField1 = new LabelsReportField();
		labelsReportField1.positionX = 1;
		labelsReportField1.positionY = 1;
		labelsReportField1.height = 3;
		labelsReportField1.width = 100;
		labelsReportField1.setValue("Value 1");
		labelsReportField1.setFont(new LabelsReportFont().setSize(8.0f).setBold(false).setItalic(false));
		LabelsReportField labelsReportField2 = new LabelsReportField();
		labelsReportField2.positionX = 3;
		labelsReportField2.positionY = 6;
		labelsReportField2.height = 2;
		labelsReportField2.width = 100;
		labelsReportField2.setFont(new LabelsReportFont().setSize(8.0f).setBold(false).setItalic(false));
		labelsReportField2.setValue("Value of the second field");
		LabelsReportField labelsReportField3 = new LabelsReportField();
		labelsReportField3.positionX = 16;
		labelsReportField3.positionY = 8;
		labelsReportField3.height = 2;
		labelsReportField3.width = 40;
		labelsReportField3.setFont(new LabelsReportFont().setSize(8.0f).setBold(false).setItalic(false));
		labelsReportField3.setValue("Word.");
		List<LabelsReportField> fields = Arrays.asList(labelsReportField1/*, labelsReportField2, labelsReportField3*/);

		LabelsReportLabel sticker = new LabelsReportLabel(fields);

		List<LabelsReportLabel> stickers = Arrays
				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker,
						sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker);

		model.setLabelsReportLabels(stickers);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

	@Test
	public void given2ColumnsAnd5RowsOfLabelsThenSheetIsCorrectlyDivided2() {

		LabelsReportModel model = new LabelsReportModel();

		LabelsReportLayout labelsReportLayout = LabelsReportLayout.LABEL_1_5_X_5_25;
		System.out.println(labelsReportLayout.getPageSize());
		model.setLayout(labelsReportLayout);
		model.setColumnsNumber(60);
		model.setRowsNumber(5);

		LabelsReportField labelsReportField1 = new LabelsReportField();
		labelsReportField1.positionX = 0;
		labelsReportField1.positionY = 0;
		labelsReportField1.height = 1;
		labelsReportField1.width = 8;
		labelsReportField1.setValue("S4 100");
		labelsReportField1.setFont(new LabelsReportFont().setSize(10).setBold(true).setItalic(false));

		LabelsReportField labelsReportField2 = new LabelsReportField();
		labelsReportField2.positionX = 8;
		labelsReportField2.positionY = 0;
		labelsReportField2.height = 1;
		labelsReportField2.width = 40;
		labelsReportField2.setValue("2015-028 P -PA");
		labelsReportField2.setFont(new LabelsReportFont().setSize(10).setBold(true).setItalic(true));

		LabelsReportField labelsReportField3 = new LabelsReportField();
		labelsReportField3.positionX = 50;
		labelsReportField3.positionY = 0;
		labelsReportField3.height = 1;
		labelsReportField3.width = 10;
		labelsReportField3.setValue("2015-04-28");
		labelsReportField3.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(true));

		LabelsReportField labelsReportField4 = new LabelsReportField();
		labelsReportField4.positionX = 0;
		labelsReportField4.positionY = 1;
		labelsReportField4.height = 1;
		labelsReportField4.width = 60;
		labelsReportField4.setValue("Demande d'enquête");
		labelsReportField4.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		LabelsReportField labelsReportField5 = new LabelsReportField();
		labelsReportField5.positionX = 0;
		labelsReportField5.positionY = 2;
		labelsReportField5.height = 1;
		labelsReportField5.width = 60;
		labelsReportField5.setValue("Syndic");
		labelsReportField5.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		LabelsReportField labelsReportField6 = new LabelsReportField();
		labelsReportField6.positionX = 0;
		labelsReportField6.positionY = 3;
		labelsReportField6.height = 1;
		labelsReportField6.width = 60;
		labelsReportField6.setValue("Activitées spécifiques à l'OPPQ");
		labelsReportField6.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		LabelsReportField labelsReportField7 = new LabelsReportField();
		labelsReportField7.positionX = 0;
		labelsReportField7.positionY = 4;
		labelsReportField7.height = 1;
		labelsReportField7.width = 10;
		labelsReportField7.setValue("5038");
		labelsReportField7.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		LabelsReportField labelsReportField8 = new LabelsReportField();
		labelsReportField8.positionX = 30;
		labelsReportField8.positionY = 4;
		labelsReportField8.height = 1;
		labelsReportField8.width = 5;
		labelsReportField8.setValue("P");
		labelsReportField8.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		LabelsReportField labelsReportField9 = new LabelsReportField();
		labelsReportField9.positionX = 40;
		labelsReportField9.positionY = 4;
		labelsReportField9.height = 1;
		labelsReportField9.width = 10;
		labelsReportField9.setValue("SYN01-SYN");
		labelsReportField9.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		LabelsReportField labelsReportField10 = new LabelsReportField();
		labelsReportField10.positionX = 50;
		labelsReportField10.positionY = 4;
		labelsReportField10.height = 1;
		labelsReportField10.width = 10;
		labelsReportField10.setValue("-DIR.LG");
		labelsReportField10.setFont(new LabelsReportFont().setSize(10).setBold(false).setItalic(false));

		List<LabelsReportField> fields = Arrays.asList(
				labelsReportField1,
				labelsReportField2,
				labelsReportField3,
				labelsReportField4,
				labelsReportField5,
				labelsReportField6,
				labelsReportField7,
				labelsReportField8,
				labelsReportField9,
				labelsReportField10
		);

		LabelsReportLabel sticker = new LabelsReportLabel(fields);

		//		List<LabelsReportLabel> stickers = Arrays
		//				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker,
		//						sticker, sticker, sticker, sticker, sticker);
		//		List<LabelsReportLabel> stickers = Arrays
		//				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker);

		List<LabelsReportLabel> stickers = Arrays
				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker);

		model.setLabelsReportLabels(stickers);
		//		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

	@Test
	public void given2ColumnsAnd2RowsOfLabelsThenSheetIsCorrectlyDivided() {

		LabelsReportModel model = new LabelsReportModel();

		LabelsReportLayout labelsReportLayout = LabelsReportLayout.AVERY_5168;
		System.out.println(labelsReportLayout.getPageSize());
		model.setLayout(labelsReportLayout);
		model.setColumnsNumber(48);
		model.setRowsNumber(3);

		LabelsReportField labelsReportField1 = new LabelsReportField();
		labelsReportField1.positionX = 5;
		labelsReportField1.positionY = 0;
		labelsReportField1.height = 1;
		labelsReportField1.width = 48;
		labelsReportField1.setValue("13-0010");
		labelsReportField1.setFont(new LabelsReportFont().setSize(40).setBold(true).setItalic(false));

		LabelsReportField labelsReportField2 = new LabelsReportField();
		labelsReportField2.positionX = 5;
		labelsReportField2.positionY = 2;
		labelsReportField2.height = 1;
		labelsReportField2.width = 22;
		labelsReportField2.setValue("8888-8888");
		labelsReportField2.setFont(new LabelsReportFont().setSize(25).setBold(true).setItalic(false));

		LabelsReportField labelsReportField3 = new LabelsReportField();
		labelsReportField3.positionX = 27;
		labelsReportField3.positionY = 2;
		labelsReportField3.height = 1;
		labelsReportField3.width = 20;
		labelsReportField3.setValue("Destruction");
		labelsReportField3.setFont(new LabelsReportFont().setSize(20).setBold(false).setItalic(true));

		List<LabelsReportField> fields = Arrays.asList(
				labelsReportField1,
				labelsReportField2,
				labelsReportField3
		);

		LabelsReportLabel sticker = new LabelsReportLabel(fields);

		//		List<LabelsReportLabel> stickers = Arrays
		//				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker,
		//						sticker, sticker, sticker, sticker, sticker);
		//		List<LabelsReportLabel> stickers = Arrays
		//				.asList(sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker, sticker);

		List<LabelsReportLabel> stickers = Arrays
				.asList(sticker, sticker, sticker, sticker);

		model.setLabelsReportLabels(stickers);
		model.setPrintBorders(true);

		build(new LabelsReportBuilder(model));
	}

}
