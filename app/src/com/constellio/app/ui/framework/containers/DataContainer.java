package com.constellio.app.ui.framework.containers;

import com.constellio.app.ui.framework.data.DataProvider;
import com.constellio.app.ui.framework.data.DataProvider.DataRefreshListener;
import com.vaadin.data.util.IndexedContainer;

@SuppressWarnings("serial")
public abstract class DataContainer<T extends DataProvider> extends ContainerAdapter<IndexedContainer> implements RefreshableContainer {
	
	private T dataProvider;

	public DataContainer(T dataProvider) {
		super(new IndexedContainer());
		this.dataProvider = dataProvider;
		dataProvider.addDataRefreshListener(new DataRefreshListener() {
			@Override
			public void dataRefresh() {
				DataContainer.this.refresh();
			}
		});
	}

	@Override
	public void refresh() {
		removeAllItems();
		populateFromData(dataProvider);
	}
	
	protected abstract void populateFromData(T dataProvider);
	
}
