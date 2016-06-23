package com.constellio.data.dao.services.sequence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.UpdateParams;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.records.RecordDao;

public class TurboSolrSequencesManager implements SequencesManager {

	Map<String, RecordDTO> sequences = new HashMap<>();

	SolrClient client;
	RecordDao recordDao;

	static Map<String, Object> INCREMENT_BY_ONE = new HashMap<>();

	static {
		INCREMENT_BY_ONE.put("inc", 1.0);
	}

	public TurboSolrSequencesManager(RecordDao recordDao) {
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

	@Override
	public synchronized long next(String sequenceId) {

		try {
			recordDao.get("seq_" + sequenceId);
		} catch (NoSuchRecordWithId noSuchRecordWithId) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.setField("id", "seq_" + sequenceId);
			doc.setField("type_s", "sequence");
			doc.setField("counter_d", 0.0);
			try {
				client.add(doc);
				client.commit();
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return 1L;
		}

		UpdateRequest request = new UpdateRequest();

		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", "seq_" + sequenceId);
		doc.setField("_version_", "1");
		doc.setField("type_s", "sequence");
		doc.setField("counter_d", INCREMENT_BY_ONE);
		request.setParam(UpdateParams.VERSIONS, "true");
		request.add(doc);

		try {
			UpdateResponse response = request.process(client);
			System.out.println(response);
			throw new RuntimeException("TODO");
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
