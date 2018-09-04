package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.collection.CollectionGroupView;
import com.vaadin.ui.Button;
import org.apache.solr.ltr.feature.SolrFeature;

public interface SolrFeatureView extends BaseView, CollectionGroupView {

	void refreshTable();

	Button buildAddEditForm(final SolrFeature feature);

}
