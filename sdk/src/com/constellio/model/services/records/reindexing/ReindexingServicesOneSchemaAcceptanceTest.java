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
package com.constellio.model.services.records.reindexing;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;

public class ReindexingServicesOneSchemaAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.plusHours(5);

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	//TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	RecordServices recordServices;
	ReindexingServices reindexingServices;
	RecordDao recordDao;

	Users users = new Users();
	String dakotaId;

	@Before
	public void setup()
			throws Exception {
		givenDisabledAfterTestValidations();
		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		recordDao = getDataLayerFactory().newRecordDao();

		givenCollection(zeCollection).withAllTestUsers().andUsersWithWriteAccess(dakota);
		defineSchemasManager().using(schemas.with(copiedAndCalculatedMetadatas()));

		dakotaId = users.setUp(getModelLayerFactory().newUserServices()).dakotaLIndienIn(zeCollection).getId();
	}

	@Test
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_1()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		RecordDTO record = recordDao.get("000042");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertThatRecord(withId("000666"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value4")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

	}

	@Test
	public void givenLogicallyDeletedRecordWhenReindexingThenStillLogicallyDeleted_1()
			throws RecordServicesException {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1");
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertActiveIndexForRecord("000042");

		recordServices.logicallyDelete(record("000042"), User.GOD);

		assertNoActiveIndexForRecord("000042");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));

		assertNoActiveIndexForRecord("000042");

	}

	@Test
	public void givenLogicallyDeletedRecordWhenReindexingThenStillLogicallyDeleted_2()
			throws RecordServicesException {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1");
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertActiveIndexForRecord("000666");

		recordServices.logicallyDelete(record("000666"), User.GOD);

		assertNoActiveIndexForRecord("000666");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));

		assertNoActiveIndexForRecord("000666");

	}

	@Test
	public void whenReindexingThenRefreshCopiedAndCalculatedMetadatas_2()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000666");
		recordServices.execute(transaction);

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

		givenTimeIs(tockOClock);
		Map<String, Object> modifiedValues = new HashMap<>();
		modifiedValues.put("copiedMetadataInput_s", "value3");
		modifiedValues.put("calculatedMetadataInput_s", "value4");

		RecordDTO record = recordDao.get("000666");
		RecordDeltaDTO recordDeltaDTO = new RecordDeltaDTO(record, modifiedValues, record.getFields());
		recordDao.execute(new TransactionDTO(RecordsFlushing.NOW()).withModifiedRecords(asList(recordDeltaDTO)));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value1")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value2");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertThatRecord(withId("000042"))
				.hasMetadataValue(zeSchema.metadata("copiedMetadata"), "value3")
				.hasMetadataValue(zeSchema.metadata("calculatedMetadata"), "value4")
				.hasMetadataValue(Schemas.CREATED_BY, dakotaId)
				.hasMetadataValue(Schemas.CREATED_ON, shishOClock)
				.hasMetadataValue(Schemas.MODIFIED_BY, dakotaId)
				.hasMetadataValue(Schemas.MODIFIED_ON, shishOClock);

	}

	@Test
	public void givenVaultWithoutIndexesWhenReindexingWithRewriteThenRebuildThem_1()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertActiveIndexForRecord("000042");
		assertActiveIndexForRecord("000666");
		assertCounterIndexForRecordWithValue("000042", 1);
		assertCounterIndexForRecordWithValue("000666", 0);

		deleteActiveIndex("000042");
		deleteActiveIndex("000666");
		deleteCounterIndex("000042");
		deleteCounterIndex("000666");

		assertNoActiveIndexForRecord("000042");
		assertNoActiveIndexForRecord("000666");
		assertNoCounterIndexForRecord("000042");
		assertNoCounterIndexForRecord("000666");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertNoActiveIndexForRecord("000042");
		assertNoActiveIndexForRecord("000666");
		assertNoCounterIndexForRecord("000042");
		assertNoCounterIndexForRecord("000666");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));

		assertActiveIndexForRecord("000042");
		assertActiveIndexForRecord("000666");
		assertCounterIndexForRecordWithValue("000042", 1);
		assertCounterIndexForRecordWithValue("000666", 0);
	}

	@Test
	public void givenVaultWithoutIndexesWhenReindexingWithRewriteThenRebuildThem_2()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000666");
		recordServices.execute(transaction);

		assertActiveIndexForRecord("000666");
		assertActiveIndexForRecord("000042");
		assertCounterIndexForRecordWithValue("000666", 1);
		assertCounterIndexForRecordWithValue("000042", 0);

		deleteActiveIndex("000666");
		deleteActiveIndex("000042");
		deleteCounterIndex("000666");
		deleteCounterIndex("000042");

		assertNoActiveIndexForRecord("000666");
		assertNoActiveIndexForRecord("000042");
		assertNoCounterIndexForRecord("000666");
		assertNoCounterIndexForRecord("000042");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertNoActiveIndexForRecord("000666");
		assertNoActiveIndexForRecord("000042");
		assertNoCounterIndexForRecord("000666");
		assertNoCounterIndexForRecord("000042");

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE).setBatchSize(1));

		assertActiveIndexForRecord("000666");
		assertActiveIndexForRecord("000042");
		assertCounterIndexForRecordWithValue("000666", 1);
		assertCounterIndexForRecordWithValue("000042", 0);
	}

	@Test
	public void givenVaultWithInvalidCounterIndexesWhenReindexingWithRewriteThenRebuildThem_1()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000666");
		recordServices.execute(transaction);

		assertActiveIndexForRecord("000666");
		assertActiveIndexForRecord("000042");
		assertCounterIndexForRecordWithValue("000666", 1);
		assertCounterIndexForRecordWithValue("000042", 0);

		modifyCounterIndex("000666", 42);
		modifyCounterIndex("000042", 666);

		assertActiveIndexForRecord("000666");
		assertActiveIndexForRecord("000042");
		assertCounterIndexForRecordWithValue("000666", 42);
		assertCounterIndexForRecordWithValue("000042", 666);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertActiveIndexForRecord("000666");
		assertActiveIndexForRecord("000042");
		assertCounterIndexForRecordWithValue("000666", 42);
		assertCounterIndexForRecordWithValue("000042", 666);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertActiveIndexForRecord("000666");
		assertActiveIndexForRecord("000042");
		assertCounterIndexForRecordWithValue("000666", 1);
		assertCounterIndexForRecordWithValue("000042", 0);
	}

	@Test
	public void givenVaultWithInvalidCounterIndexesWhenReindexingWithRewriteThenRebuildThem_2()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.metadata("copiedMetadataInput"), "value1")
				.set(zeSchema.metadata("calculatedMetadataInput"), "value2");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.metadata("referenceToZeSchema"), "000042");
		recordServices.execute(transaction);

		assertActiveIndexForRecord("000042");
		assertActiveIndexForRecord("000666");
		assertCounterIndexForRecordWithValue("000042", 1);
		assertCounterIndexForRecordWithValue("000666", 0);

		modifyCounterIndex("000042", 42);
		modifyCounterIndex("000666", 666);

		assertActiveIndexForRecord("000042");
		assertActiveIndexForRecord("000666");
		assertCounterIndexForRecordWithValue("000042", 42);
		assertCounterIndexForRecordWithValue("000666", 666);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.RECALCULATE).setBatchSize(1));

		assertActiveIndexForRecord("000042");
		assertActiveIndexForRecord("000666");
		assertCounterIndexForRecordWithValue("000042", 42);
		assertCounterIndexForRecordWithValue("000666", 666);

		reindexingServices.reindexCollections(new ReindexationParams(ReindexationMode.REWRITE).setBatchSize(1));

		assertActiveIndexForRecord("000042");
		assertActiveIndexForRecord("000666");
		assertCounterIndexForRecordWithValue("000042", 1);
		assertCounterIndexForRecordWithValue("000666", 0);
	}

	// ---------------------------------------------------

	private void modifyCounterIndex(String recordId, double value) {
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		try {
			RecordDTO record = recordDao.get("idx_rfc_" + recordId);

			Map<String, Object> modifiedFields = new HashMap<>();
			modifiedFields.put("refs_d", value);
			RecordDeltaDTO modifyValueDeltaDTO = new RecordDeltaDTO(record, modifiedFields, record.getFields());
			transaction = transaction.withModifiedRecords(asList(modifyValueDeltaDTO));
			recordDao.execute(transaction);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void deleteCounterIndex(String recordId) {
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		transaction = transaction.withDeletedByQueries(new ModifiableSolrParams().set("q", "id:idx_rfc_" + recordId));
		try {
			recordDao.execute(transaction);
		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}
	}

	private void deleteActiveIndex(String recordId) {
		TransactionDTO transaction = new TransactionDTO(RecordsFlushing.NOW());
		transaction = transaction.withDeletedByQueries(new ModifiableSolrParams().set("q", "id:idx_act_" + recordId));
		try {
			recordDao.execute(transaction);
		} catch (OptimisticLocking optimisticLocking) {
			throw new RuntimeException(optimisticLocking);
		}
	}

	private void assertActiveIndexForRecord(String recordId) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_act_" + recordId);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			fail("No active index for record id '" + recordId + "'");
		}
	}

	private void assertNoActiveIndexForRecord(String recordId) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_act_" + recordId);
			fail("No active index expected for record id '" + recordId + "'");
		} catch (NoSuchRecordWithId noSuchRecordWithId) {

		}
	}

	private void assertCounterIndexForRecordWithValue(String recordId, double expectedValue) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_rfc_" + recordId);
			Double wasValue = (Double) recordDTO.getFields().get("refs_d");
			assertThat(wasValue).isEqualTo(expectedValue);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			fail("No counter index for record id '" + recordId + "'");
		}
	}

	private void assertNoCounterIndexForRecord(String recordId) {
		try {
			RecordDTO recordDTO = recordDao.get("idx_rfc_" + recordId);
			Double wasValue = (Double) recordDTO.getFields().get("refs_d");
			fail("No counter index expected for record id '" + recordId + "'");
		} catch (NoSuchRecordWithId noSuchRecordWithId) {

		}
	}

	private MetadataSchemaTypesConfigurator copiedAndCalculatedMetadatas() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaBuilder zeSchema = schemaTypes.getDefaultSchema("zeSchemaType");
				MetadataBuilder copiedMetadataInput = zeSchema.create("copiedMetadataInput").setType(STRING);
				zeSchema.create("calculatedMetadataInput").setType(STRING);

				//MetadataSchemaBuilder anotherSchema = schemaTypes.getDefaultSchema("anotherSchemaType");
				MetadataBuilder referenceToZeSchema = zeSchema.create("referenceToZeSchema")
						.defineReferencesTo(asList(zeSchema));
				zeSchema.create("copiedMetadata").setType(STRING)
						.defineDataEntry().asCopied(referenceToZeSchema, copiedMetadataInput);
				zeSchema.create("calculatedMetadata").setType(STRING)
						.defineDataEntry().asCalculated(ReindexingServicesAcceptanceTest_Calculator.class);

			}
		};
	}

	public static class ReindexingServicesAcceptanceTest_Calculator implements MetadataValueCalculator<String> {

		ReferenceDependency<String> inputDependency = ReferenceDependency
				.toAString("referenceToZeSchema", "calculatedMetadataInput");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameters.get(inputDependency);
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(inputDependency);
		}
	}
}
