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

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class BatchProcessReaderTest extends ConstellioTest {

	Document document;
	BatchProcessWriter writer;
	BatchProcessReader reader;
	List<String> recordsWithErrors;
	List<String> recordsWithErrors2;
	List<String> records;
	Element batchProcessElement;
	LocalDateTime requestDatetime;

	@Before
	public void setup()
			throws Exception {

		recordsWithErrors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			recordsWithErrors.add("record" + i);
		}
		recordsWithErrors2 = new ArrayList<>();
		for (int i = 5; i < 11; i++) {
			recordsWithErrors2.add("record" + i);
		}
		records = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			records.add("record" + i);
		}

		document = new Document();
		writer = new BatchProcessWriter(document);
		requestDatetime = aDateTime();

		writer.newBatchProcess("1", requestDatetime, records);
		batchProcessElement = document.getRootElement();
		reader = new BatchProcessReader(document);

	}

	@Test
	public void given10RecordsWithErrorWhenGetRecordsWithErrorsThenAListOfStringIsReturned()
			throws Exception {
		List<String> recordsWithErrors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			recordsWithErrors.add("record" + i);
		}
		List<String> recordsWithErrors2 = new ArrayList<>();
		for (int i = 5; i < 10; i++) {
			recordsWithErrors2.add("record" + i);
		}
		writer.assignBatchProcessPartTo("computer1", 100);
		writer.assignBatchProcessPartTo("computer2", 100);
		writer.markHasDone("computer1", recordsWithErrors);
		writer.markHasDone("computer2", recordsWithErrors2);

		List<String> recordsWithError = reader.getRecordsWithError();

		assertThat(recordsWithError).hasSize(10);
		for (int j = 0; j < recordsWithError.size(); j++) {
			assertThat(recordsWithError.get(j)).isEqualTo("record" + j);
		}
	}

	@Test
	public void givenNoRecordsWithErrorWhenGetRecordsWithErrorsThenAEmptyListIsReturned()
			throws Exception {
		List<String> recordsWithErrors = new ArrayList<>();
		writer.assignBatchProcessPartTo("computer1", 100);
		writer.assignBatchProcessPartTo("computer2", 100);
		writer.markHasDone("computer1", recordsWithErrors);
		writer.markHasDone("computer2", recordsWithErrors);

		List<String> recordsWithError = reader.getRecordsWithError();

		assertThat(recordsWithError).isEmpty();
	}
}