package com.constellio.model.services.search.moreLikeThis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MoreLikeThisClustering{
	public static interface StringConverter<T>{
		public String converToString(T obj);
	}
	
	private Map<String, Double> facetValues = new TreeMap<>();

	public <T> MoreLikeThisClustering(Map<T, Double> records, StringConverter<T> converter) {
		
		double maxScore = 0;

		for (Entry<T, Double> recordWithScore: records.entrySet()){
			String facetValue = converter.converToString(recordWithScore.getKey());
			if (facetValue == null)
				continue;
			Double score = facetValues.get(facetValue);
			if (score == null)
				score = 0d;
			
			score += recordWithScore.getValue();
			if (score > maxScore){
				maxScore = score;
			}
			
			facetValues.put(facetValue, score);
		}

		for (Entry<String, Double> aScore: facetValues.entrySet()){
			aScore.setValue(aScore.getValue()/maxScore);
		}
	}
	
	public Map<String, Double> getClusterScore(){
		List<Entry<String, Double>> facetsValues = new ArrayList<>(facetValues.entrySet());
		Collections.sort(facetsValues, new Comparator<Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				double diff = o1.getValue() - o2.getValue();
				if (diff > 0)
					return 1;
				if (diff < 0)
					return -1;
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		
		Collections.reverse(facetsValues);
		LinkedHashMap<String, Double> result = new LinkedHashMap<>();
		for (Entry<String, Double> anEntry: facetsValues){
			result.put(anEntry.getKey(), anEntry.getValue());
		}
 		return result;
	}
}
