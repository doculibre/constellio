package com.constellio.data.dao.services.sequence;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class SolrSequencesManager implements SequencesManager {

	SecondTransactionLogManager secondTransactionLogManager;

	SolrClient client;

	public SolrSequencesManager(RecordDao recordDao, SecondTransactionLogManager secondTransactionLogManager) {
		this.client = recordDao.getBigVaultServer().getNestedSolrServer();
		this.secondTransactionLogManager = secondTransactionLogManager;
	}

	@Override
	public void set(String sequenceId, long value) {
		if (StringUtils.isBlank(sequenceId)) {
			throw new IllegalArgumentException("sequenceId is blank");
		}
		try {
			SolrInputDocument document = newSequenceUpdateInputDocument(sequenceId);
			document.addField("counter_d", new Long(value).doubleValue());

			if (secondTransactionLogManager != null) {
				secondTransactionLogManager.setSequence(sequenceId, value);
			}

			client.add(document);

		} catch (Exception e) {
			//The document does not exist

			try {
				createSequenceDocument(sequenceId, null);

			} catch (SolrServerException | IOException e1) {
				throw new RuntimeException(e1);
			}
			set(sequenceId, value);
		}

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

		if (secondTransactionLogManager != null) {
			secondTransactionLogManager.nextSequence(sequenceId);
		}

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

	@Override
	public Map<String, Long> getSequences() {

		try {
			client.commit(true, true, true);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map<String, Long> sequences = new HashMap<>();
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.add("q", "id:seq_*");
		solrParams.set("rows", 1000000);
		try {
			QueryResponse queryResponse = client.query(solrParams);

			for (SolrDocument doc : queryResponse.getResults()) {
				String id = ((String) doc.get("id")).substring(4);
				Double value = ((Double) doc.get("counter_d"));
				sequences.put(id, value.longValue());
			}

		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}

		return Collections.unmodifiableMap(sequences);
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
		} catch (SolrServerException | IOException e) {
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
		if (uuid == null) {
			solrInputDocument.addField("uuids_ss", new ArrayList<>());
		} else {
			solrInputDocument.addField("uuids_ss", asList(uuid));
		}
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
		} catch (IOException | SolrServerException e) {
			throw new RuntimeException(e);
		}
		return (SolrDocument) response.getResponse().get("doc");
	}

	public static SolrInputDocument setSequenceInLogReplay(String sequenceId, long value) {
		String uuid = UUIDV1Generator.newRandomId();
		SolrInputDocument solrInputDocument;
		solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", "seq_" + sequenceId);
		solrInputDocument.addField("_version_", "-1");
		solrInputDocument.addField("type_s", "sequence");
		if (uuid == null) {
			solrInputDocument.addField("uuids_ss", new ArrayList<>());
		} else {
			solrInputDocument.addField("uuids_ss", asList(uuid));
		}
		solrInputDocument.addField("counter_d", new Long(value).doubleValue());
		solrInputDocument.remove("_version_");
		return solrInputDocument;

	}

	public static SolrInputDocument incrementSequenceInLogReplay(String sequenceId) {
		SolrInputDocument solrInputDocument = newSequenceUpdateInputDocument(sequenceId);
		solrInputDocument.addField("counter_d", singletonMap("inc", 1.0));
		solrInputDocument.remove("_version_");
		return solrInputDocument;
	}

	private static SolrInputDocument newSequenceUpdateInputDocument(String sequenceId) {
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", "seq_" + sequenceId);
		solrInputDocument.addField("_version_", "1");
		solrInputDocument.addField("type_s", "sequence");
		return solrInputDocument;
	}
}
