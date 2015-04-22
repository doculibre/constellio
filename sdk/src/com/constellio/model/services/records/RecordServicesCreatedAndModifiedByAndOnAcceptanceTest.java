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
import static org.mockito.Mockito.spy;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.setups.Users;

public class RecordServicesCreatedAndModifiedByAndOnAcceptanceTest extends ConstellioTest {

	RecordServicesTestSchemaSetup schemas = new RecordServicesTestSchemaSetup();
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	Record record;
	Users users = new Users();
	BatchProcessesManager batchProcessesManager;
	LocalDateTime now = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = new LocalDateTime();
	User alice, bob, dakota;
	private RecordServices recordServices;

	@Before
	public void setup()
			throws Exception {
		recordServices = spy(getModelLayerFactory().newRecordServices());
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();

		defineSchemasManager().using(schemas.withADateTimeMetadata());
		givenTimeIs(now);

		record = new TestRecord(zeSchema);
		users.setUp(getModelLayerFactory().newUserServices());

		getModelLayerFactory().newUserServices().addUserToCollection(users.alice(), zeCollection);
		getModelLayerFactory().newUserServices().addUserToCollection(users.bob(), zeCollection);
		getModelLayerFactory().newUserServices().addUserToCollection(users.dakotaLIndien(), zeCollection);

		alice = users.aliceIn(zeCollection);
		bob = users.bobIn(zeCollection);
		dakota = users.dakotaLIndienIn(zeCollection);

		recordServices.update(alice.setCollectionWriteAccess(true));
		recordServices.update(bob.setCollectionWriteAccess(true));
		recordServices.update(dakota.setCollectionWriteAccess(true));
	}

	@Test
	public void whenCreateRecordThenCreatedOnIsNowAndModifiedOnIsNow()
			throws Exception {

		Record record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		recordServices.execute(new Transaction(record).setUser(alice));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(now);

	}

	@Test
	public void whenCreateRecordWithCustomCreationDatesAndUsersThenCorrectValuesAreSet()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.CREATED_BY, bob.getId());
		record.set(Schemas.CREATED_ON, shishOClock);
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(bob.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(shishOClock);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(bob.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(shishOClock);
		assertThat(record.get(Schemas.TITLE)).isEqualTo("firstTitle");
	}

	@Test
	public void whenCreateRecordWithCustomCreationAndModificationDatesAndUsersThenCorrectValuesAreSet()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.CREATED_BY, bob.getId());
		record.set(Schemas.CREATED_ON, shishOClock);
		record.set(Schemas.MODIFIED_BY, dakota.getId());
		record.set(Schemas.MODIFIED_ON, tockOClock);
		recordServices.execute(new Transaction(record).setUser(alice));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(bob.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(shishOClock);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(dakota.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(tockOClock);

	}

	@Test
	public void whenUpdateRecordWithCustomCreationDatesAndUsersThenCorrectValuesAreSet()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));

		givenTimeIs(shishOClock);
		record.set(Schemas.TITLE, "newTitle");
		recordServices.execute(new Transaction(record).setUser(bob));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(bob.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(shishOClock);
		assertThat(record.get(Schemas.TITLE)).isEqualTo("newTitle");

	}

	@Test
	public void whenUpdateRecordWithCustomCreationAndModificationDatesAndUsersThenCorrectValuesAreSet()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		recordServices.execute(new Transaction(record).setUser(alice));

		record.set(Schemas.CREATED_BY, bob.getId());
		record.set(Schemas.CREATED_ON, shishOClock);
		record.set(Schemas.MODIFIED_BY, dakota.getId());
		record.set(Schemas.MODIFIED_ON, tockOClock);
		recordServices.execute(new Transaction(record).setUser(alice));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(bob.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(shishOClock);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(dakota.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(tockOClock);
	}

	@Test
	public void whenUpdateAsyncRecordWithCustomCreationAndModificationDatesAndUsersThenCorrectValuesAreSet()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		recordServices.executeHandlingImpactsAsync(new Transaction(record).setUser(alice));

		record.set(Schemas.CREATED_BY, bob.getId());
		record.set(Schemas.CREATED_ON, shishOClock);
		record.set(Schemas.MODIFIED_BY, dakota.getId());
		record.set(Schemas.MODIFIED_ON, tockOClock);
		recordServices.executeHandlingImpactsAsync(new Transaction(record).setUser(alice));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(bob.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(shishOClock);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(dakota.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(tockOClock);
	}

	@Test
	public void whenCreateWithoutUserThenLastModificationUserIsNull()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record));

		assertThat(record.get(Schemas.MODIFIED_BY)).isNull();
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isNull();
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(now);
	}

	@Test
	public void whenAddWithoutUserThenLastModificationUserIsNull()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.add(record);

		givenTimeIs(shishOClock);
		record.set(Schemas.TITLE, "newTitle");
		recordServices.update(record);

		assertThat(record.get(Schemas.MODIFIED_BY)).isNull();
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isNull();
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(shishOClock);
	}

	@Test
	public void whenUpdateWithoutUserThenLastModificationUserIsNull()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));

		givenTimeIs(shishOClock);
		record.set(Schemas.TITLE, "newTitle");
		recordServices.execute(new Transaction(record));

		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isNull();
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(shishOClock);
	}

	@Test
	public void whenMergingRecordThenMergeAndKeepLatestModificationInfos()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));

		givenTimeIs(shishOClock);
		Record anotherRecord = recordServices.getDocumentById(record.getId());
		anotherRecord.set(Schemas.TITLE, "newTitle");
		recordServices.execute(new Transaction(anotherRecord).setUser(bob));

		givenTimeIs(tockOClock);
		record.set(zeSchema.dateTimeMetadata(), now);
		recordServices.execute(new Transaction(record).setUser(dakota));

		assertThat(record.get(zeSchema.dateTimeMetadata())).isEqualTo(now);
		assertThat(record.get(Schemas.TITLE)).isEqualTo("newTitle");
		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(dakota.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(tockOClock);
	}

	@Test
	public void whenRecordIsNotModifiedThenKeepModificationInfos()
			throws Exception {

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));

		givenTimeIs(shishOClock);
		recordServices.execute(new Transaction(record).setUser(bob));

		recordServices.refresh(record);
		assertThat(record.get(Schemas.TITLE)).isEqualTo("firstTitle");
		assertThat(record.get(Schemas.CREATED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.CREATED_ON)).isEqualTo(now);
		assertThat(record.get(Schemas.MODIFIED_BY)).isEqualTo(alice.getId());
		assertThat(record.get(Schemas.MODIFIED_ON)).isEqualTo(now);
	}

	@Test
	public void givenCreatedByIsRequiredWhenCreatingRecordWithCreatorThenOk()
			throws Exception {

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getSchema(zeSchema.code()).getMetadata(Schemas.CREATED_BY.getLocalCode()).setDefaultRequirement(true);
		getModelLayerFactory().getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));
	}

	@Test
	public void givenModifiedByIsRequiredWhenUpdatingRecordWithModifierThenOk()
			throws Exception {

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types);
		typesBuilder.getSchema(zeSchema.code()).getMetadata(Schemas.MODIFIED_BY.getLocalCode()).setDefaultRequirement(true);
		getModelLayerFactory().getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);

		User alice = users.aliceIn(zeCollection);
		record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(Schemas.TITLE, "firstTitle");
		recordServices.execute(new Transaction(record).setUser(alice));

		recordServices.execute(new Transaction(record).setUser(alice));
	}
}