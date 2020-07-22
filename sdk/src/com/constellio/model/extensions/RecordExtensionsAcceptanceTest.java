package com.constellio.model.extensions;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyZeroInteractions;

public class RecordExtensionsAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	RecordServices recordServices;

	@Mock RecordExtension recordExtension1;
	@Mock RecordExtension recordExtension2;
	@Mock RecordExtension otherCollectionRecordExtension;

	Record existingZeSchemaRecord, existingAnotherSchemaRecord, logicallyDeletedZeSchemaRecord, logicallyDeletedZeSchemaChildRecord, existingZeSchemaChildRecord;

	@Before
	public void setUp()
			throws Exception {
		defineSchemasManager().using(schemas
				.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false)
				.withAParentReferenceFromZeSchemaToZeSchema());
		givenTimeIs(shishOClock);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "ze taxo");

		Taxonomy taxonomy = new Taxonomy("ze taxo", labelTitle1, emptyMap(), zeCollection, zeSchema.typeCode());
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy, getModelLayerFactory().getMetadataSchemasManager());

		recordServices = getModelLayerFactory().newRecordServices();

		existingAnotherSchemaRecord = new TestRecord(anotherSchema, "existingAnotherSchemaRecord").set(TITLE, "My second record");
		existingZeSchemaRecord = new TestRecord(zeSchema, "existingZeSchemaRecord").set(TITLE, "My first record")
				.set(zeSchema.firstReferenceToAnotherSchema(), existingAnotherSchemaRecord);
		existingZeSchemaChildRecord = new TestRecord(zeSchema, "existingZeSchemaChildRecord").set(TITLE, "Child record")
				.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), existingZeSchemaRecord);

		logicallyDeletedZeSchemaRecord = new TestRecord(zeSchema, "logicallyDeletedZeSchemaRecord")
				.set(TITLE, "Parent record");
		logicallyDeletedZeSchemaChildRecord = new TestRecord(zeSchema, "logicallyDeletedZeSchemaChildRecord")
				.set(TITLE, "Child record").set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), logicallyDeletedZeSchemaRecord);

		recordServices.execute(new Transaction(existingZeSchemaRecord, existingAnotherSchemaRecord,
				logicallyDeletedZeSchemaRecord, logicallyDeletedZeSchemaChildRecord, existingZeSchemaChildRecord));

		recordServices.logicallyDelete(logicallyDeletedZeSchemaRecord, User.GOD);

		ModelLayerExtensions extensions = getModelLayerFactory().getExtensions();
		ModelLayerCollectionExtensions zeCollectionListeners = extensions.forCollection(zeCollection);
		ModelLayerCollectionExtensions anotherCollectionListeners = extensions.forCollection(anotherCollection);

		zeCollectionListeners.recordExtensions.add(1, recordExtension1);
		zeCollectionListeners.recordExtensions.add(2, recordExtension2);
		anotherCollectionListeners.recordExtensions.add(1, otherCollectionRecordExtension);
		givenTimeIs(shishOClock.plusDays(1));

	}

	@After
	public void verifyThatOtherCollectionListenersAreNeverCalled() {

		verifyZeroInteractions(otherCollectionRecordExtension);

	}

	@Test
	public void whenCreatingARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent> recordInCreationArgs = ArgumentCaptor
				.forClass(RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent.class);
		ArgumentCaptor<RecordCreationEvent> recordCreatedArgs = ArgumentCaptor.forClass(RecordCreationEvent.class);
		Record record1 = new TestRecord(zeSchema, "newZeSchemaRecord").set(TITLE, "My first record");
		Record record2 = new TestRecord(anotherSchema, "newOtherSchemaRecord").set(TITLE, "My second record");
		recordServices.execute(new Transaction(record1, record2));

		InOrder inOrder = inOrder(recordExtension1, recordExtension2);

		inOrder.verify(recordExtension1).recordInCreationBeforeValidationAndAutomaticValuesCalculation(
				recordInCreationArgs.capture());
		inOrder.verify(recordExtension2).recordInCreationBeforeValidationAndAutomaticValuesCalculation(
				recordInCreationArgs.capture());
		inOrder.verify(recordExtension1).recordInCreationBeforeValidationAndAutomaticValuesCalculation(
				recordInCreationArgs.capture());
		inOrder.verify(recordExtension2).recordInCreationBeforeValidationAndAutomaticValuesCalculation(
				recordInCreationArgs.capture());

		inOrder.verify(recordExtension1).recordCreated(recordCreatedArgs.capture());
		inOrder.verify(recordExtension2).recordCreated(recordCreatedArgs.capture());
		inOrder.verify(recordExtension1).recordCreated(recordCreatedArgs.capture());
		inOrder.verify(recordExtension2).recordCreated(recordCreatedArgs.capture());
		verifyZeroInteractions(otherCollectionRecordExtension);

		assertThat(recordInCreationArgs.getAllValues().get(0).getRecord().getId()).isEqualTo("newOtherSchemaRecord");
		assertThat(recordInCreationArgs.getAllValues().get(1).getRecord().getId()).isEqualTo("newOtherSchemaRecord");
		assertThat(recordInCreationArgs.getAllValues().get(2).getRecord().getId()).isEqualTo("newZeSchemaRecord");
		assertThat(recordInCreationArgs.getAllValues().get(3).getRecord().getId()).isEqualTo("newZeSchemaRecord");

		assertThat(recordCreatedArgs.getAllValues().get(0).getRecord().getId()).isEqualTo("newOtherSchemaRecord");
		assertThat(recordCreatedArgs.getAllValues().get(1).getRecord().getId()).isEqualTo("newOtherSchemaRecord");
		assertThat(recordCreatedArgs.getAllValues().get(2).getRecord().getId()).isEqualTo("newZeSchemaRecord");
		assertThat(recordCreatedArgs.getAllValues().get(3).getRecord().getId()).isEqualTo("newZeSchemaRecord");
	}

	@Test
	public void whenModifyingARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent> recordInModificationArgs = ArgumentCaptor
				.forClass(RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent.class);
		ArgumentCaptor<RecordModificationEvent> recordModifiedArgs = ArgumentCaptor.forClass(RecordModificationEvent.class);
		existingZeSchemaRecord.set(Schemas.TITLE, "new title");
		existingAnotherSchemaRecord.set(Schemas.TITLE, "an other new title");
		recordServices.execute(new Transaction(existingZeSchemaRecord, existingAnotherSchemaRecord));

		InOrder inOrder = inOrder(recordExtension1, recordExtension2);

		inOrder.verify(recordExtension1).recordInModificationBeforeValidationAndAutomaticValuesCalculation(
				recordInModificationArgs.capture());
		inOrder.verify(recordExtension2).recordInModificationBeforeValidationAndAutomaticValuesCalculation(
				recordInModificationArgs.capture());
		inOrder.verify(recordExtension1).recordInModificationBeforeValidationAndAutomaticValuesCalculation(
				recordInModificationArgs.capture());
		inOrder.verify(recordExtension2).recordInModificationBeforeValidationAndAutomaticValuesCalculation(
				recordInModificationArgs.capture());

		inOrder.verify(recordExtension1).recordModified(recordModifiedArgs.capture());
		inOrder.verify(recordExtension2).recordModified(recordModifiedArgs.capture());
		inOrder.verify(recordExtension1).recordModified(recordModifiedArgs.capture());
		inOrder.verify(recordExtension2).recordModified(recordModifiedArgs.capture());
		verifyZeroInteractions(otherCollectionRecordExtension);

		assertThat(recordInModificationArgs.getAllValues().get(0).getRecord().getId())
				.isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(recordInModificationArgs.getAllValues().get(0).getModifiedMetadatas().stream().map(Metadata::getCode).collect(Collectors.toList()))
				.containsOnly("anotherSchemaType_default_title");
		assertThat(recordInModificationArgs.getAllValues().get(1).getRecord().getId())
				.isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(recordInModificationArgs.getAllValues().get(1).getModifiedMetadatas().stream().map(Metadata::getCode).collect(Collectors.toList()))
				.containsOnly("anotherSchemaType_default_title");
		assertThat(recordInModificationArgs.getAllValues().get(2).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(recordInModificationArgs.getAllValues().get(2).getModifiedMetadatas().stream().map(Metadata::getCode).collect(Collectors.toList()))
				.containsOnly("zeSchemaType_default_title");
		assertThat(recordInModificationArgs.getAllValues().get(3).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(recordInModificationArgs.getAllValues().get(3).getModifiedMetadatas().stream().map(Metadata::getCode).collect(Collectors.toList()))
				.containsOnly("zeSchemaType_default_title");

		assertThat(recordModifiedArgs.getAllValues().get(0).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(recordModifiedArgs.getAllValues().get(0).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("anotherSchemaType_default_title", "anotherSchemaType_default_autocomplete",
						"anotherSchemaType_default_modifiedOn");
		assertThat(recordModifiedArgs.getAllValues().get(1).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(recordModifiedArgs.getAllValues().get(1).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("anotherSchemaType_default_title", "anotherSchemaType_default_autocomplete",
						"anotherSchemaType_default_modifiedOn");
		assertThat(recordModifiedArgs.getAllValues().get(2).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(recordModifiedArgs.getAllValues().get(2).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("zeSchemaType_default_title", "zeSchemaType_default_autocomplete",
						"zeSchemaType_default_modifiedOn");
		assertThat(recordModifiedArgs.getAllValues().get(3).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(recordModifiedArgs.getAllValues().get(3).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("zeSchemaType_default_title", "zeSchemaType_default_autocomplete",
						"zeSchemaType_default_modifiedOn");
	}

	@Test
	public void whenModifyingARecordWithImpactsThenListenerCalledForImpactedRecords()
			throws Exception {
		ArgumentCaptor<RecordModificationEvent> argumentCaptor = ArgumentCaptor.forClass(RecordModificationEvent.class);
		recordServices.update(
				existingAnotherSchemaRecord.set(Schemas.TITLE, "new title").set(anotherSchema.stringMetadata(), "New value!"));

		InOrder inOrder = inOrder(recordExtension1, recordExtension2);
		inOrder.verify(recordExtension1).recordModified(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordModified(argumentCaptor.capture());
		inOrder.verify(recordExtension1).recordModified(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordModified(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
	}

	@Test
	public void whenLogicallyDeletingARecordThenListenersCalled()
			throws Exception {

		//		ReindexingServices reindexingServices = new ReindexingServices(getModelLayerFactory());
		//		reindexingServices.reindexCollection(zeCollection, ReindexationMode.RECALCULATE_AND_REWRITE);

		ArgumentCaptor<RecordLogicalDeletionEvent> argumentCaptor = ArgumentCaptor.forClass(RecordLogicalDeletionEvent.class);
		recordServices.logicallyDelete(existingZeSchemaRecord, User.GOD);

		InOrder inOrder = inOrder(recordExtension1, recordExtension2);
		inOrder.verify(recordExtension1).recordLogicallyDeleted(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordLogicallyDeleted(argumentCaptor.capture());
		inOrder.verify(recordExtension1).recordLogicallyDeleted(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordLogicallyDeleted(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues()).extracting("record.id").containsOnly(
				existingZeSchemaRecord.getId(),
				existingZeSchemaRecord.getId(),
				existingZeSchemaChildRecord.getId(),
				existingZeSchemaChildRecord.getId()
		);

	}

	@Test
	public void whenRestoringARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordRestorationEvent> argumentCaptor = ArgumentCaptor.forClass(RecordRestorationEvent.class);
		recordServices.restore(logicallyDeletedZeSchemaRecord, User.GOD);

		InOrder inOrder = inOrder(recordExtension1, recordExtension2);
		inOrder.verify(recordExtension1).recordRestored(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordRestored(argumentCaptor.capture());
		inOrder.verify(recordExtension1).recordRestored(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordRestored(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaChildRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaChildRecord.getId());
	}

	@Test
	public void whenPhysicallyARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordPhysicalDeletionEvent> argumentCaptor = ArgumentCaptor.forClass(RecordPhysicalDeletionEvent.class);
		recordServices.physicallyDelete(logicallyDeletedZeSchemaRecord, User.GOD);

		InOrder inOrder = inOrder(recordExtension1, recordExtension2);
		inOrder.verify(recordExtension1).recordPhysicallyDeleted(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordPhysicallyDeleted(argumentCaptor.capture());
		inOrder.verify(recordExtension1).recordPhysicallyDeleted(argumentCaptor.capture());
		inOrder.verify(recordExtension2).recordPhysicallyDeleted(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaChildRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaChildRecord.getId());
	}

}
