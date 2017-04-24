package com.constellio.sdk.dev.tools;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrMain {

	static HttpSolrClient client;

	public static void main(String argv[])
			throws SolrServerException, IOException {

		client = new HttpSolrClient("http://localhost:8983/solr/records");
		client.commit();
		long start = new Date().getTime();

		client.deleteById("seq");
		client.commit();

		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", "seq");
		doc.addField("items_ss", atomicAdd("item1"));
		client.add(doc);
		client.commit();
		printItems();

		doc = new SolrInputDocument();
		doc.addField("id", "seq");
		doc.addField("items_ss", atomicAdd("item2"));
		client.add(doc);
		client.commit();
		printItems();

		doc = new SolrInputDocument();
		doc.addField("id", "seq");
		doc.addField("items_ss", atomicAdd("item3"));
		client.add(doc);
		client.commit();
		printItems();

		doc = new SolrInputDocument();
		doc.addField("id", "seq");
		doc.addField("items_ss", atomicAdd("item1"));
		client.add(doc);
		client.commit();
		printItems();

		doc = new SolrInputDocument();
		doc.addField("id", "seq");
		doc.addField("items_ss", atomicAdd("item4"));
		client.add(doc);
		client.commit();
		printItems();

		doc = new SolrInputDocument();
		doc.addField("id", "seq");
		doc.addField("items_ss", atomicReplace("item1", "item5"));
		client.add(doc);
		client.commit();
		printItems();

	}

	private static void printItems() {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:seq");
		try {
			QueryResponse response = client.query(params);
			System.out.println(response.getResults().get(0).getFieldValues("items_ss"));
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<String, Object> atomicAdd(String value) {
		return Collections.singletonMap("add", (Object) value);
	}

	private static Map<String, Object> atomicRemove(String value) {
		return Collections.singletonMap("remove", (Object) value);
	}

	private static Map<String, Object> atomicReplace(String value1, String value2) {
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("remove", value1);
		objectMap.put("add", value2);
		return objectMap;
	}

	private static Map<String, Object> newSetMap(String value) {
		Map<String, Object> map = new HashMap<>();
		map.put("set", value);
		return map;
	}

	private static String toId(int i) {
		String idWithTooMuchZeros = "0000000000" + i;

		return idWithTooMuchZeros.substring(idWithTooMuchZeros.length() - 11);
	}

}
