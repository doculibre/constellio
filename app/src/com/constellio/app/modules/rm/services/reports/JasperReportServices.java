package com.constellio.app.modules.rm.services.reports;

import com.constellio.app.modules.rm.services.reports.printable.PrintableGenerator;
import com.constellio.app.modules.rm.services.reports.printable.PrintableGeneratorFactory;
import com.constellio.app.modules.rm.services.reports.printable.PrintableGeneratorParams;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGenerator;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGeneratorFactory;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGeneratorParams;
import com.constellio.app.services.factories.AppLayerFactory;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperReportsContext;

import java.io.InputStream;

public class JasperReportServices {

	private final XMLDataSourceGeneratorFactory dataSourceGeneratorFactory;
	private final PrintableGeneratorFactory printableGeneratorFactory;

	public JasperReportServices(String collection, AppLayerFactory appLayerFactory) {
		dataSourceGeneratorFactory = new XMLDataSourceGeneratorFactory(collection, appLayerFactory);
		printableGeneratorFactory = new PrintableGeneratorFactory(collection, appLayerFactory);

		JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		jasperReportsContext.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		jasperReportsContext.setProperty("net.sf.jasperreports.default.font.name", "Arial");
	}

	public InputStream generateXmlDataSource(XMLDataSourceGeneratorParams params) {
		XMLDataSourceGenerator generator = dataSourceGeneratorFactory.createXMLDataSourceGenerator(params.getSchemaType(),
				params.getXmlDataSourceType());
		return generator.generate(params);
	}

	public InputStream generatePrintable(PrintableGeneratorParams params) throws Exception {
		PrintableGenerator generator = printableGeneratorFactory.createPrintableGenerator(params.getXMLDataSourceType());
		return generator.generate(params);
	}

}
