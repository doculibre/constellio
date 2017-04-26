package com.constellio.app.ui.framework.data;

import java.io.Serializable;
import java.util.List;

public interface DataProvider extends Serializable {
	
	void addDataRefreshListener(DataRefreshListener dataRefreshListener);
	
	List<DataRefreshListener> getDataRefreshListeners();
	
	void removeDataRefreshListener(DataRefreshListener dataRefreshListener);
	
	void fireDataRefreshEvent();
	
	public static abstract class DataRefreshListener implements Serializable {
		
		public abstract void dataRefresh();
		
	}
	
}
