package com.constellio.data.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;

public class LoggerUtils {

	public static void logDocument(Logger logger, String comment, Document document) {
		Format format = Format.getPrettyFormat();
		StringWriter stringWriter = new StringWriter();
		try {
			new XMLOutputter(format).output(document, stringWriter);
			logger.info(comment + " : \n" + stringWriter.getBuffer().toString());
			stringWriter.close();
		} catch (IOException e) {
			throw new ImpossibleRuntimeException(e);
		}

	}

	public static String toString(TransactionDTO transaction) {
		StringBuilder logBuilder = new StringBuilder();

		for (RecordDTO recordDTO : transaction.getNewRecords()) {
			logBuilder.append("\n\t" + toString(recordDTO));
		}

		for (RecordDeltaDTO recordDeltaDTO : transaction.getModifiedRecords()) {
			logBuilder.append("\n\t" + toString(recordDeltaDTO));
		}

		return logBuilder.toString();
	}

	public static String toString(RecordDTO recordDTO) {
		StringBuilder log = new StringBuilder();

		log.append(recordDTO.getId() + ">>  ");
		for (Map.Entry<String, Object> field : recordDTO.getFields().entrySet()) {
			log.append(field.getKey());
			log.append(":");
			log.append(field.getValue());
			log.append("; ");
		}
		for (Map.Entry<String, Object> field : recordDTO.getCopyFields().entrySet()) {
			log.append(field.getKey());
			log.append(":");
			log.append(field.getValue());
			log.append("; ");
		}

		return log.toString();
	}

	public static String toString(RecordDeltaDTO recordDeltaDTO) {
		StringBuilder log = new StringBuilder();

		log.append(recordDeltaDTO.getId() + ">>  ");
		for (String modifiedField : recordDeltaDTO.getModifiedFields().keySet()) {
			log.append(modifiedField);
			log.append(":");
			log.append(recordDeltaDTO.getInitialFields().get(modifiedField));
			log.append("=>");
			log.append(recordDeltaDTO.getModifiedFields().get(modifiedField));
			log.append("; ");
		}

		return log.toString();
	}

	public static String toParamsString(SolrParams params) {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<String> itIterator = params.getParameterNamesIterator();
		boolean first = true;
		while (itIterator.hasNext()) {
			String key = itIterator.next();
			for (String value : params.getParams(key)) {
				if (!first) {
					stringBuilder.append(",  ");
				}
				stringBuilder.append(key);
				stringBuilder.append("=");
				stringBuilder.append(value);
				first = false;
			}
		}
		return stringBuilder.toString();
	}

	private void logExceptionWhileAdding(Logger logger, List<SolrInputDocument> inputDocuments, Exception e) {
		for (SolrInputDocument inputDocument : inputDocuments) {
			logger.info("ADD document '" + inputDocument.getFieldValue("id") + "' with version '" + inputDocument
					.getFieldValue("_version_") + "'");
		}
		logger.error("SolrServerException", e);
	}

}
