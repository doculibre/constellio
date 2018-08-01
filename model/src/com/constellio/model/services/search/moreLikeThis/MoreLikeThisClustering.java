package com.constellio.model.services.search.moreLikeThis;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.MoreLikeThisRecord;

import java.util.*;
import java.util.Map.Entry;

public class MoreLikeThisClustering {
	public static interface StringConverter<T> {
		public String converToString(T obj);
	}

	private Map<String, Double> facetValues = new TreeMap<>();

	public MoreLikeThisClustering(List<MoreLikeThisRecord> records, StringConverter<Record> converter) {

		double maxScore = 0;

		for (MoreLikeThisRecord moreLikeThisRecord : records) {
			String facetValue = converter.converToString(moreLikeThisRecord.getRecord());
			if (facetValue == null) {
				continue;
			}
			Double score = facetValues.get(facetValue);
			if (score == null) {
				score = 0d;
			}

			score += moreLikeThisRecord.getScore();
			if (score > maxScore) {
				maxScore = score;
			}

			facetValues.put(facetValue, score);
		}

		for (Entry<String, Double> aScore : facetValues.entrySet()) {
			aScore.setValue(aScore.getValue() / maxScore);
		}
	}

	public Map<String, Double> getClusterScore() {
		List<Entry<String, Double>> facetsValues = new ArrayList<>(facetValues.entrySet());
		Collections.sort(facetsValues, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1,
							   Entry<String, Double> o2) {
				double diff = o1.getValue() - o2.getValue();
				if (diff > 0) {
					return 1;
				}
				if (diff < 0) {
					return -1;
				}
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		Collections.reverse(facetsValues);
		LinkedHashMap<String, Double> result = new LinkedHashMap<>();
		for (Entry<String, Double> anEntry : facetsValues) {
			result.put(anEntry.getKey(), anEntry.getValue());
		}
		return result;
	}
}
