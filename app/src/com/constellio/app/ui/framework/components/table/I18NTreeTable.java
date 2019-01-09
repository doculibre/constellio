package com.constellio.app.ui.framework.components.table;

import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

public class I18NTreeTable extends TreeTable {
	
	private boolean initDone = false;

	public I18NTreeTable() {
		super();
		init();
	}

	public I18NTreeTable(String caption, Container dataSource) {
		super(caption, dataSource);
		init();
	}

	public I18NTreeTable(String caption) {
		super(caption);
		init();
	}

	private void init() {
		if (!initDone) {
			setCellStyleGenerator(new CellStyleGenerator() {
				@Override
				public String getStyle(Table source, Object itemId, Object propertyId) {
					boolean hierarchyColumn = false;
					Object[] visibleColumns = getVisibleColumns();
					if (visibleColumns.length > 0) {
						hierarchyColumn = visibleColumns[visibleColumns.length - 1].equals(propertyId);
					}
					return hierarchyColumn ? "hierarchy-column" : null;
				}
			});
			addAttachListener(new AttachListener() {
				@Override
				public void attach(AttachEvent event) {
					Object[] visibleColumns = getVisibleColumns();
					if (isRightToLeft()) {
						ArrayUtils.reverse(visibleColumns);
						setVisibleColumns(visibleColumns);

						List<?> propertyIds = new ArrayList<>(getContainerPropertyIds());
						for (Object propertyId : propertyIds) {
							Align alignment = adjustAlignment(getColumnAlignment(propertyId));
							setColumnAlignment(propertyId, alignment);
						}
						if (visibleColumns.length > 0) {
							setHierarchyColumn(visibleColumns[visibleColumns.length - 1]);
						}
					}
				} 
			});
			initDone = true;
		}
	}

	private Align adjustAlignment(Align alignment) {
		Align result;
		if (isRightToLeft()) {
			if (Align.LEFT.equals(alignment)) {
				result = Align.RIGHT;
			} else if (Align.RIGHT.equals(alignment)) {
				result = Align.LEFT;
			} else if (Align.CENTER.equals(alignment)) {
				result = alignment;
			} else {
				result = Align.RIGHT;
			}
		} else {
			result = alignment;
		}
		return result;
	}

}
