package com.constellio.app.ui.framework.components.table;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.SessionContext.SelectedRecordIdsChangeListener;
import com.vaadin.data.Item;
import com.vaadin.ui.Table;

public abstract class RecordVOSelectionTableAdapter extends SelectionTableAdapter implements SelectedRecordIdsChangeListener {
	
	private Map<String, Object> recordIdsToItemIdsMap = new HashMap<>();
	
	public RecordVOSelectionTableAdapter() {
		this(null);
	}
	
	public RecordVOSelectionTableAdapter(Table table) {
		super(table);
	}

	@Override
	public void attach() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		boolean listenerAlreadyAdded = false;
		for (SelectedRecordIdsChangeListener listener : sessionContext.getSelectedRecordIdsChangeListeners()) {
			if (listener == this) {
				listenerAlreadyAdded = true;
			}
		}
		if (!listenerAlreadyAdded) {
			sessionContext.addSelectedRecordIdsChangeListener(this);
		}
		super.attach();
	}

	@Override
	public void detach() {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		sessionContext.removeSelectedRecordIdsChangeListener(this);
		super.detach();
	}
	
	private Object getItemId(String recordId) {
		Object itemId = recordIdsToItemIdsMap.get(recordId);
		if (itemId == null) {
			for (Object tableItemId : getLoadedItemIds()) {
				Item item = table.getItem(tableItemId);
				if (item instanceof RecordVOItem) {
					RecordVOItem recordVOItem = (RecordVOItem) table.getItem(tableItemId);
					String tableRecordId = recordVOItem.getRecord().getId();
					if (tableRecordId.equals(recordId)) {
						itemId = tableItemId;
						recordIdsToItemIdsMap.put(recordId, itemId);
						break;
					}
				}
			}
		}
		return itemId;
	}
	
	private Collection<?> getLoadedItemIds() {
		return table.getVisibleItemIds();
	}

	@Override
	public void recordIdAdded(String recordId) {
		if (table != null) {
			Object itemId = getItemId(recordId);
			if (itemId != null && !Boolean.TRUE.equals(isChecked(itemId))) {
				setChecked(itemId, true);
			}
		}
	}

	@Override
	public void recordIdRemoved(String recordId) {
		if (table != null) {
			Object itemId = getItemId(recordId);
			if (itemId != null && !Boolean.FALSE.equals(isChecked(itemId))) {
				setChecked(itemId, false);
			}
		}
	}

	@Override
	public void selectionCleared() {
		if (table != null) {
			for (Object itemId : getLoadedItemIds()) {
				if (!Boolean.FALSE.equals(isChecked(itemId))) {
					setChecked(itemId, false);
				}
			}
		}
	}

}
