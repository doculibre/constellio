package com.constellio.data.dao.services.transactionLog;

import com.constellio.data.dao.services.transactionLog.sql.TransactionDocumentLogContent;
import com.constellio.data.dao.services.transactionLog.sql.TransactionLogContent;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.data.MapEntry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTransactionLogParserAcceptanceTest extends ConstellioTest {

	@Test
	public void givenTransactionReadThenRemoveUpdatesWithOnlyMarkedForReindexingMarkerThenSkipThatUpdate()
			throws Exception {

		JsonTransactionLogParser parser = new JsonTransactionLogParser();
		String json = getTestResourceContent("markedForReindexingUpdate.txt");
		TransactionLogContent transaction = parser.parse(json);

		assertThat(transaction.getNewDocuments()).hasSize(1);
		assertThat(transaction.getUpdatedDocuments()).hasSize(0);

	}

	@Test
	public void givenTransactionReadThenRemoveUpdatesWithMarkedForReindexingMarkerAndOtherFieldThenOnlyKeepOtherFields()
			throws Exception {

		JsonTransactionLogParser parser = new JsonTransactionLogParser();
		String json = getTestResourceContent("markedForReindexingAndOtherFieldUpdate.txt");
		TransactionLogContent transaction = parser.parse(json);

		assertThat(transaction.getNewDocuments()).hasSize(1);
		assertThat(transaction.getUpdatedDocuments()).hasSize(1);
		TransactionDocumentLogContent update = transaction.getUpdatedDocuments().get(0);
		assertThat(update.getFields()).containsOnly(
				MapEntry.entry("set pouet_s", "pouetpouetvalue")
		);

	}
}
