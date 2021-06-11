package com.constellio.model.services.migrations;

import com.constellio.data.utils.AccentApostropheCleaner;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.migrations.RecordMigrationsManagerRuntimeException.RecordMigrationsManagerRuntimeException_ScriptNotRegistered;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesTestSchemaSetup;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.entities.schemas.Schemas.MIGRATION_DATA_VERSION;
import static com.constellio.sdk.tests.TestUtils.assertThatAllRecordsOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class RecordMigrationsManagerAcceptanceTest extends ConstellioTest {

	RequiredRecordMigrations migrations;
	RecordServicesTestSchemaSetup schemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas zeSchema;
	RecordServicesTestSchemaSetup.AnotherSchemaMetadatas anotherSchema;
	RecordServicesTestSchemaSetup.ThirdSchemaMetadatas thirdSchema;

	RecordServicesTestSchemaSetup schemasInAnotherCollection;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas zeSchemaInAnotherCollection;
	RecordServicesTestSchemaSetup.AnotherSchemaMetadatas anotherSchemaInAnotherCollection;
	RecordServicesTestSchemaSetup.ThirdSchemaMetadatas thirdSchemaInAnotherCollection;

	RecordServices recordServices;
	RecordMigrationsManager recordMigrationsManager;
	ReindexingServices reindexingServices;

	@Before
	public void setup()
			throws Exception {
		schemas = new RecordServicesTestSchemaSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		thirdSchema = schemas.new ThirdSchemaMetadatas();

		schemasInAnotherCollection = new RecordServicesTestSchemaSetup("anotherCollection");
		zeSchemaInAnotherCollection = schemasInAnotherCollection.new ZeSchemaMetadatas();
		anotherSchemaInAnotherCollection = schemasInAnotherCollection.new AnotherSchemaMetadatas();
		thirdSchemaInAnotherCollection = schemasInAnotherCollection.new ThirdSchemaMetadatas();

		defineSchemasManager().using(schemas.withAStringMetadata().withANumberMetadata());
		defineSchemasManager().using(schemasInAnotherCollection.withAStringMetadata().withANumberMetadata());

		recordServices = getModelLayerFactory().newRecordServices();
		recordMigrationsManager = getModelLayerFactory().getRecordMigrationsManager();

		reindexingServices = getModelLayerFactory().newReindexingServices();
	}

	@Test
	public void givenRegisteredRecordScriptsThenReturnedWhenMigratingRecord()
			throws Exception {
		givenBackgroundThreadsEnabled();
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "vodka").set(zeSchema.stringMetadata(), "Vodka Framboise"));
		transaction.add(new TestRecord(zeSchema, "edouard").set(zeSchema.stringMetadata(), "Édouard Lechat"));
		recordServices.execute(transaction);

		registerInZeCollectionAndReindex(new CountLetterE());
		waitForBatchProcess();
		assertThatAllRecordsOf(zeSchema.type()).extractingMetadatas("stringMetadata", "numberMetadata").containsOnly(
				tuple("Vodka Framboise", 1.0),
				tuple("Édouard Lechat", 2.0)
		);

		registerInZeCollectionAndReindex(new SetStringMetadata());
		waitForBatchProcess();
		assertThatAllRecordsOf(zeSchema.type()).extractingMetadatas("stringMetadata", "numberMetadata").containsOnly(
				tuple("a value defined by a script", 1.0),
				tuple("a value defined by a script", 2.0)
		);
	}

	@Test
	public void givenRecordScriptsOfMultipleSchemaTypesAndCollectionThenAllP()
			throws Exception {
		givenBackgroundThreadsEnabled();
		Record zeSchemaInZecollectionRecord1 = given(new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "Vodka Framboise"));
		Record anotherSchemaInZecollectionRecord1 = given(new TestRecord(anotherSchema)
				.set(Schemas.TITLE, "Vodka Framboise"));
		Record zeSchemaInAnotherCollectionRecord1 = given(new TestRecord(zeSchemaInAnotherCollection)
				.set(Schemas.TITLE, "Vodka Framboise"));
		registerInZeCollectionAndReindex(new CountLetterE());
		registerInZeCollectionAndReindex(new AnotherSchemaScript1());
		registerInAnotherCollectionAndReindex(new SetStringMetadata());

		Record zeSchemaInZecollectionRecord2 = given(new TestRecord(zeSchema)
				.set(zeSchema.stringMetadata(), "Édouard Lechat"));
		Record anotherSchemaInZecollectionRecord2 = given(new TestRecord(anotherSchema)
				.set(Schemas.TITLE, "Édouard Lechat"));
		Record zeSchemaInAnotherCollectionRecord2 = given(new TestRecord(zeSchemaInAnotherCollection)
				.set(Schemas.TITLE, "Édouard Lechat"));

		assertThat(zeSchemaInZecollectionRecord1.getDataMigrationVersion()).isEqualTo(0);
		assertThat(zeSchemaInZecollectionRecord2.getDataMigrationVersion()).isEqualTo(1);

		assertThat(anotherSchemaInZecollectionRecord1.getDataMigrationVersion()).isEqualTo(0);
		assertThat(anotherSchemaInZecollectionRecord2.getDataMigrationVersion()).isEqualTo(1);

		assertThat(zeSchemaInAnotherCollectionRecord1.getDataMigrationVersion()).isEqualTo(0);
		assertThat(zeSchemaInAnotherCollectionRecord2.getDataMigrationVersion()).isEqualTo(1);

		registerInZeCollectionAndReindex(new CountLetterD(), new AnotherSchemaScript2());
		registerInAnotherCollectionAndReindex(new ChangeFramboiseForCanneberge());

		RecordMigrationsManager migrationsManager = getModelLayerFactory().getRecordMigrationsManager();
		RecordMigrationsManager otherMigrationsManager = new RecordMigrationsManager(getModelLayerFactory());
		otherMigrationsManager.initialize();

		otherMigrationsManager.registerReturningTypesWithNewScripts(zeCollection, asList(new CountLetterE(),
				new AnotherSchemaScript1(), new SetStringMetadata(), new CountLetterD(),
				new AnotherSchemaScript2(), new ChangeFramboiseForCanneberge()), false);

		otherMigrationsManager.registerReturningTypesWithNewScripts("anotherCollection", asList(new CountLetterE(),
				new AnotherSchemaScript1(), new SetStringMetadata(), new CountLetterD(),
				new AnotherSchemaScript2(), new ChangeFramboiseForCanneberge()), false);

		migrations = migrationsManager.getRecordMigrationsFor(zeSchemaInZecollectionRecord1);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("CountLetterE", "CountLetterD");

		migrations = migrationsManager.getRecordMigrationsFor(anotherSchemaInZecollectionRecord1);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("AnotherSchemaScript1", "AnotherSchemaScript2");

		migrations = migrationsManager.getRecordMigrationsFor(zeSchemaInAnotherCollectionRecord1);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("SetStringMetadata", "ChangeFramboiseForCanneberge");

		migrations = migrationsManager.getRecordMigrationsFor(zeSchemaInZecollectionRecord2);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("CountLetterD");

		migrations = migrationsManager.getRecordMigrationsFor(anotherSchemaInZecollectionRecord2);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("AnotherSchemaScript2");

		migrations = migrationsManager.getRecordMigrationsFor(zeSchemaInAnotherCollectionRecord2);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("ChangeFramboiseForCanneberge");

		migrations = otherMigrationsManager.getRecordMigrationsFor(zeSchemaInZecollectionRecord1);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("CountLetterE", "CountLetterD");

		migrations = otherMigrationsManager.getRecordMigrationsFor(anotherSchemaInZecollectionRecord1);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("AnotherSchemaScript1", "AnotherSchemaScript2");

		migrations = otherMigrationsManager.getRecordMigrationsFor(zeSchemaInAnotherCollectionRecord1);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("SetStringMetadata", "ChangeFramboiseForCanneberge");

		migrations = otherMigrationsManager.getRecordMigrationsFor(zeSchemaInZecollectionRecord2);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("CountLetterD");

		migrations = otherMigrationsManager.getRecordMigrationsFor(anotherSchemaInZecollectionRecord2);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("AnotherSchemaScript2");

		migrations = otherMigrationsManager.getRecordMigrationsFor(zeSchemaInAnotherCollectionRecord2);
		assertThat(migrations.getVersion()).isEqualTo(2L);
		assertThat(migrations.getScripts()).extracting("class.simpleName")
				.containsExactly("ChangeFramboiseForCanneberge");

	}

	@Test
	public void givenScriptRegisteredTwiceThenOnlyExecutedOnce()
			throws Exception {
		givenBackgroundThreadsEnabled();
		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "vodka").set(zeSchema.stringMetadata(), "Vodka Framboise"));
		transaction.add(new TestRecord(zeSchema, "edouard").set(zeSchema.stringMetadata(), "Édouard Lechat"));
		recordServices.execute(transaction);

		registerInZeCollectionAndReindex(new CountLetterE());
		waitForBatchProcess();
		assertThatAllRecordsOf(zeSchema.type()).extractingMetadatas("stringMetadata", "numberMetadata").containsOnly(
				tuple("Vodka Framboise", 1.0),
				tuple("Édouard Lechat", 2.0)
		);

		registerInZeCollectionAndReindex(new SetStringMetadata());
		waitForBatchProcess();
		assertThatAllRecordsOf(zeSchema.type()).extractingMetadatas("stringMetadata", "numberMetadata").containsOnly(
				tuple("a value defined by a script", 1.0),
				tuple("a value defined by a script", 2.0)
		);

		registerInZeCollectionAndReindex(new CountLetterE());
		waitForBatchProcess();
		assertThatAllRecordsOf(zeSchema.type()).extractingMetadatas("stringMetadata", "numberMetadata").containsOnly(
				tuple("a value defined by a script", 1.0),
				tuple("a value defined by a script", 2.0)
		);

		recordMigrationsManager.checkScriptsToFinish();
		assertThat(countLetterEMigrateCallCounter.get()).isEqualTo(2);
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(1);
		assertThat(SetStringMetadataMigrateCallCounter.get()).isEqualTo(2);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(1);
	}

	@Test
	public void givenScriptRegisteredThenFinishScriptCalledAfterExecutionOnLastRecord()
			throws Exception {
		Transaction transaction = new Transaction();
		Record record1 = transaction.add(new TestRecord(zeSchema, "vodka").set(zeSchema.stringMetadata(), "Vodka Framboise"));
		Record record2 = transaction.add(new TestRecord(zeSchema, "edouard").set(zeSchema.stringMetadata(), "Édouard Lechat"));
		recordServices.execute(transaction);

		recordMigrationsManager
				.registerReturningTypesWithNewScripts(zeCollection, asList((RecordMigrationScript) new CountLetterE()), true);

		//  0/2 reindexed record
		recordMigrationsManager.checkScriptsToFinish();

		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterE())).isFalse();
		try {
			assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterD())).isFalse();
			fail("Exception expected");
		} catch (RecordMigrationsManagerRuntimeException_ScriptNotRegistered e) {
			//OK
		}
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(0);
		assertThat(countLetterDFinishCallCounter.get()).isEqualTo(0);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(0);

		reindex(record1);

		//  1/2 reindexed record
		recordMigrationsManager.checkScriptsToFinish();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterE())).isFalse();
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(0);
		assertThat(countLetterDFinishCallCounter.get()).isEqualTo(0);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(0);

		recordMigrationsManager
				.registerReturningTypesWithNewScripts(zeCollection, asList(new CountLetterD(), new SetStringMetadata()), true);
		registerInZeCollectionAndReindex(new SetStringMetadata(), new CountLetterD());
		reindex(record2);

		transaction = new Transaction();
		Record record3 = transaction.add(new TestRecord(zeSchema, "tomcat").set(zeSchema.stringMetadata(), "Tomcat"));
		recordServices.execute(transaction);

		//  3/3 reindexed record on script E, 2/3 on others
		recordMigrationsManager.checkScriptsToFinish();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterE())).isTrue();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterD())).isFalse();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new SetStringMetadata())).isFalse();
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(1);
		assertThat(countLetterDFinishCallCounter.get()).isEqualTo(0);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(0);

		reindex(record1);
		//  3/3 reindexed record on all scripts, but checkScriptsToFinish has not been called yet
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterE())).isTrue();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterD())).isFalse();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new SetStringMetadata())).isFalse();
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(1);
		assertThat(countLetterDFinishCallCounter.get()).isEqualTo(0);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(0);

		//  2/2 reindexed record on all scripts
		recordMigrationsManager.checkScriptsToFinish();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterE())).isTrue();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterD())).isTrue();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new SetStringMetadata())).isTrue();
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(1);
		assertThat(countLetterDFinishCallCounter.get()).isEqualTo(1);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(1);

		//  Nothing changed
		recordMigrationsManager.checkScriptsToFinish();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterE())).isTrue();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new CountLetterD())).isTrue();
		assertThat(recordMigrationsManager.isFinished(zeCollection, new SetStringMetadata())).isTrue();
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(1);
		assertThat(countLetterDFinishCallCounter.get()).isEqualTo(1);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(1);

	}

	private void reindex(Record record) {
		recordServices = getModelLayerFactory().newRecordServices();
		Transaction transaction = new Transaction();

		transaction.add(record);

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void givenScriptAlreadyRegisteredWhenCreatingANewRecordThenNotExecuted()
			throws Exception {
		givenBackgroundThreadsEnabled();
		registerInZeCollectionAndReindex(new CountLetterE());
		registerInZeCollectionAndReindex(new SetStringMetadata());

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "vodka").set(zeSchema.stringMetadata(), "Vodka Framboise"));
		transaction.add(new TestRecord(zeSchema, "edouard").set(zeSchema.stringMetadata(), "Édouard Lechat"));
		recordServices.execute(transaction);

		waitForBatchProcess();
		assertThatAllRecordsOf(zeSchema.type()).extractingMetadatas("stringMetadata", "numberMetadata",
				MIGRATION_DATA_VERSION.getLocalCode()).containsOnly(
				tuple("Vodka Framboise", null, 2.0),
				tuple("Édouard Lechat", null, 2.0)
		);

		recordMigrationsManager.checkScriptsToFinish();
		assertThat(countLetterEMigrateCallCounter.get()).isEqualTo(0);
		assertThat(countLetterEFinishCallCounter.get()).isEqualTo(1);
		assertThat(SetStringMetadataMigrateCallCounter.get()).isEqualTo(0);
		assertThat(SetStringMetadataFinishCallCounter.get()).isEqualTo(1);

	}

	//TODO Test isFinished

	AtomicInteger countLetterDMigrateCallCounter = new AtomicInteger();
	AtomicInteger countLetterDFinishCallCounter = new AtomicInteger();

	class CountLetterD extends RecordMigrationScript {

		@Override
		public String getSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void migrate(Record record) {
			Metadata stringMetadata = zeSchema.stringMetadata();
			Metadata numberMetadata = zeSchema.numberMetadata();

			String str = AccentApostropheCleaner.cleanAll(record.<String>get(stringMetadata).toLowerCase());
			int count = StringUtils.countMatches(str, "d");
			record.set(numberMetadata, count);
			countLetterDMigrateCallCounter.incrementAndGet();
		}

		@Override
		public void afterLastMigratedRecord() {
			countLetterDFinishCallCounter.incrementAndGet();
		}
	}

	AtomicInteger anotherSchemaScript1MigrateCallCounter = new AtomicInteger();
	AtomicInteger anotherSchemaScript1FinishCallCounter = new AtomicInteger();

	class AnotherSchemaScript1 extends RecordMigrationScript {

		@Override
		public String getSchemaType() {
			return "anotherSchemaType";
		}

		@Override
		public void migrate(Record record) {
			anotherSchemaScript1MigrateCallCounter.incrementAndGet();
		}

		@Override
		public void afterLastMigratedRecord() {
			anotherSchemaScript1FinishCallCounter.incrementAndGet();
		}
	}

	AtomicInteger anotherSchemaScript2MigrateCallCounter = new AtomicInteger();
	AtomicInteger anotherSchemaScript2FinishCallCounter = new AtomicInteger();

	class AnotherSchemaScript2 extends RecordMigrationScript {

		@Override
		public String getSchemaType() {
			return "anotherSchemaType";
		}

		@Override
		public void migrate(Record record) {
			anotherSchemaScript2MigrateCallCounter.incrementAndGet();
		}

		@Override
		public void afterLastMigratedRecord() {
			anotherSchemaScript2FinishCallCounter.incrementAndGet();
		}
	}

	AtomicInteger countLetterEMigrateCallCounter = new AtomicInteger();
	AtomicInteger countLetterEFinishCallCounter = new AtomicInteger();

	class CountLetterE extends RecordMigrationScript {

		@Override
		public String getSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void migrate(Record record) {
			countLetterEMigrateCallCounter.incrementAndGet();
			Metadata stringMetadata = zeSchema.stringMetadata();
			Metadata numberMetadata = zeSchema.numberMetadata();
			String str = AccentApostropheCleaner.cleanAll(record.<String>get(stringMetadata).toLowerCase());
			int count = StringUtils.countMatches(str, "e");
			record.set(numberMetadata, count);
		}

		@Override
		public void afterLastMigratedRecord() {
			countLetterEFinishCallCounter.incrementAndGet();
		}
	}

	AtomicInteger changerFramboiseACannebergeMigrateCallCounter = new AtomicInteger();
	AtomicInteger changerFramboiseACannebergeFinishCallCounter = new AtomicInteger();

	class ChangeFramboiseForCanneberge extends RecordMigrationScript {

		@Override
		public String getSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void migrate(Record record) {
			changerFramboiseACannebergeMigrateCallCounter.incrementAndGet();
			Metadata stringMetadata = zeSchema.stringMetadata();

			String value = record.get(stringMetadata);

			if (value != null) {
				record.set(stringMetadata, value.replace("Framboise", "Canneberge"));
			}
		}

		@Override
		public void afterLastMigratedRecord() {
			changerFramboiseACannebergeFinishCallCounter.incrementAndGet();
		}
	}

	AtomicInteger SetStringMetadataMigrateCallCounter = new AtomicInteger();
	AtomicInteger SetStringMetadataFinishCallCounter = new AtomicInteger();

	class SetStringMetadata extends RecordMigrationScript {

		@Override
		public String getSchemaType() {
			return "zeSchemaType";
		}

		@Override
		public void migrate(Record record) {
			SetStringMetadataMigrateCallCounter.incrementAndGet();
			Metadata stringMetadata = zeSchema.stringMetadata();

			record.set(stringMetadata, "a value defined by a script");
		}

		@Override
		public void afterLastMigratedRecord() {
			SetStringMetadataFinishCallCounter.incrementAndGet();
		}
	}

	void registerInZeCollectionAndReindex(RecordMigrationScript... scripts) {
		Set<String> schemaTypeCodes = recordMigrationsManager
				.registerReturningTypesWithNewScripts(zeCollection, asList(scripts), true);
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		List<MetadataSchemaType> schemaTypes = manager.getSchemaTypes(zeCollection)
				.getSchemaTypesWithCode(new ArrayList<>(schemaTypeCodes));

		getModelLayerFactory().getBatchProcessesManager().reindexInBackground(schemaTypes);
	}

	void registerInAnotherCollectionAndReindex(RecordMigrationScript... scripts) {
		Set<String> schemaTypeCodes = recordMigrationsManager
				.registerReturningTypesWithNewScripts("anotherCollection", asList(scripts), true);
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();
		List<MetadataSchemaType> schemaTypes = manager.getSchemaTypes("anotherCollection")
				.getSchemaTypesWithCode(new ArrayList<>(schemaTypeCodes));

		getModelLayerFactory().getBatchProcessesManager().reindexInBackground(schemaTypes);
	}

	private Record given(Record record)
			throws RecordServicesException {
		recordServices.add(record);
		return record;
	}
}
