package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.builders.SearchBoostToVOBuilder;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;

public class SearchBoostByQueryPresenter extends SearchBoostPresenter {

	private final String TYPE = "query";

	public SearchBoostByQueryPresenter(SearchBoostView view) {
		super(view);
	}

	public SearchBoostDataProvider newDataProvider() {
		return new SearchBoostDataProvider(TYPE, collection, new SearchBoostToVOBuilder(), modelLayerFactory);
	}

	boolean validate(SearchBoostVO searchBoostVO, String value) {
		if (searchBoostVO == null) {
			showErrorMessageView($("SearchBoostByQueryView.invalidLabelQuery"));
			return false;
		} else {
			if (StringUtils.isBlank(searchBoostVO.getLabel())) {
				showErrorMessageView($("SearchBoostByQueryView.invalidLabel"));
				return false;
			}
			if (StringUtils.isBlank(searchBoostVO.getKey())) {
				showErrorMessageView($("SearchBoostByQueryView.invalidQuery"));
				return false;
			}
		}
		try {
			Double.valueOf(value);
		} catch (NumberFormatException e) {
			showErrorMessageView($("SearchBoostByQueryView.invalidValue"));
			return false;
		}
		return true;
	}

	@Override
	String getSearchBoostType() {
		return TYPE;
	}

	public SearchBoostVO getSearchBoostVO(Integer itemId, SearchBoostDataProvider provider) {
		Integer index = itemId;
		return provider.getSearchBoostVO(index);
	}
}
