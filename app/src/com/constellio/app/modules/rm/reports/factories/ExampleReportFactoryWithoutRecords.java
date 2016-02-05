package com.constellio.app.modules.rm.reports.factories;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.reports.builders.labels.LabelsReportBuilder;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportField;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportFont;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLabel;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportLayout;
import com.constellio.app.modules.rm.reports.model.labels.LabelsReportModel;
import com.constellio.app.ui.framework.reports.ReportBuilder;
import com.constellio.app.ui.framework.reports.ReportBuilderFactory;
import com.constellio.model.services.factories.ModelLayerFactory;

// TODO: DELETE ME!
public class ExampleReportFactoryWithoutRecords implements ReportBuilderFactory {
	@Override
	public ReportBuilder getReportBuilder(ModelLayerFactory modelLayerFactory) {
		// TODO: Build the model and return a ReportBuilder
		LabelsReportModel model = new LabelsReportModel();

		LabelsReportLayout labelsReportLayout = LabelsReportLayout.AVERY_5159;
		model.setLayout(labelsReportLayout);

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

		return new LabelsReportBuilder(model);
	}

	@Override
	public String getFilename() {
		// TODO: Return a filename
		return "example_report.pdf";
	}
}
