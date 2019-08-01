package com.constellio.data.dao.services.bigVault;

import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.TermVectorParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class JaccardDocumentSorter {
	public static final String SIMILARITY_SCORE_FIELD = "sim_score";
	private final String idField;
	private final String contentField;
	private final Map<String, Map<String, Double>> sourceDocTermVector;
	private final Map<String, Map<String, Map<String, Double>>> doc2FieldTermVectors = new TreeMap<String, Map<String, Map<String, Double>>>();
	private final BigVaultServer server;
	private final JaccardTermVectorSimilarity similarity = new JaccardTermVectorSimilarity();

	public JaccardDocumentSorter(BigVaultServer server, SolrDocument source, String contentField, String idField)
			throws SolrServerException, IOException {
		this.idField = idField;
		this.contentField = contentField;
		this.server = server;

		sourceDocTermVector = getTermVectors(source);
		if (sourceDocTermVector == null) {
			throw new RuntimeException();
		}
	}

	private Map<String, Map<String, Double>> getTermVectors(SolrDocument doc)
			throws SolrServerException, IOException {
		String id = doc.getFieldValue(idField).toString();

		Map<String, Map<String, Double>> result = doc2FieldTermVectors.get(id);
		if (result == null) {
			SolrQuery solrQuery = new SolrQuery(String.format("%s:\"%s\"", idField, ClientUtils.escapeQueryChars(id)));
			solrQuery.setRequestHandler("/tvrh");
			String fields = String.format("%s", contentField);
			//			solrQuery.setParam(CommonParams.FL, fields);
			solrQuery.setParam(TermVectorParams.TF, "true");
			solrQuery.setParam(TermVectorParams.DF, "true");
			solrQuery.setParam(TermVectorParams.TF_IDF, "true");
			solrQuery.setParam(TermVectorParams.FIELDS, fields);
			solrQuery.setRows(1);

			QueryResponse response = null;
			try {
				response = server.query("searchMoreLikeThis " + id, solrQuery);
			} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
				throw new RuntimeException(couldNotExecuteQuery);
			}
			TermVectoreResponse termVectoreResponse = new TermVectoreResponse(response);
			Map<String, Map<String, Map<String, Map<String, Double>>>> doc2FieldTermVectors = termVectoreResponse
					.getDoc2FieldTermVectors();
			if (!doc2FieldTermVectors.containsKey(id)) {
				throw new RuntimeException(
						"The " + contentField + " does not support termVectors, please update the solr schema file.");
			}

			result = new TreeMap<>();
			for (Entry<String, Map<String, Map<String, Double>>> aFieldTermVector : doc2FieldTermVectors.get(id).entrySet()) {
				result.putAll(aFieldTermVector.getValue());
			}
			this.doc2FieldTermVectors.put(id, result);
		}
		return result;

	}

	public List<SolrDocument> sort(List<SolrDocument> results)
			throws SolrServerException, IOException {
		List<SolrDocument> sortedResults = new ArrayList<>(results.size());
		for (SolrDocument solrDocument : results) {
			solrDocument.setField(SIMILARITY_SCORE_FIELD,
					new Double(similarity.getSimilarity(sourceDocTermVector, getTermVectors(solrDocument))));
			sortedResults.add(solrDocument);
		}

		Collections.sort(sortedResults, new Comparator<SolrDocument>() {
			@Override
			public int compare(SolrDocument o1, SolrDocument o2) {
				Double score1 = getScore(o1);
				Double score2 = getScore(o2);
				Double diff = score1 - score2;
				if (diff < 0) {
					return -1;
				}
				if (diff > 0) {
					return +1;
				}
				return 0;
			}

			private Double getScore(SolrDocument o) {
				Double score = (Double) o.getFieldValue(SIMILARITY_SCORE_FIELD);
				if (score == null) {
					score = 0D;
				}
				return score;
			}
		});

		Collections.reverse(sortedResults);
		return sortedResults;
	}

}