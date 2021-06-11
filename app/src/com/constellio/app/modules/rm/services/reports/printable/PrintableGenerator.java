package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGenerator;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGeneratorFactory;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceGeneratorParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordHierarchyServices;
import com.constellio.model.services.records.RecordServices;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static com.constellio.app.modules.rm.enums.TemplateVersionType.CONSTELLIO_5;

public abstract class PrintableGenerator {

	protected final AppLayerFactory appLayerFactory;
	protected final ModelLayerFactory modelLayerFactory;
	protected final DataLayerFactory dataLayerFactory;
	protected final String collection;

	protected final RMSchemasRecordsServices rm;
	protected final ContentManager contentManager;
	protected final RecordServices recordServices;
	protected final RecordHierarchyServices recordHierarchyServices;

	public PrintableGenerator(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		dataLayerFactory = modelLayerFactory.getDataLayerFactory();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		recordHierarchyServices = new RecordHierarchyServices(appLayerFactory.getModelLayerFactory());
	}

	abstract public InputStream generate(PrintableGeneratorParams params) throws Exception;

	protected String generateXml(PrintableGeneratorParams printableGeneratorParams) throws Exception {
		XMLDataSourceGeneratorParams xmlDataSourceGeneratorParams = XMLDataSourceGeneratorParams.builder()
				.xmlDataSourceType(printableGeneratorParams.getXMLDataSourceType())
				.locale(printableGeneratorParams.getLocale())
				.username(printableGeneratorParams.getUsername())
				.recordIds(printableGeneratorParams.getRecordIds())
				.schemaType(printableGeneratorParams.getSchemaType())
				.numberOfCopies(printableGeneratorParams.getNumberOfCopies())
				.query(printableGeneratorParams.getQuery())
				.startingPosition(printableGeneratorParams.getStartingPosition())
				.requiredMetadataCodes(printableGeneratorParams.getRequiredMetadataCodes())
				.depth(printableGeneratorParams.getDepth())
				.ignoreReferences(printableGeneratorParams.isIgnoreReferences())
				.isXmlForTest(false)
				.build();
		XMLDataSourceGeneratorFactory factory = new XMLDataSourceGeneratorFactory(collection, appLayerFactory);
		XMLDataSourceGenerator generator = factory.createXMLDataSourceGenerator(printableGeneratorParams.getSchemaType(),
				printableGeneratorParams.getXMLDataSourceType(), getTemplateVersion(printableGeneratorParams.getPrintableId()));
		InputStream xmlInputStream = generator.generate(xmlDataSourceGeneratorParams);
		return IOUtils.toString(xmlInputStream, StandardCharsets.UTF_8.name());
	}

	protected TemplateVersionType getTemplateVersion(String printableId) {
		return printableId != null ? rm.getPrintable(printableId).getTemplateVersion() : CONSTELLIO_5;
	}

	protected InputStream generate(PrintableExtension printableExtension, InputStream jasperInputStream,
								   Map<String, Object> parameters) throws Exception {
		IOServices ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		String tempFilename = UUID.randomUUID().toString();
		File tempJasperPrintFile = ioServices.newTemporaryFile(tempFilename + ".jrprint");
		File tempReportFile = ioServices.newTemporaryFile(tempFilename + printableExtension.getExtension());
		try (OutputStream tempJasperPrintOutputStream = new FileOutputStream(tempJasperPrintFile)) {
			JasperFillManager.fillReportToStream(jasperInputStream, tempJasperPrintOutputStream, parameters);

			JRAbstractExporter exporter = getExporter(printableExtension);
			exporter.setExporterInput(new SimpleExporterInput(tempJasperPrintFile));
			exporter.setExporterOutput(getExporterOutput(printableExtension, tempReportFile));
			exporter.exportReport();

			return new ByteArrayInputStream(FileUtils.readFileToByteArray(tempReportFile));
		} finally {
			ioServices.deleteQuietly(tempJasperPrintFile);
			ioServices.deleteQuietly(tempReportFile);
		}
	}

	private JRAbstractExporter getExporter(PrintableExtension extension) {
		switch (extension) {
			case PDF:
				return new JRPdfExporter();
			case DOCX:
				return new JRDocxExporter();
			case XLSX:
				return new JRXlsxExporter();
			case HTML:
				return new HtmlExporter();
			default:
				throw new UnsupportedOperationException("Unsupported PrintableExtension : " + extension.name());
		}
	}

	private ExporterOutput getExporterOutput(PrintableExtension extension, File file) {
		if (extension == PrintableExtension.HTML) {
			return new SimpleHtmlExporterOutput(file);
		}
		return new SimpleOutputStreamExporterOutput(file);
	}

}
