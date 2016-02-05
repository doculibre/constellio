package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.entities.SearchBoostVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.collection.CollectionGroupView;
import com.vaadin.ui.Button;

public interface SearchBoostView extends BaseView, CollectionGroupView {

	void refreshTable();

	Button buildAddEditForm(final SearchBoostVO searchBoostVO);

}
