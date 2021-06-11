package com.constellio.app.ui.pages.management.schemas.metadata.reports;

import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.data.utils.TimeProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static com.constellio.app.ui.i18n.i18n.$;

public class UniqueMetadataDuplicateExcelReportFactory implements NewReportWriterFactory<UniqueMetadataDuplicateExcelReportParameters> {

	public UniqueMetadataDuplicateExcelReportFactory() {
	}

	@Override
	public ReportWriter getReportBuilder(UniqueMetadataDuplicateExcelReportParameters parameters) {
		UniqueMetadataDuplicateExcelReportModel model = new UniqueMetadataDuplicateExcelReportModel(parameters);

		return new UniqueMetadataDuplicateExcelReportWriter(model);
	}

	@Override
	public String getFilename(UniqueMetadataDuplicateExcelReportParameters parameters) {
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH_mm_ss");
		String formattedDate = df.format(TimeProvider.getLocalDateTime().toDate());

		return $("UniqueMetadataDuplicateExcelReport.title", parameters.getMetadata().getLocalCode(), formattedDate);
	}
}
