package com.constellio.model.services.search;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;

public class SPEQueryResponseUtils {

	public static boolean isSameResults(SPEQueryResponse response1, SPEQueryResponse response2) {
		return response1.getRecords().equals(response2.getRecords()) && response1.getNumFound() == response2.getNumFound();
	}

	public static void ensureSameResponse(SPEQueryResponse solrResponse, SPEQueryResponse cacheResponse) {

		if (!isSameResults(solrResponse, cacheResponse)) {

			StringBuilder messageBuilder = new StringBuilder();

			messageBuilder.append("Solr and memory responses are not equal \n");
			messageBuilder.append("Numfound in solr response : " + solrResponse.getNumFound() + "\n");
			messageBuilder.append("Numfound in cache response : " + cacheResponse.getNumFound() + "\n");
			messageBuilder.append("\n");
			messageBuilder.append("Solr response records : \n");
			for (Record record : solrResponse.getRecords()) {
				messageBuilder.append(record.getIdTitle());
				messageBuilder.append(" version=");
				messageBuilder.append(record.getVersion());
				messageBuilder.append("\n");
			}

			messageBuilder.append("\n");
			messageBuilder.append("Memory response records : \n");
			for (Record record : cacheResponse.getRecords()) {
				messageBuilder.append(record.getIdTitle());
				messageBuilder.append(" version=");
				messageBuilder.append(record.getVersion());
				messageBuilder.append("\n");
			}

			throw new ImpossibleRuntimeException(messageBuilder.toString());
		}

	}
}
