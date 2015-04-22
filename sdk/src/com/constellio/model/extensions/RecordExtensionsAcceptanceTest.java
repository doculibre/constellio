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
package com.constellio.model.extensions;

import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordCreationEvent.RecordCreationEventListener;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent.RecordLogicalDeletionEventListener;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent.RecordModificationEventListener;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent.RecordPhysicalDeletionEventListener;
import com.constellio.model.extensions.events.records.RecordRestorationEvent;
import com.constellio.model.extensions.events.records.RecordRestorationEvent.RecordRestorationEventListener;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordExtensionsAcceptanceTest extends ConstellioTest {

	String anotherCollection = "anotherCollection";
	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	RecordServices recordServices;

	@Mock RecordCreationEventListener recordCreationEventListener1;
	@Mock RecordCreationEventListener recordCreationEventListener2;
	@Mock RecordCreationEventListener otherCollectionRecordCreationEventListener;

	@Mock RecordModificationEventListener recordModificationEventListener1;
	@Mock RecordModificationEventListener recordModificationEventListener2;
	@Mock RecordModificationEventListener otherCollectionRecordModificationEventListener;

	@Mock RecordLogicalDeletionEventListener recordLogicalDeletionEventListener1;
	@Mock RecordLogicalDeletionEventListener recordLogicalDeletionEventListener2;
	@Mock RecordLogicalDeletionEventListener otherCollectionRecordLogicalDeletionEventListener;

	@Mock RecordPhysicalDeletionEventListener recordPhysicalDeletionEventListener1;
	@Mock RecordPhysicalDeletionEventListener recordPhysicalDeletionEventListener2;
	@Mock RecordPhysicalDeletionEventListener otherCollectionRecordPhysicalDeletionEventListener;

	@Mock RecordRestorationEventListener recordRestorationEventListener1;
	@Mock RecordRestorationEventListener recordRestorationEventListener2;
	@Mock RecordRestorationEventListener otherCollectionRecordRestorationEventListener2;

	Record existingZeSchemaRecord, existingAnotherSchemaRecord, logicallyDeletedZeSchemaRecord, logicallyDeletedZeSchemaChildRecord, existingZeSchemaChildRecord;

	@Before
	public void setUp()
			throws Exception {
		defineSchemasManager().using(schemas
				.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false)
				.withAParentReferenceFromZeSchemaToZeSchema());

		Taxonomy taxonomy = new Taxonomy("ze taxo", "ze taxo", zeCollection, zeSchema.typeCode());
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
		ModelLayerCollectionEventsListeners zeCollectionListeners = extensions.getCollectionListeners(zeCollection);
		ModelLayerCollectionEventsListeners anotherCollectionListeners = extensions.getCollectionListeners(anotherCollection);

		zeCollectionListeners.recordsCreationListeners.add(1, recordCreationEventListener1);
		zeCollectionListeners.recordsCreationListeners.add(2, recordCreationEventListener2);
		anotherCollectionListeners.recordsCreationListeners.add(1, otherCollectionRecordCreationEventListener);

		zeCollectionListeners.recordsModificationListeners.add(1, recordModificationEventListener1);
		zeCollectionListeners.recordsModificationListeners.add(2, recordModificationEventListener2);
		anotherCollectionListeners.recordsModificationListeners.add(1, otherCollectionRecordModificationEventListener);

		zeCollectionListeners.recordsLogicallyDeletionListeners.add(1, recordLogicalDeletionEventListener1);
		zeCollectionListeners.recordsLogicallyDeletionListeners.add(2, recordLogicalDeletionEventListener2);
		anotherCollectionListeners.recordsLogicallyDeletionListeners.add(1, otherCollectionRecordLogicalDeletionEventListener);

		zeCollectionListeners.recordsPhysicallyDeletionListeners.add(1, recordPhysicalDeletionEventListener1);
		zeCollectionListeners.recordsPhysicallyDeletionListeners.add(2, recordPhysicalDeletionEventListener2);
		anotherCollectionListeners.recordsPhysicallyDeletionListeners.add(1, otherCollectionRecordPhysicalDeletionEventListener);

		zeCollectionListeners.recordsRestorationListeners.add(1, recordRestorationEventListener1);
		zeCollectionListeners.recordsRestorationListeners.add(2, recordRestorationEventListener2);
		anotherCollectionListeners.recordsRestorationListeners.add(1, otherCollectionRecordRestorationEventListener2);
	}

	@After
	public void verifyThatOtherCollectionListenersAreNeverCalled() {

		verifyNoMoreInteractions(recordCreationEventListener1);
		verifyNoMoreInteractions(recordCreationEventListener2);

		verifyNoMoreInteractions(recordModificationEventListener1);
		verifyNoMoreInteractions(recordModificationEventListener2);

		verifyNoMoreInteractions(recordLogicalDeletionEventListener1);
		verifyNoMoreInteractions(recordLogicalDeletionEventListener2);

		verifyNoMoreInteractions(recordPhysicalDeletionEventListener1);
		verifyNoMoreInteractions(recordPhysicalDeletionEventListener2);

		verifyNoMoreInteractions(recordRestorationEventListener1);
		verifyNoMoreInteractions(recordRestorationEventListener2);

		verify(otherCollectionRecordCreationEventListener, never()).notify(any(RecordCreationEvent.class));
		verify(otherCollectionRecordModificationEventListener, never()).notify(any(RecordModificationEvent.class));
		verify(otherCollectionRecordLogicalDeletionEventListener, never()).notify(any(RecordLogicalDeletionEvent.class));
		verify(otherCollectionRecordPhysicalDeletionEventListener, never()).notify(any(RecordPhysicalDeletionEvent.class));
		verify(otherCollectionRecordRestorationEventListener2, never()).notify(any(RecordRestorationEvent.class));
	}

	@Test
	public void whenCreatingARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordCreationEvent> argumentCaptor = ArgumentCaptor.forClass(RecordCreationEvent.class);
		Record record1 = new TestRecord(zeSchema, "newZeSchemaRecord").set(TITLE, "My first record");
		Record record2 = new TestRecord(anotherSchema, "newOtherSchemaRecord").set(TITLE, "My second record");
		recordServices.execute(new Transaction(record1, record2));

		InOrder inOrder = inOrder(recordCreationEventListener1, recordCreationEventListener2);
		inOrder.verify(recordCreationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordCreationEventListener2).notify(argumentCaptor.capture());
		inOrder.verify(recordCreationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordCreationEventListener2).notify(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId()).isEqualTo("newOtherSchemaRecord");
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId()).isEqualTo("newOtherSchemaRecord");
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId()).isEqualTo("newZeSchemaRecord");
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId()).isEqualTo("newZeSchemaRecord");
	}

	@Test
	public void whenModifyingARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordModificationEvent> argumentCaptor = ArgumentCaptor.forClass(RecordModificationEvent.class);
		existingZeSchemaRecord.set(Schemas.TITLE, "new title");
		existingAnotherSchemaRecord.set(Schemas.TITLE, "an other new title");
		recordServices.execute(new Transaction(existingZeSchemaRecord, existingAnotherSchemaRecord));

		InOrder inOrder = inOrder(recordModificationEventListener1, recordModificationEventListener2);
		inOrder.verify(recordModificationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordModificationEventListener2).notify(argumentCaptor.capture());
		inOrder.verify(recordModificationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordModificationEventListener2).notify(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(0).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("anotherSchemaType_default_title", "anotherSchemaType_default_modifiedOn");
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("anotherSchemaType_default_title", "anotherSchemaType_default_modifiedOn");
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("zeSchemaType_default_title", "zeSchemaType_default_modifiedOn");
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(3).getModifiedMetadatas().toMetadatasCodesList())
				.containsOnly("zeSchemaType_default_title", "zeSchemaType_default_modifiedOn");
	}

	@Test
	public void whenModifyingARecordWithImpactsThenListenerCalledForImpactedRecords()
			throws Exception {
		ArgumentCaptor<RecordModificationEvent> argumentCaptor = ArgumentCaptor.forClass(RecordModificationEvent.class);
		recordServices.update(
				existingAnotherSchemaRecord.set(Schemas.TITLE, "new title").set(anotherSchema.stringMetadata(), "New value!"));

		InOrder inOrder = inOrder(recordModificationEventListener1, recordModificationEventListener2);
		inOrder.verify(recordModificationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordModificationEventListener2).notify(argumentCaptor.capture());
		inOrder.verify(recordModificationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordModificationEventListener2).notify(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId()).isEqualTo(existingAnotherSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
	}

	@Test
	public void whenLogicallyDeletingARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordLogicalDeletionEvent> argumentCaptor = ArgumentCaptor.forClass(RecordLogicalDeletionEvent.class);
		recordServices.logicallyDelete(existingZeSchemaRecord, User.GOD);

		InOrder inOrder = inOrder(recordLogicalDeletionEventListener1, recordLogicalDeletionEventListener2);
		inOrder.verify(recordLogicalDeletionEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordLogicalDeletionEventListener2).notify(argumentCaptor.capture());
		inOrder.verify(recordLogicalDeletionEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordLogicalDeletionEventListener2).notify(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId()).isEqualTo(existingZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId()).isEqualTo(existingZeSchemaChildRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId()).isEqualTo(existingZeSchemaChildRecord.getId());
	}

	@Test
	public void whenRestoringARecordThenListenersCalled()
			throws Exception {
		ArgumentCaptor<RecordRestorationEvent> argumentCaptor = ArgumentCaptor.forClass(RecordRestorationEvent.class);
		recordServices.restore(logicallyDeletedZeSchemaRecord, User.GOD);

		InOrder inOrder = inOrder(recordRestorationEventListener1, recordRestorationEventListener2);
		inOrder.verify(recordRestorationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordRestorationEventListener2).notify(argumentCaptor.capture());
		inOrder.verify(recordRestorationEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordRestorationEventListener2).notify(argumentCaptor.capture());

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

		InOrder inOrder = inOrder(recordPhysicalDeletionEventListener1, recordPhysicalDeletionEventListener2);
		inOrder.verify(recordPhysicalDeletionEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordPhysicalDeletionEventListener2).notify(argumentCaptor.capture());
		inOrder.verify(recordPhysicalDeletionEventListener1).notify(argumentCaptor.capture());
		inOrder.verify(recordPhysicalDeletionEventListener2).notify(argumentCaptor.capture());

		assertThat(argumentCaptor.getAllValues().get(0).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(1).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(2).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaChildRecord.getId());
		assertThat(argumentCaptor.getAllValues().get(3).getRecord().getId())
				.isEqualTo(logicallyDeletedZeSchemaChildRecord.getId());
	}

}
