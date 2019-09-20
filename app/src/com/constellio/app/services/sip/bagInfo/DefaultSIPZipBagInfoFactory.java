package com.constellio.app.services.sip.bagInfo;

import com.constellio.app.services.factories.AppLayerFactory;
import org.apache.commons.io.FileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

public class DefaultSIPZipBagInfoFactory implements SIPZipBagInfoFactory {

	private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

	private List<String> headerLines = new ArrayList<>();

	private AppLayerFactory appLayerFactory;

	private Locale locale;

	public DefaultSIPZipBagInfoFactory(AppLayerFactory appLayerFactory, Locale locale) {
		this.appLayerFactory = appLayerFactory;
		this.locale = locale;
	}

	public List<String> getHeaderLines() {
		return headerLines;
	}

	public DefaultSIPZipBagInfoFactory setHeaderLines(List<String> headerLines) {
		this.headerLines = headerLines;
		return this;
	}

	@Override
	public String buildBagInfoContent(SIPZipInfos zipInfos) {

		StringBuilder content = new StringBuilder();
		if (headerLines != null) {
			for (String headerLine : headerLines) {
				content.append(headerLine).append("\n");
			}
		}

		if (content.length() > 0) {
			content.append("\n");
		}

		content.append("Nombre de fichiers numériques : ").append(zipInfos.getTotalFilesCount()).append("\n");

		content.append("Portrait général des formats numériques : ");
		boolean first = true;
		for (Entry<String, Integer> entry : zipInfos.getExtensionAndCount().entriesSortedByDescValue()) {
			if (!first) {
				content.append(", ");
			}
			content.append(entry.getKey()).append("(").append(entry.getValue()).append(")");
			first = false;
		}
		content.append("\n\n");

		content.append("Taille des fichiers numériques non compressés : ")
				.append(FileUtils.byteCountToDisplaySize(zipInfos.getUncompressedLengthOfFiles()))
				.append(" (")
				.append(zipInfos.getUncompressedLengthOfFiles())
				.append(" octets").append(")").append("\n\n");

		content.append("Logiciel : Constellio").append("\n");
		content.append("Site web de l’éditeur : http://www.constellio.com").append("\n");

		String currentVersion = appLayerFactory.newApplicationService().getWarVersion();
		content.append("Version du logiciel : ").append(currentVersion).append("\n");
		content.append("Date de création du paquet : ").append(sdfDate.format(zipInfos.getCreationTime().toDate()));
		return content.toString();
	}
}
