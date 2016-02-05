package com.constellio.data.dao.services.bigVault;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class JaccardTermVectorSimilarity {
	public static final String TF = "tf";
	public static final String TF_IDF = "tf-idf";

	private final String freqTag;
	private final String scoreTag;

	public JaccardTermVectorSimilarity() {
		this.freqTag = TF;
		this.scoreTag = TF_IDF;
	}

	public double getSimilarity(Map<String, Map<String, Double>> doc1, Map<String, Map<String, Double>> doc2) {
		return intersection(doc1, doc2) / union(doc1, doc2);
	}

	public double intersection(Map<String, Map<String, Double>> doc1, Map<String, Map<String, Double>> doc2) {
		Set<String> intersectTerms = new TreeSet<>(doc1.keySet());
		intersectTerms.retainAll(doc2.keySet());

		double intersectSize = 0;
		for (String term : intersectTerms) {
			double minFreq = Math.min(safeGetVal(doc1, term, freqTag), safeGetVal(doc2, term, freqTag));
			double minScore = Math.min(safeGetVal(doc1, term, scoreTag), safeGetVal(doc2, term, scoreTag));
			intersectSize += minFreq * minScore;
		}
		return intersectSize;
	}

	public double union(Map<String, Map<String, Double>> doc1, Map<String, Map<String, Double>> doc2) {
		Set<String> intersectTerms = new TreeSet<>(doc1.keySet());
		intersectTerms.addAll(doc2.keySet());

		double intersectSize = 0;
		for (String term : intersectTerms) {
			double minFreq = Math.max(safeGetVal(doc1, term, freqTag), safeGetVal(doc2, term, freqTag));
			double minScore = Math.max(safeGetVal(doc1, term, scoreTag), safeGetVal(doc2, term, scoreTag));
			intersectSize += minFreq * minScore;
		}
		return intersectSize;
	}

	private Double safeGetVal(Map<String, Map<String, Double>> doc, String term, String tag) {
		Map<String, Double> map = doc.get(term);
		if (map == null)
			return 0.0;

		return map.get(tag);
	}

}