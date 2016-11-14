package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;

public class RecordServicesAgregatedMetadatasAcceptTestRecords {

	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;
	ThirdSchemaMetadatas thirdSchema;

	RecordServices recordServices;
	ModelLayerFactory modelLayerFactory;

	public void setup(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.schemas = schemas;
		this.zeSchema = schemas.new ZeSchemaMetadatas();
		this.anotherSchema = schemas.new AnotherSchemaMetadatas();
		this.thirdSchema = schemas.new ThirdSchemaMetadatas();

		Transaction transaction = new Transaction();
		Record zeSchemaRecord1 = transaction.add(new TestRecord(zeSchema, "zeSchemaRecord1"));
		zeSchemaRecord1.set(zeSchema.metadata("ref"), "anotherSchemaRecord1");
		zeSchemaRecord1.set(zeSchema.metadata("number"), 1);

		Record zeSchemaRecord2 = transaction.add(new TestRecord(zeSchema, "zeSchemaRecord2"));
		zeSchemaRecord2.set(zeSchema.metadata("ref"), "anotherSchemaRecord1");
		zeSchemaRecord2.set(zeSchema.metadata("number"), 2);

		Record zeSchemaRecord3 = transaction.add(new TestRecord(zeSchema, "zeSchemaRecord3"));
		zeSchemaRecord3.set(zeSchema.metadata("ref"), "anotherSchemaRecord2");
		zeSchemaRecord3.set(zeSchema.metadata("number"), 3);

		Record zeSchemaRecord4 = transaction.add(new TestRecord(zeSchema, "zeSchemaRecord4"));
		zeSchemaRecord4.set(zeSchema.metadata("ref"), "anotherSchemaRecord2");
		zeSchemaRecord4.set(zeSchema.metadata("number"), 4);

		Record anotherSchemaRecord1 = transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1"));
		anotherSchemaRecord1.set(anotherSchema.metadata("ref"), "aThirdSchemaRecord1");

		Record anotherSchemaRecord2 = transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2"));
		anotherSchemaRecord2.set(anotherSchema.metadata("ref"), "aThirdSchemaRecord1");

		Record aThirdSchemaRecord1 = transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord1"));
		Record aThirdSchemaRecord2 = transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord2"));

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
	}

	public Record zeSchemaRecord1() {
		return recordServices.getDocumentById("zeSchemaRecord1");
	}

	public Record zeSchemaRecord2() {
		return recordServices.getDocumentById("zeSchemaRecord2");
	}

	public Record zeSchemaRecord3() {
		return recordServices.getDocumentById("zeSchemaRecord3");
	}

	public Record zeSchemaRecord4() {
		return recordServices.getDocumentById("zeSchemaRecord4");
	}

	public Record anotherSchemaRecord1() {
		return recordServices.getDocumentById("anotherSchemaRecord1");
	}

	public Record anotherSchemaRecord2() {
		return recordServices.getDocumentById("anotherSchemaRecord2");
	}

	public Record aThirdSchemaRecord1() {
		return recordServices.getDocumentById("aThirdSchemaRecord1");
	}

	public Record aThirdSchemaRecord2() {
		return recordServices.getDocumentById("aThirdSchemaRecord2");
	}

}
