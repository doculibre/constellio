package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.vaadin.data.Container;

public class DynamicHeightTable extends BaseTable {

	public static final String STYLE_NAME = "dynamic-height";

	public static final int HEADER_HEIGHT = 38; // PIXELS

	public static final int ROW_HEIGHT = 45; // PIXELS

	/* Sizeable and other size related methods */

	public DynamicHeightTable(String tableId) {
		super(tableId);
		init();
	}

	public DynamicHeightTable(String tableId, String caption, Container dataSource) {
		super(tableId, caption, dataSource);
		init();
	}

	public DynamicHeightTable(String tableId, String caption) {
		super(tableId, caption);
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
			int exceedingPixels = size > 0 ? (size - 1) : 0;
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
