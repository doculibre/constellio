package com.constellio.app.modules.es.connectors.smb.utils;

import com.constellio.app.modules.es.connectors.smb.config.SmbRetrievalConfiguration;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.connectors.spi.ConsoleConnectorLogger;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectorSmbUtils {
	private ConnectorLogger logger = new ConsoleConnectorLogger();
	private ESSchemasRecordsServices es;

	public ConnectorSmbUtils() {
	}

	/**
	 * We first look at rejected/black URLs, then if nothing matches look for accepted/white URLs. If nothing matches, the URL is
	 * rejected.
	 */
	public boolean isAccepted(String url, ConnectorSmbInstance connectorInstance) {
		SmbRetrievalConfiguration smbRetrievalConfiguration = new SmbRetrievalConfiguration(connectorInstance.getSeeds(), connectorInstance.getInclusions(),
				connectorInstance.getExclusions(), connectorInstance.isSkipShareAccessControl(), connectorInstance.isSkipContentAndAcl());
		return isAccepted(url, smbRetrievalConfiguration);
	}

	public boolean isAccepted(String url, SmbRetrievalConfiguration smbRetrievalConfiguration) {
		if (!url.startsWith("smb://")) {
			return false;
		}
		if (StringUtils.isBlank(url)) {
			return false;
		}

		if (smbRetrievalConfiguration.getExclusions() != null && !smbRetrievalConfiguration.getExclusions()
				.isEmpty()) {
			for (String blackRegEx : smbRetrievalConfiguration.getExclusions()) {
				if (StringUtils.isNotBlank(blackRegEx)) {
					if (url.startsWith(blackRegEx)) {
						return false;
					}
					Pattern pattern = Pattern.compile(blackRegEx, Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						return false;
					}
				}
			}
		}

		if (smbRetrievalConfiguration.getInclusions() != null && !smbRetrievalConfiguration.getInclusions()
				.isEmpty()) {
			for (String whiteRegEx : smbRetrievalConfiguration.getInclusions()) {
				if (StringUtils.isNotBlank(whiteRegEx)) {
					if (url.startsWith(whiteRegEx)) {
						return true;
					}
					Pattern pattern = Pattern.compile(whiteRegEx, Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(url);
					if (matcher.find()) {
						return true;
					}
				}
			}
			return false;
		}
		// Fallback to seeds
		if (smbRetrievalConfiguration.getSeeds() != null) {
			for (String whiteRegEx : smbRetrievalConfiguration.getSeeds()) {
				if (StringUtils.isNotBlank(whiteRegEx)) {
					if (url.startsWith(whiteRegEx)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isFolder(String url) {
		if (StringUtils.endsWith(url, "/")) {
			return true;
		} else {
			return false;
		}
	}

	public String getStackTrace(Exception e) {
		// http://stackoverflow.com/questions/4812570/how-to-store-printstacktrace-into-a-string
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}
}
