package com.constellio.app.extensions.ui;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.vaadin.server.Resource;

public class ViewableRecordVOTablePanelExtension {

	public Boolean isDisplayInWindowOnSelection(ViewableRecordVOTablePanelExtensionParams params) {
		return null;
	}

	public Resource getThumbnail(ViewableRecordVOTablePanelExtensionParams params) {
		return null;
	}

	public static class ViewableRecordVOTablePanelExtensionParams {

		private RecordVO recordVO;

		private String searchTerm;

		private ViewableRecordVOTablePanel panel;

		public ViewableRecordVOTablePanelExtensionParams(RecordVO recordVO, String searchTerm,
														 ViewableRecordVOTablePanel panel) {
			this.recordVO = recordVO;
			this.searchTerm = searchTerm;
			this.panel = panel;
		}

		public RecordVO getRecordVO() {
			return recordVO;
		}

		public String getSearchTerm() {
			return searchTerm;
		}

		public ViewableRecordVOTablePanel getPanel() {
			return panel;
		}

	}

}
