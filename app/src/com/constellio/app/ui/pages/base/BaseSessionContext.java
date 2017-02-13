package com.constellio.app.ui.pages.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseSessionContext implements SessionContext {
	
	private List<SelectedRecordIdsChangeListener> selectedRecordIdsChangeListeners = new ArrayList<>();

	@Override
	public List<String> getSelectedRecordIds() {
		return Collections.unmodifiableList(ensureSelectedRecordIds());
	}

	@Override
	public void addSelectedRecordId(String recordId) {
		List<String> selectedRecordIds = ensureSelectedRecordIds();
		if (!selectedRecordIds.contains(recordId)) {
			selectedRecordIds.add(recordId);
			for (SelectedRecordIdsChangeListener listener : getSelectedRecordIdsChangeListeners()) {
				listener.recordIdAdded(recordId);
			}
		}
	}

	@Override
	public void removeSelectedRecordId(String recordId) {
		List<String> selectedRecordIds = ensureSelectedRecordIds();
		selectedRecordIds.remove(recordId);
		for (SelectedRecordIdsChangeListener listener : getSelectedRecordIdsChangeListeners()) {
			listener.recordIdRemoved(recordId);
		}
	}

	@Override
	public void clearSelectedRecordIds() {
		List<String> selectedRecordIds = ensureSelectedRecordIds();
		selectedRecordIds.clear();
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

}
