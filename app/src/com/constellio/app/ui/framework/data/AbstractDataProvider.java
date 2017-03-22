package com.constellio.app.ui.framework.data;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDataProvider implements DataProvider {

	private List<DataRefreshListener> dataRefreshListeners = new ArrayList<>();

	@Override
	public void addDataRefreshListener(DataRefreshListener dataRefreshListener) {
		this.dataRefreshListeners.add(dataRefreshListener);
	}

	@Override
	public List<DataRefreshListener> getDataRefreshListeners() {
		return dataRefreshListeners;
	}

	@Override
	public void removeDataRefreshListener(DataRefreshListener dataRefreshListener) {
		dataRefreshListeners.remove(dataRefreshListener);
	}

	@Override
	public void fireDataRefreshEvent() {
		for (DataRefreshListener dataRefreshListener : dataRefreshListeners) {
			dataRefreshListener.dataRefresh();
		}
	}

}
