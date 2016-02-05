package com.constellio.app.ui.framework.data;

import java.io.Serializable;

public interface DataProvider extends Serializable {
	
	public static abstract class DataRefreshListener implements Serializable {
		
		public abstract void dataRefresh();
		
	}
	
}
