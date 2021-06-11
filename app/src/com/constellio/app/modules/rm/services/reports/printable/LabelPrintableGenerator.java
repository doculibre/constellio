package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRXmlUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class LabelPrintableGenerator extends PrintableGenerator {

	public LabelPrintableGenerator(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
	}

	@Override
	public InputStream generate(PrintableGeneratorParams params) throws Exception {
		PrintableLabel printableLabel = rm.getPrintableLabel(params.getPrintableId());
		Content content = printableLabel.get(Printable.JASPERFILE);

		try (InputStream jasperInputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), getClass().getSimpleName())) {
			org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(generateXml(params).getBytes(StandardCharsets.UTF_8)));
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);

			return generate(PrintableExtension.PDF, jasperInputStream, parameters);
		}
	}
}
