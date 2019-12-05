package com.constellio.data.dao.services.transactionLog.reader1;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

public class TransactionJsonMapperObjectReaderV1 {

	public TransactionJsonMapperObjectReaderV1(){

	}

	public TransactionLogContent transactionLogSqlContentDeserialize(String json) throws IOException {

		ObjectMapper objectMapper = new ObjectMapper();
		TransactionLogContent transactionLogs1Content = objectMapper.readValue(json, TransactionLogContent.class);
		return transactionLogs1Content;
	}
}
