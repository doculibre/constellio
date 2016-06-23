package com.constellio.data.dao.services.sequence;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.constellio.data.utils.ImpossibleRuntimeException;

public class SolrSequencesManager implements SequencesManager {

	SolrClient client;
	RecordDao recordDao;

	public SolrSequencesManager(RecordDao recordDao) {
		this.recordDao = recordDao;
		this.client = recordDao.getBigVaultServer().getNestedSolrServer();
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

	private SolrDocument getSequenceRealtimeDocument(String sequenceId) {
		SolrQuery q = new SolrQuery();
		q.setRequestHandler("/get");
		q.set("id", "seq_" + sequenceId);
		QueryResponse response = null;
		try {
			response = client.query(q);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		return (SolrDocument) response.getResponse().get("doc");
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

		SolrDocument document = getSequenceRealtimeDocument("zeSequence");
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

				uuidsOperations.put("remove", toRemove);
				solrInputDocument.addField("uuids_to_remove_ss", singletonMap("remove", toRemove));
			}
		}

		solrInputDocument.addField("counter_d", singletonMap("inc", 1.0));
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
			solrInputDocument.addField("counter_d", 1.0);
			try {
				UpdateRequest request = new UpdateRequest();
				request.add(solrInputDocument);
				request.setCommitWithin(-1);
				request.process(client);
			} catch (Exception e2) {
				return next(sequenceId);
			}
		}

		try {

			document = getSequenceRealtimeDocument(sequenceId);
			List<String> uuids = (List) document.getFieldValues("uuids_ss");
			int index = -1;
			for (int i = uuids.size() - 1; i >= 0; i--) {
				String aUUID = uuids.get(i);
				if (uuid.equals(aUUID)) {
					index = i;
					break;
				}

			}
			if (index == -1) {
				throw new ImpossibleRuntimeException("UUID not found.");
			}
			int indexFromEndOfList = uuids.size() - index - 1;

			long counter = ((Double) document.getFieldValue("counter_d")).longValue();
			long value = counter - indexFromEndOfList;
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
