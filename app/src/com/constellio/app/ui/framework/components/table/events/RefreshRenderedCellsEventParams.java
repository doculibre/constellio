package com.constellio.app.ui.framework.components.table.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class RefreshRenderedCellsEventParams {
	List<Object> selectedIds;
	boolean areAllItemSelected;
}
