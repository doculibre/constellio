package com.constellio.app.ui.application;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;

public abstract class OneInstanceViewProvider implements ViewProvider {
	
	private View singleInstance;
	
	public String viewName;
	
	public OneInstanceViewProvider(String viewName) {
		this.viewName = viewName;
	}

	@Override
	public String getViewName(String viewAndParameters) {
        if (null == viewAndParameters) {
            return null;
        }
        if (viewAndParameters.equals(viewName)
                || viewAndParameters.startsWith(viewName + "/")) {
            return viewName;
        }
        return null;
	}

	@Override
	public final View getView(String viewName) {
		if (singleInstance == null) {
			singleInstance = newView(viewName);
		}	
		return singleInstance;
	}
	
	protected abstract View newView(String viewName);

}
