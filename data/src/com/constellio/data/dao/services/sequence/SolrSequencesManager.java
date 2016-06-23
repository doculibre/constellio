package com.constellio.data.dao.services.sequence;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;

public class SolrSequencesManager implements SequencesManager {

	int simultaneousUsages = 128;

	SolrClient client;
	RecordDao recordDao;
	Map<String, List<String>> uuidsToRemoveBySequenceId = new HashMap<>();

	public SolrSequencesManager(RecordDao recordDao) {
		this.recordDao = recordDao;
		this.client = recordDao.getBigVaultServer().getNestedSolrServer();
		new Thread() {

			@Override
			public void run() {
				while (true) {

					synchronized (SolrSequencesManager.class) {
						SolrQuery q = new SolrQuery();
						q.setRequestHandler("/get");
						q.set("id", "seq_" + "zeSequence");

						//		ModifiableSolrParams params = new ModifiableSolrParams();
						//		params.set("q", "id:seq_" + sequenceId);

						try {

							SolrDocument document = null;
							int i = 0;
							boolean found = false;
							//while (!found) {
							QueryResponse response = client.query(q);
							document = (SolrDocument) response.getResponse().get("doc");

							if (document != null) {
								List<String> readyToRemove = (List) document.getFieldValues("uuids_to_remove_ss");

								if (readyToRemove != null) {
									List<String> uuids = (List) document.getFieldValues("uuids_ss");

									List<String> toRemove = new ArrayList<>();
									for (String currentUUID : uuids) {
										if (readyToRemove.contains(currentUUID)) {
											toRemove.add(currentUUID);
										} else {
											break;
										}
									}

									//System.out.println("Removing " + sizeToRemove + " items");
									SolrInputDocument solrInputDocument = new SolrInputDocument();
									solrInputDocument.addField("id", "seq_" + "zeSequence");
									solrInputDocument.addField("_version_", "1");
									solrInputDocument.addField("type_s", "sequence");
									solrInputDocument.addField("counter_d", singletonMap("inc", toRemove.size()));

									solrInputDocument.addField("uuids_ss", singletonMap("remove", toRemove));

									solrInputDocument.addField("uuids_to_remove_ss", singletonMap("remove", toRemove));
									client.add(solrInputDocument);

									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										throw new RuntimeException(e);

									}
								}
							}
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}

					}
				}
			}
		}.start();
	}

	@Override
	public void set(String sequenceId, long value) {

	}

	@Override
	public long getLastSequenceValue(String sequenceId) {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("id", "seq_" + sequenceId);

		try {
			QueryResponse response = client.query(params);
			if (response.getResults().getNumFound() == 1) {
				return Double.valueOf((String) response.getResults().get(0).getFieldValue("counter_d")).longValue();
			} else {
				return 0;
			}

		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long next(String sequenceId) {

		String uuid = UUIDV1Generator.newRandomId();

		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", "seq_" + sequenceId);
		solrInputDocument.addField("_version_", "1");
		solrInputDocument.addField("type_s", "sequence");

		Map<String, Object> uuidsOperations = new HashMap<>();
		uuidsOperations.put("add", uuid);
		solrInputDocument.addField("uuids_ss", uuidsOperations);

		try {
			UpdateRequest request = new UpdateRequest();
			request.add(solrInputDocument);
			request.setCommitWithin(-1);
			request.process(client);
		} catch (Exception e) {

			solrInputDocument = new SolrInputDocument();
			solrInputDocument.addField("id", "seq_" + sequenceId);
			solrInputDocument.addField("_version_", "-1");
			solrInputDocument.addField("type_s", "sequence");
			solrInputDocument.addField("uuids_ss", asList(uuid));
			solrInputDocument.addField("counter_d", 0.0);
			try {
				UpdateRequest request = new UpdateRequest();
				request.add(solrInputDocument);
				request.setCommitWithin(-1);
				request.process(client);
			} catch (Exception e2) {
				return next(sequenceId);
			}
		}

		//		try {
		//			client.commit(true, true, true);
		//		} catch (SolrServerException e) {
		//			throw new RuntimeException(e);
		//		} catch (IOException e) {
		//			throw new RuntimeException(e);
		//		}

		SolrQuery q = new SolrQuery();
		q.setRequestHandler("/get");
		q.set("id", "seq_" + sequenceId);

		//		ModifiableSolrParams params = new ModifiableSolrParams();
		//		params.set("q", "id:seq_" + sequenceId);

		try {

			SolrDocument document = null;
			List<String> uuids = null;
			int i = 0;
			boolean found = false;
			//while (!found) {
			QueryResponse response = client.query(q);
			document = (SolrDocument) response.getResponse().get("doc");
			uuids = (List) document.getFieldValues("uuids_ss");
			ListIterator<String> uuidsIterator = uuids.listIterator();

			i = 0;
			while (!found && uuidsIterator.hasNext()) {
				i++;
				String aUUID = uuidsIterator.next();
				if (uuid.equals(aUUID)) {
					found = true;
				}
			}

			if (!found) {
				throw new RuntimeException("Not found!");
			}

			//}
			long counter = ((Double) document.getFieldValue("counter_d")).longValue();
			long value = counter + i;
			solrInputDocument = new SolrInputDocument();
			solrInputDocument.addField("id", "seq_" + sequenceId);
			solrInputDocument.addField("_version_", "1");
			solrInputDocument.addField("type_s", "sequence");
			solrInputDocument.addField("uuids_to_remove_ss", singletonMap("add", uuid));
			try {
				client.add(solrInputDocument);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return value;

		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}

	}

}
