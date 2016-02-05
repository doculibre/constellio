package com.constellio.data.dao.services.bigVault.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import com.constellio.data.utils.ConsoleLogger;

public class BigVaultLogger {

	private boolean enabled;

	private List<String> loggedFields;

	private BigVaultLogger(List<String> loggedFields, boolean enabled) {
		this.loggedFields = loggedFields;
		this.enabled = enabled;
	}

	public static BigVaultLogger forConfig(Map<String, String> configs) {
		String loggedEnabled = configs.get("bigVaultLogger_enabled");
		if ("TRUE".equalsIgnoreCase(loggedEnabled)) {
			String loggedFields = configs.get("bigVaultLogger_loggedFields");
			if (StringUtils.isNotBlank(loggedFields)) {
				return new BigVaultLogger(Arrays.asList(loggedFields.split(",")), true);

			} else {
				return new BigVaultLogger(null, true);
			}
		} else {
			return disabled();
		}
	}

	public static BigVaultLogger disabled() {
		return new BigVaultLogger(null, false);
	}

	public void log(List<SolrInputDocument> newDocuments, List<SolrInputDocument> updatedDocuments) {
		if (enabled) {
			List<String> lines = new ArrayList<>();

			String header = "";
			if (!newDocuments.isEmpty()) {
				header = "Added " + newDocuments.size() + " records";
			}
			if (!updatedDocuments.isEmpty()) {
				if (header.length() > 0) {
					header += " / ";
				}
				header = "Updates " + updatedDocuments.size() + " records";
			}
			lines.add(header);
			for (SolrInputDocument inputDocument : newDocuments) {
				lines.add(toString(inputDocument));
			}
			for (SolrInputDocument inputDocument : updatedDocuments) {
				lines.add(toString(inputDocument));
			}
			ConsoleLogger.log(lines);
		}
	}

	private String toString(SolrInputDocument inputDocument) {
		StringBuilder lineBuilder = new StringBuilder("\t");
		lineBuilder.append(inputDocument.getFieldValue("id"));
		lineBuilder.append(" { ");
		for (String fieldName : inputDocument.getFieldNames()) {
			if (loggedFields == null || loggedFields.contains(fieldName)) {
				lineBuilder.append(fieldName);
				lineBuilder.append("=");
				if (fieldName.endsWith("ss") || fieldName.endsWith("ds") || fieldName.endsWith("dts")) {
					lineBuilder.append(inputDocument.getFieldValues(fieldName));
				} else {
					lineBuilder.append(inputDocument.getFieldValue(fieldName));
				}
				lineBuilder.append(", ");
			}
		}

		lineBuilder.append(" } ");
		return lineBuilder.toString();
	}

}

