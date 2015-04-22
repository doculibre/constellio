/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
