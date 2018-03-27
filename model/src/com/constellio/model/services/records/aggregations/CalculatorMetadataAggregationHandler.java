package com.constellio.model.services.records.aggregations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedCalculator;
import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;
import com.constellio.model.entities.schemas.entries.SearchAggregatedValuesParams;

public class CalculatorMetadataAggregationHandler implements MetadataAggregationHandler {

	@Override
	public Object calculate(SearchAggregatedValuesParams params) {
		Class<? extends AggregatedCalculator<?>> calculatorClass = params.getAggregatedDataEntry().getAggregatedCalculator();
		Metadata metadata = params.getMetadata();
		if (calculatorClass != null) {
			try {
				return calculatorClass.newInstance().calculate(params);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Invalid aggregatedCalculator for metadata : " + metadata.getCode());
			}
		} else {
			throw new RuntimeException("Invalid aggregatedCalculator for metadata : " + metadata.getCode());
		}
	}


	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		Class<? extends AggregatedCalculator<?>> calculatorClass = params.getAggregatedDataEntry().getAggregatedCalculator();
		Metadata metadata = params.getMetadata();
		if (calculatorClass != null) {
			try {
				return calculatorClass.newInstance().calculate(params);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Invalid aggregatedCalculator for metadata : " + metadata.getCode());
			}
		} else {
			throw new RuntimeException("Invalid aggregatedCalculator for metadata : " + metadata.getCode());
		}
	}

	@Override
	public List<Metadata> getMetadatasUsedToCalculate(GetMetadatasUsedToCalculateParams params) {
		List<Metadata> metadatas = new ArrayList<>();
		try {
			List<String> metadataDependencies = params.getAggregatedDataEntry().getAggregatedCalculator().newInstance()
					.getMetadataDependencies();
			if (metadataDependencies != null) {
				for (String metadataCode : metadataDependencies) {
					metadatas.add(params.getMetadata(metadataCode));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid AggregatedCalculator for metadata : " + params.getAggregatedMetadata().getCode());
		}
		return metadatas;
	}
}
