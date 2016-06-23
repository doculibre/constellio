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
		SolrDocument document = getSequenceDocumentUsingRealtimeGet(sequenceId);
		return document == null ? -1L : ((Double) document.getFieldValue("counter_d")).longValue();
	}

	@Override
	public long next(String sequenceId) {

		String uuid = UUIDV1Generator.newRandomId();

		SolrInputDocument solrInputDocument = prepareSolrInputDocumentForAtomicIncrement(sequenceId, uuid);

		try {
			client.add(solrInputDocument);

		} catch (Exception e) {
			//The document does not exist

			try {
				createSequenceDocument(sequenceId, uuid);
			} catch (Exception e2) {
				//The document has probably been created by an other thread, retrying to increment the counter...
				return next(sequenceId);
			}
		}

		SolrDocument document = getSequenceDocumentUsingRealtimeGet(sequenceId);

		long counter = ((Double) document.getFieldValue("counter_d")).longValue();
		long returnedValue = counter - getUUIDIndexFromEndOfList(uuid, document);

		markUUIDHasRemovable(sequenceId, uuid);

		return returnedValue;

	}

	SolrInputDocument prepareSolrInputDocumentForAtomicIncrement(String sequenceId, String uuid) {
		SolrInputDocument solrInputDocument = newSequenceUpdateInputDocument(sequenceId);
		Map<String, Object> uuidsOperations = new HashMap<>();
		uuidsOperations.put("add", uuid);

		SolrDocument document = getSequenceDocumentUsingRealtimeGet(sequenceId);

		List<String> toRemove = findUUIDSToRemove(document);
		if (!toRemove.isEmpty()) {
			uuidsOperations.put("remove", toRemove);
			solrInputDocument.addField("uuids_to_remove_ss", singletonMap("remove", toRemove));
		}

		solrInputDocument.addField("counter_d", singletonMap("inc", 1.0));
		solrInputDocument.addField("uuids_ss", uuidsOperations);
		return solrInputDocument;
	}

	private void markUUIDHasRemovable(String sequenceId, String uuid) {
		SolrInputDocument solrInputDocument;
		solrInputDocument = newSequenceUpdateInputDocument(sequenceId);
		solrInputDocument.addField("uuids_to_remove_ss", singletonMap("add", uuid));
		try {
			client.add(solrInputDocument);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private int getUUIDIndexFromEndOfList(String uuid, SolrDocument document) {
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
		return uuids.size() - index - 1;
	}

	private List<String> findUUIDSToRemove(SolrDocument document) {

		List<String> toRemove = new ArrayList<>();
		if (document != null) {
			List<String> readyToRemove = (List) document.getFieldValues("uuids_to_remove_ss");
			if (readyToRemove != null) {
				List<String> uuids = (List) document.getFieldValues("uuids_ss");

				boolean removeFirst100 = uuids.size() >= 1000;
				for (int i = 0; i < uuids.size(); i++) {
					String currentUUID = uuids.get(i);
					if ((removeFirst100 && i < 100) || readyToRemove.contains(currentUUID)) {
						toRemove.add(currentUUID);
					} else {
						break;
					}
				}

			}
		}
		return toRemove;
	}

	void createSequenceDocument(String sequenceId, String uuid)
			throws SolrServerException, IOException {
		SolrInputDocument solrInputDocument;
		solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", "seq_" + sequenceId);
		solrInputDocument.addField("_version_", "-1");
		solrInputDocument.addField("type_s", "sequence");
		solrInputDocument.addField("uuids_ss", asList(uuid));
		solrInputDocument.addField("counter_d", 1.0);
		UpdateRequest request = new UpdateRequest();
		request.add(solrInputDocument);
		request.setCommitWithin(-1);
		request.process(client);
	}

	private SolrDocument getSequenceDocumentUsingRealtimeGet(String sequenceId) {
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

	private SolrInputDocument newSequenceUpdateInputDocument(String sequenceId) {
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", "seq_" + sequenceId);
		solrInputDocument.addField("_version_", "1");
		solrInputDocument.addField("type_s", "sequence");
		return solrInputDocument;
	}
}
