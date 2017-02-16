package com.constellio.app.ui.pages.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BaseSessionContext implements SessionContext {
	
	private List<SelectedRecordIdsChangeListener> selectedRecordIdsChangeListeners = new ArrayList<>();

	@Override
	public List<String> getSelectedRecordIds() {
		return Collections.unmodifiableList(ensureSelectedRecordIds());
	}

	@Override
	public Map<String, Long> getSelectedRecordSchemaTypeCodes() {
		return Collections.unmodifiableMap(ensureSelectedRecordSchemaTypeCodes());
	}

	@Override
	public void addSelectedRecordId(String recordId, String schemaTypeCode) {
		List<String> selectedRecordIds = ensureSelectedRecordIds();
		Map<String, Long> selectedRecordSchemaTypeCodes = ensureSelectedRecordSchemaTypeCodes();
		if (!selectedRecordIds.contains(recordId)) {
			selectedRecordIds.add(recordId);
			if(selectedRecordSchemaTypeCodes.containsKey(schemaTypeCode)) {
				selectedRecordSchemaTypeCodes.put(schemaTypeCode, selectedRecordSchemaTypeCodes.get(schemaTypeCode)+1);
			} else {
				selectedRecordSchemaTypeCodes.put(schemaTypeCode, 1L);
			}
			for (SelectedRecordIdsChangeListener listener : getSelectedRecordIdsChangeListeners()) {
				listener.recordIdAdded(recordId);
			}
		}
	}

	@Override
	public void removeSelectedRecordId(String recordId, String schemaTypeCode) {
		List<String> selectedRecordIds = ensureSelectedRecordIds();
		selectedRecordIds.remove(recordId);
		Map<String, Long> selectedRecordSchemaTypeCodes = ensureSelectedRecordSchemaTypeCodes();
		selectedRecordSchemaTypeCodes.put(schemaTypeCode, selectedRecordSchemaTypeCodes.get(schemaTypeCode)-1);
		if(selectedRecordSchemaTypeCodes.get(selectedRecordSchemaTypeCodes.get(schemaTypeCode)) <= 0) {
			selectedRecordSchemaTypeCodes.remove(schemaTypeCode);
		}

		for (SelectedRecordIdsChangeListener listener : getSelectedRecordIdsChangeListeners()) {
			listener.recordIdRemoved(recordId);
		}
	}

	@Override
	public void clearSelectedRecordIds() {
		List<String> selectedRecordIds = ensureSelectedRecordIds();
		selectedRecordIds.clear();
		Map<String, Long> selectedRecordSchemaTypeCodes = ensureSelectedRecordSchemaTypeCodes();
		selectedRecordSchemaTypeCodes.clear();
		for (SelectedRecordIdsChangeListener listener : getSelectedRecordIdsChangeListeners()) {
			listener.selectionCleared();
		}
	}
	
	public List<SelectedRecordIdsChangeListener> getSelectedRecordIdsChangeListeners() {
		return Collections.unmodifiableList(selectedRecordIdsChangeListeners);
	}
	
	public void addSelectedRecordIdsChangeListener(SelectedRecordIdsChangeListener listener) {
		this.selectedRecordIdsChangeListeners.add(listener);
	}
	
	public void removeSelectedRecordIdsChangeListener(SelectedRecordIdsChangeListener listener) {
		this.selectedRecordIdsChangeListeners.remove(listener);
	}
	
	protected abstract List<String> ensureSelectedRecordIds();

	protected abstract Map<String, Long> ensureSelectedRecordSchemaTypeCodes();
}
