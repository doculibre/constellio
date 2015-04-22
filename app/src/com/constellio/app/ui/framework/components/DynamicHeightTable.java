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
package com.constellio.app.ui.framework.components;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;

public class DynamicHeightTable extends Table {
	
	public static final String STYLE_NAME = "dynamic-height";

	public static final int HEADER_HEIGHT = 38; // PIXELS
	
	public static final int ROW_HEIGHT = 45; // PIXELS
	
    /* Sizeable and other size related methods */

    public DynamicHeightTable() {
		super();
		init();
	}

	public DynamicHeightTable(String caption, Container dataSource) {
		super(caption, dataSource);
		init();
	}

	public DynamicHeightTable(String caption) {
		super(caption);
		init();
	}
	
	private void init() {
		addStyleName(STYLE_NAME);
	}

	/*
     * (non-Javadoc)
     * 
     * @see com.vaadin.Sizeable#getHeight()
     */
    @Override
    public float getHeight() {
    	int totalHeight;
    	
    	int pageLength = getPageLength();
    	if (pageLength == 0) {
    		pageLength = getContainerDataSource().size();
    	}
    	
    	int size = pageLength;
    	if (size > 0) {
        	int exceedingPixels = size > 0 ? (size -1) : 0;
        	totalHeight = (ROW_HEIGHT * size) - exceedingPixels;
    	} else {
    		totalHeight = 0;
    	}
    	if (getColumnHeaderMode() != ColumnHeaderMode.HIDDEN) {
    		totalHeight += HEADER_HEIGHT;
    	}
    	return totalHeight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.server.Sizeable#getHeightUnits()
     */
    @Override
    public Unit getHeightUnits() {
        return Unit.PIXELS;
    }

}
