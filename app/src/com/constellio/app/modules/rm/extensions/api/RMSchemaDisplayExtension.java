package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.api.extensions.SchemaDisplayExtension;
import com.constellio.app.api.extensions.params.SchemaDisplayParams;
import com.constellio.app.modules.rm.ui.pages.containers.DisplayContainerViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentViewImpl;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentWindow;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskViewImpl;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RMSchemaDisplayExtension extends SchemaDisplayExtension {

	@Override
	public Component getDisplay(SchemaDisplayParams params) {
		Component result;
		final RecordVO recordVO = params.getRecordVO();
		final String searchTerm = params.getSearchTerm();
		final Component parentComponent = params.getParentComponent();
		if (parentComponent instanceof ViewableRecordVOTablePanel) {
			final ViewableRecordVOTablePanel panel = (ViewableRecordVOTablePanel) parentComponent;
			String schemaTypeCode = recordVO.getSchema().getTypeCode();
			if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
				DisplayDocumentViewImpl view = new DisplayDocumentViewImpl(recordVO, true, false);
				view.setSearchTerm(searchTerm);
				view.enter(null);
				view.addEditWindowCloseListener(new Window.CloseListener() {
					@Override
					public void windowClose(CloseEvent e) {
						panel.refreshMetadata();
					}
				});
				result = view;
			} else if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
				DisplayFolderViewImpl view = new DisplayFolderViewImpl(recordVO, true, false);
				view.enter(null);
				result = view;
			} else if (Task.SCHEMA_TYPE.equals(schemaTypeCode)) {
				DisplayTaskViewImpl view = new DisplayTaskViewImpl(recordVO, true, false);
				view.enter(null);
				result = view;
			} else if (ContainerRecord.SCHEMA_TYPE.equals(schemaTypeCode)) {
				DisplayContainerViewImpl view = new DisplayContainerViewImpl(recordVO, false, true);
				view.enter(null);
				result = view;
			} else {
				result = null;
			}
		} else {
			result = null;
		}
		return result;
	}

	@Override
	public ViewWindow getWindowDisplay(SchemaDisplayParams params) {
		ViewWindow result;
		RecordVO recordVO = params.getRecordVO();
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			try {
				result = new DisplayDocumentWindow(recordVO);
			} catch (UserDoesNotHaveAccessException e) {
				log.error(e.getMessage(), e);
				result = null;
			}
		} else {
			result = null;
		}
		return result;
	}


}
