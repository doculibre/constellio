/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.batch.xml.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.filter.Filters;
import org.jdom2.util.IteratorIterable;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessWriterTest extends ConstellioTest {

	private static final String COMPUTER_NAME = "computerName";
	private static final String BATCH_PROCESS_PART = "batchProcessPart";
	private static final String METADATAS = "metadatas";
	private static final String ERRORS = "errors";
	private static final String RECORDS = "records";
	private static final String REQUEST_DATE_TIME = "requestDateTime";
	private static final String ID = "id";
	Document document;
	BatchProcessWriter writer;
	List<String> records;
	Element batchProcessElement;
	LocalDateTime requestDatetime;

	@Before
	public void setup()
			throws Exception {

		records = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			records.add("record" + i);
		}

		document = new Document();
		writer = new BatchProcessWriter(document);
		requestDatetime = aDateTime();

		writer.newBatchProcess("1", requestDatetime, records);
		batchProcessElement = document.getRootElement();
	}

	@Test
	public void whenNewBatchProcessThenItIsCreated()
			throws Exception {

		assertThat(batchProcessElement.getChild(ID).getText()).isEqualTo("1");
		assertThat(batchProcessElement.getChild(REQUEST_DATE_TIME).getText()).isEqualTo(requestDatetime.toString());
		assertThat(batchProcessElement.getChild(RECORDS).getChildren()).hasSize(records.size());
		for (int i = 0; i < records.size(); i++) {
			assertThat(batchProcessElement.getChild(RECORDS).getChildren().get(i).getText()).isEqualTo(records.get(i)).isEqualTo(
					"record" + i);
		}
		assertThat(batchProcessElement.getChild(ERRORS).getChildren()).isEmpty();
	}

	@Test
	public void whenAssignBatchProcessPartToWith100RecordsThen100RecordIsMovedToBatchProcessPart()
			throws Exception {

		writer.assignBatchProcessPartTo("computer1", 100);

		assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getAttributeValue(COMPUTER_NAME)).isEqualTo("computer1");
		assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getChild(RECORDS).getChildren()).hasSize(100);
		assertThat(batchProcessElement.getChild(RECORDS).getChildren()).hasSize(900);
		for (int i = 0; i < 100; i++) {
			assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getChild(RECORDS).getChildren().get(i).getText())
					.isEqualTo(records.get(i)).isEqualTo("record" + i);
		}
		for (int i = 0; i < 900; i++) {
			assertThat(batchProcessElement.getChild(RECORDS).getChildren().get(i).getText()).isEqualTo("record" + (i + 100));
		}
	}

	@Test
	public void given60RecordMissingWhenAssignBatchProcessPartToWith100RecordsThen60RecordIsMovedToBatchProcessPart()
			throws Exception {

		writer.assignBatchProcessPartTo("computer1", 940);
		writer.assignBatchProcessPartTo("computer2", 100);

		List<Element> batchProcessPartElements = getBatchProcessPartElements();
		assertThat(batchProcessPartElements.get(0).getAttributeValue(COMPUTER_NAME)).isEqualTo("computer1");
		assertThat(batchProcessPartElements.get(0).getChild(RECORDS).getChildren()).hasSize(940);
		assertThat(batchProcessPartElements.get(1).getAttributeValue(COMPUTER_NAME)).isEqualTo("computer2");
		assertThat(batchProcessPartElements.get(1).getChild(RECORDS).getChildren()).hasSize(60);
		assertThat(batchProcessElement.getChild(RECORDS).getChildren()).isEmpty();
		for (int i = 0; i < 940; i++) {
			assertThat(batchProcessPartElements.get(0).getChild(RECORDS).getChildren().get(i).getText())
					.isEqualTo(records.get(i)).isEqualTo("record" + i);
		}
		for (int i = 0; i < 60; i++) {
			assertThat(batchProcessPartElements.get(1).getChild(RECORDS).getChildren().get(i).getText()).isEqualTo(
					"record" + (i + 940));
		}
	}

	@Test
	public void givenNoRecordMissingWhenAssignBatchProcessPartToThenEmptyListIsReturned()
			throws Exception {

		writer.assignBatchProcessPartTo("computer1", 900);
		writer.assignBatchProcessPartTo("computer2", 100);

		assertThat(writer.assignBatchProcessPartTo("computer3", 100)).isEmpty();
	}

	@Test(expected = BatchProcessWriterRuntimeException.AlreadyProcessingABatchProcessPart.class)
	public void whenAssignBatchProcessPartToTwiceForTheSameComputerThenException()
			throws Exception {

		writer.assignBatchProcessPartTo("computer1", 100);
		writer.assignBatchProcessPartTo("computer1", 100);

		List<Element> batchProcessPartElements = getBatchProcessPartElements();
		assertThat(batchProcessPartElements).hasSize(1);
		assertThat(batchProcessPartElements.get(0).getAttributeValue(COMPUTER_NAME)).isEqualTo("computer1");
		assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getChild(RECORDS).getChildren()).hasSize(100);
		assertThat(batchProcessElement.getChild(RECORDS).getChildren()).hasSize(900);
		for (int i = 0; i < 100; i++) {
			assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getChild(RECORDS).getChildren().get(i).getText())
					.isEqualTo(records.get(i)).isEqualTo("record" + i);
		}
		for (int i = 0; i < 900; i++) {
			assertThat(batchProcessElement.getChild(RECORDS).getChildren().get(i).getText()).isEqualTo("record" + (i + 100));
		}
	}

	@Test
	public void givenErrorsWhenMarkHasDoneThenErrorsInListAndNoBatchProcessForThisComputer()
			throws Exception {
		List<String> recordsWithErrors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			recordsWithErrors.add("record" + i);
		}

		writer.assignBatchProcessPartTo("computer1", 100);
		writer.markHasDone("computer1", recordsWithErrors);

		assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART)).isNull();
		assertThat(batchProcessElement.getChild(RECORDS).getChildren()).hasSize(900);
		assertThat(batchProcessElement.getChild(ERRORS).getChildren()).hasSize(recordsWithErrors.size());
	}

	@Test(expected = BatchProcessWriterRuntimeException.ComputerNotFound.class)
	public void givenInexistentComputerWhenMarkHasDoneThenException()
			throws Exception {
		List<String> recordsWithErrors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			recordsWithErrors.add("record" + i);
		}

		writer.assignBatchProcessPartTo("computer1", 100);
		writer.markHasDone("computer2", recordsWithErrors);

		List<Element> batchProcessPartElements = getBatchProcessPartElements();
		assertThat(batchProcessPartElements).hasSize(1);
		assertThat(batchProcessPartElements.get(0).getAttributeValue(COMPUTER_NAME)).isEqualTo("computer1");
		assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getChild(RECORDS).getChildren()).hasSize(100);
		assertThat(batchProcessElement.getChild(RECORDS).getChildren()).hasSize(900);
		for (int i = 0; i < 100; i++) {
			assertThat(batchProcessElement.getChild(BATCH_PROCESS_PART).getChild(RECORDS).getChildren().get(i).getText())
					.isEqualTo(records.get(i)).isEqualTo("record" + i);
		}
		for (int i = 0; i < 900; i++) {
			assertThat(batchProcessElement.getChild(RECORDS).getChildren().get(i).getText()).isEqualTo("record" + (i + 100));
		}
	}

	@SuppressWarnings("unchecked")
	private List<Element> getBatchProcessPartElements() {
		Filter<Element> filters = Filters.element(BATCH_PROCESS_PART);
		IteratorIterable<Element> batchProcessPartElement = batchProcessElement.getDescendants(filters);
		List<Element> batchProcessPartElements = IteratorUtils.toList(batchProcessPartElement);
		return batchProcessPartElements;
	}
}