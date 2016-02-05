package com.constellio.app.ui.framework.builders;

import java.io.Serializable;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.model.services.search.entities.SearchBoost;

@SuppressWarnings("serial")
public class SearchBoostToVOBuilder implements Serializable {

	public SearchBoostVO build(SearchBoost searchBoost) {
		String key = searchBoost.getKey();
		String label = searchBoost.getLabel();
		double value = searchBoost.getValue();
		String type = searchBoost.getType();

		return new SearchBoostVO(type, key, label, value);

	}
}