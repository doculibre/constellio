package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.framework.data.SearchBoostDataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchBoostManager;
import com.constellio.model.services.search.entities.SearchBoost;

public abstract class SearchBoostPresenter extends BasePresenter<SearchBoostView> {

	private transient SearchBoostManager searchBoostManager;

	public SearchBoostPresenter(SearchBoostView view) {
		super(view);
		init();
	}

	private void init() {
		searchBoostManager = modelLayerFactory.getSearchBoostManager();
	}

	public abstract SearchBoostDataProvider newDataProvider();

	public void addButtonClicked(SearchBoostVO searchBoostVO, String value) {
		if (!validate(searchBoostVO, value)) {
			return;
		}
		searchBoostVO.setType(getSearchBoostType());
		searchBoostVO.setValue(Double.valueOf(value));
		searchBoostManager.add(collection, toSearchBoost(searchBoostVO));
		view.refreshTable();
	}

	abstract boolean validate(SearchBoostVO searchBoostVO, String value);

	void showErrorMessageView(String text) {
		view.showErrorMessage($(text));
	}

	SearchBoost toSearchBoost(SearchBoostVO searchBoostVO) {
		return new SearchBoost(searchBoostVO.getType(), searchBoostVO.getKey(), searchBoostVO.getLabel(),
				searchBoostVO.getValue());
	}

	public void editButtonClicked(SearchBoostVO searchBoostVO, String value, SearchBoostVO oldSearchBoostVO) {
		if (!validate(searchBoostVO, value)) {
			return;
		}
		searchBoostManager.delete(collection, toSearchBoost(oldSearchBoostVO));
		searchBoostVO.setValue(Double.valueOf(value));
		searchBoostVO.setType(getSearchBoostType());
		searchBoostManager.add(collection, toSearchBoost(searchBoostVO));
		view.refreshTable();
	}

	abstract String getSearchBoostType();

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices().has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public void deleteButtonClicked(SearchBoostVO searchBoostVO) {
		searchBoostManager.delete(collection, toSearchBoost(searchBoostVO));
		view.refreshTable();
	}

	public SearchBoostVO getSearchBoostVO(Integer itemId, SearchBoostDataProvider provider) {
		Integer index = itemId;
		return provider.getSearchBoostVO(index);
	}
}
