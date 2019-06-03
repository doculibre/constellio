package com.constellio.app.ui.framework.components.viewers.panel;

import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderView;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.search.SearchView;
import com.vaadin.navigator.ViewChangeListener;

import java.util.HashMap;
import java.util.Map;

public class ViewableRecordVOViewChangeListener implements ViewChangeListener {

	private Map<String, Integer> searchViewReturnIndexes = new HashMap<>();

	private Map<String, Integer> displayFolderViewReturnIndexes = new HashMap<>();

	@Override
	public boolean beforeViewChange(ViewChangeEvent event) {
		BaseView oldView = (BaseView) event.getOldView();
		if (oldView instanceof SearchView) {
			SearchView searchView = (SearchView) oldView;
			String savedSearchId = searchView.getSavedSearchId();
			if (savedSearchId != null) {
				Integer returnIndex = searchView.getReturnIndex();
				searchViewReturnIndexes.put(savedSearchId, returnIndex);
			}
		} else if (oldView instanceof DisplayFolderView) {
			DisplayFolderView displayFolderView = (DisplayFolderView) oldView;
			RecordVO recordVO = displayFolderView.getRecord();
			Integer returnIndex = displayFolderView.getReturnIndex();
			displayFolderViewReturnIndexes.put(recordVO.getId(), returnIndex);
		}
		return true;
	}

	@Override
	public void afterViewChange(ViewChangeEvent event) {
		BaseView newView = (BaseView) event.getNewView();
		if (newView instanceof DisplayFolderView) {
			DisplayFolderView displayFolderView = (DisplayFolderView) newView;
			RecordVO recordVO = displayFolderView.getRecord();
			Integer returnIndex = displayFolderViewReturnIndexes.get(recordVO.getId());
			if (returnIndex != null) {
				displayFolderView.scrollIntoView(returnIndex);
			}
		} else if (newView instanceof SearchView) {
			SearchView searchView = (SearchView) newView;
			String savedSearchId = searchView.getSavedSearchId();
			if (savedSearchId != null) {
				Integer returnIndex = searchViewReturnIndexes.get(savedSearchId);
				if (returnIndex != null) {
					searchView.scrollIntoView(returnIndex);
				}
			}
		}
	}

}
