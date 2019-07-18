package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class RecordServicesAgregatedMetadatasAcceptTestRecords {

	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;
	ThirdSchemaMetadatas thirdSchema;

	RecordServices recordServices;
	ModelLayerFactory modelLayerFactory;

	private void initialize(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.schemas = schemas;
		this.zeSchema = schemas.new ZeSchemaMetadatas();
		this.anotherSchema = schemas.new AnotherSchemaMetadatas();
		this.thirdSchema = schemas.new ThirdSchemaMetadatas();
	}

	public void setupInOneTransaction(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory)
			throws Exception {
		initialize(schemas, modelLayerFactory);

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord1").set("ref", "anotherSchemaRecord1").set("number", 1));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord2").set("ref", "anotherSchemaRecord1").set("number", 2));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord3").set("ref", "anotherSchemaRecord2").set("number", 3));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord4").set("ref", "anotherSchemaRecord2").set("number", 4));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1").set("ref", "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2").set("ref", "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord2"));

		recordServices.execute(transaction);
		modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
	}

	public void setupWith(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory)
			throws Exception {
		initialize(schemas, modelLayerFactory);

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord1").set("ref", "anotherSchemaRecord1").set("number", 1));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord2").set("ref", "anotherSchemaRecord1").set("number", 2));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord3").set("ref", "anotherSchemaRecord2").set("number", 3));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord4").set("ref", "anotherSchemaRecord2").set("number", 4));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1").set("ref", "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2").set("ref", "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord2"));

		recordServices.execute(transaction);
		modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
	}

	public void setupWithAgregratedReferenceCount(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory)
			throws Exception {
		initialize(schemas, modelLayerFactory);

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord1").set("ref", "anotherSchemaRecord1"));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord2").set("ref", "anotherSchemaRecord1"));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord3").set("ref", "anotherSchemaRecord2"));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord4").set("ref", "anotherSchemaRecord2"));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1"));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2"));

		recordServices.execute(transaction);
		modelLayerFactory.getBatchProcessesManager().waitUntilAllFinished();
	}

	public void setupEmpty(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory) {
		initialize(schemas, modelLayerFactory);
	}

	public void setupWithNothingComputed(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory)
			throws Exception {
		setupInOneTransaction(schemas, modelLayerFactory);

		SolrClient client = modelLayerFactory.getDataLayerFactory().getRecordsVaultServer().getNestedSolrServer();
		List<SolrInputDocument> inputDocuments = new ArrayList<>();
		for (String id : asList("zeSchemaRecord1", "zeSchemaRecord2", "zeSchemaRecord3", "zeSchemaRecord4")) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.addField("id", id);
			solrInputDocument.addField("pct_d", setNull());
			inputDocuments.add(solrInputDocument);
		}

		for (String id : asList("anotherSchemaRecord1", "anotherSchemaRecord2")) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.addField("id", id);
			solrInputDocument.addField("sum_d", setNull());
			solrInputDocument.addField("sumX10_d", setNull());
			solrInputDocument.addField("copiedThirdSchemaTypeSum_d", setNull());
			inputDocuments.add(solrInputDocument);
		}

		for (String id : asList("aThirdSchemaRecord1", "aThirdSchemaRecord2")) {
			SolrInputDocument solrInputDocument = new SolrInputDocument();
			solrInputDocument.addField("id", id);
			solrInputDocument.addField("sum_d", setNull());
			solrInputDocument.addField("sumX10_d", setNull());
			inputDocuments.add(solrInputDocument);
		}

		client.add(inputDocuments);
		client.commit();

		modelLayerFactory.getRecordsCaches().reloadAllSchemaTypes(zeSchema.collection());
	}

	private Object setNull() {
		Map<String, Object> map = new HashMap<>();
		map.put("set", null);
		return map;
	}

	public void setupInMultipleTransaction(TestsSchemasSetup schemas, ModelLayerFactory modelLayerFactory)
			throws Exception {
		initialize(schemas, modelLayerFactory);

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(thirdSchema, "aThirdSchemaRecord2"));
		recordServices.execute(transaction);

		transaction = new Transaction();
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord1").set("ref", "aThirdSchemaRecord1"));
		transaction.add(new TestRecord(anotherSchema, "anotherSchemaRecord2").set("ref", "aThirdSchemaRecord1"));
		recordServices.execute(transaction);

		transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord1").set("ref", "anotherSchemaRecord1").set("number", 1));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord2").set("ref", "anotherSchemaRecord1").set("number", 2));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord3").set("ref", "anotherSchemaRecord2").set("number", 3));
		transaction.add(new TestRecord(zeSchema, "zeSchemaRecord4").set("ref", "anotherSchemaRecord2").set("number", 4));
		recordServices.execute(transaction);

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
