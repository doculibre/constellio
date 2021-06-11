package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.Content;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class MetadataPrintableGenerator extends PrintableGenerator {

	public MetadataPrintableGenerator(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
	}

	@Override
	public InputStream generate(PrintableGeneratorParams params) throws Exception {
		InputStream jasperInputStream = null;
		try {
			if (params.getPrintableId() != null) {
				Printable printable = rm.getPrintable(params.getPrintableId());
				Content content = printable.get(Printable.JASPERFILE);
				jasperInputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), getClass().getSimpleName());
			} else {
				File defaultTemplate = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "DocumentMetadataReport.jasper");
				jasperInputStream = new FileInputStream(defaultTemplate);
			}

			String xml = generateXml(params);
			org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);

			return generate(PrintableExtension.PDF, jasperInputStream, parameters);
		} finally {
			IOUtils.closeQuietly(jasperInputStream);
		}
	}
}
