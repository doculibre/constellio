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
package com.constellio.model.services.records;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordUtilsAcceptanceTestSchemasSetup.DocumentSchema;
import com.constellio.model.services.records.RecordUtilsAcceptanceTestSchemasSetup.FolderSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;

public class RecordUtilsAcceptanceTest extends ConstellioTest {

	RecordUtils recordUtils;

	RecordUtilsAcceptanceTestSchemasSetup schemas = new RecordUtilsAcceptanceTestSchemasSetup();
	FolderSchema folderSchema = schemas.new FolderSchema();
	DocumentSchema documentSchema = schemas.new DocumentSchema();
	private MetadataSchemasManager schemasManager;
	private Record parentFolder1;
	private Record childFolder1;
	private Record document1;
	private Record parentFolder2;
	private Record childFolder2;
	private Record document2;

	@Before
	public void setUp()
			throws Exception {
		recordUtils = new RecordUtils();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		defineSchemasManager().using(schemas.withFolderAndDocumentSchemas());
		parentFolder1 = new TestRecord(folderSchema, "parentFolder1");
		childFolder1 = new TestRecord(folderSchema, "childFolder1");
		childFolder1.set(folderSchema.parent(), parentFolder1);
		document1 = new TestRecord(documentSchema, "document1");
		document1.set(documentSchema.parent(), childFolder1);

		parentFolder2 = new TestRecord(folderSchema, "parentFolder2");
		childFolder2 = new TestRecord(folderSchema, "childFolder2");
		childFolder2.set(folderSchema.parent(), parentFolder2);
		document2 = new TestRecord(documentSchema, "document2");
		document2.set(documentSchema.parent(), childFolder2);
	}

	@Test
	public void givenOneHierarchyOf3RecordsThenRecordsSortedRight()
			throws Exception {
		List<Record> records = new ArrayList<>();

		records.add(document1);
		records.add(parentFolder1);
		records.add(childFolder1);

		List<Record> sortedRecords = recordUtils.sortRecordsOnDependencies(records, schemasManager.getSchemaTypes(zeCollection));

		assertThat(sortedRecords).containsExactly(parentFolder1, childFolder1, document1);
	}

	@Test
	public void givenTwoHierarchyOf3RecordsThenRecordsSortedRight()
			throws Exception {
		List<Record> records = new ArrayList<>();

		records.add(document2);
		records.add(childFolder2);
		records.add(document1);
		records.add(parentFolder1);
		records.add(childFolder1);
		records.add(parentFolder2);

		List<Record> sortedRecords = recordUtils.sortRecordsOnDependencies(records, schemasManager.getSchemaTypes(zeCollection));

		assertThat(sortedRecords.indexOf(parentFolder1)).isLessThan(sortedRecords.indexOf(childFolder1));
		assertThat(sortedRecords.indexOf(childFolder1)).isLessThan(sortedRecords.indexOf(document1));
		assertThat(sortedRecords.indexOf(parentFolder2)).isLessThan(sortedRecords.indexOf(childFolder2));
		assertThat(sortedRecords.indexOf(childFolder2)).isLessThan(sortedRecords.indexOf(document2));
	}

}
