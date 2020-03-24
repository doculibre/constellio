package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.dao.services.transactionLog.sql.TransactionDocumentLogContent;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonTransactionLogParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTransactionLogParser.class);

	public TransactionLogContent parse(String jsonContent) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();

		TransactionLogContent transaction = objectMapper.readValue(jsonContent, TransactionLogContent.class);
		List<TransactionDocumentLogContent> updates = transaction.getUpdatedDocuments();

		List<TransactionDocumentLogContent> filteredUpdates = new ArrayList<>();
		for (TransactionDocumentLogContent update : updates) {
			update.getFields().remove("set markedForReindexing_s");
			update.getFields().remove("set markedForParsing_s");
			update.getFields().remove("set markedForPreviewConversion_s");

			if (!update.getFields().isEmpty()) {
				LOGGER.info("Updating ");
				filteredUpdates.add(update);
			}
		}
		transaction.setUpdatedDocuments(filteredUpdates);

		return transaction;
	}
}
