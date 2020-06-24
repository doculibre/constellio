package com.constellio.app.modules.rm.extensions.ui;

import com.constellio.app.extensions.ui.ViewableRecordVOTablePanelExtension;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.viewers.panel.ViewableRecordVOTablePanel;
import com.constellio.model.services.records.RecordServices;
import org.apache.commons.lang3.StringUtils;

public class RMViewableRecordVOTablePanelExtension extends ViewableRecordVOTablePanelExtension {

	AppLayerFactory appLayerFactory;
	String collection;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;

	public RMViewableRecordVOTablePanelExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public Boolean isDisplayInWindowOnSelection(ViewableRecordVOTablePanelExtensionParams params) {
		Boolean displayInWindowOnSelection;
		RecordVO recordVO = params.getRecordVO();
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayInWindowOnSelection = false;
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			displayInWindowOnSelection = true;
		} else {
			displayInWindowOnSelection = null;
		}
		return displayInWindowOnSelection;
	}

	@Override
	public Boolean isViewerSelectionPossible(ViewableRecordVOTablePanelExtensionParams params) {
		Boolean selectionPossible;
		RecordVO recordVO = params.getRecordVO();
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		ViewableRecordVOTablePanel viewerPanel = params.getPanel();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			selectionPossible = !viewerPanel.isNested();
		} else {
			selectionPossible = null;
		}
		return selectionPossible;
	}

	@Override
	public boolean navigateFromViewerToRecordVO(ViewableRecordVOTablePanelExtensionParams params) {
		boolean navigationHandledByExtension;
		RecordVO recordVO = params.getRecordVO();
		String recordId = recordVO.getId();
		String schemaTypeCode = recordVO.getSchema().getTypeCode();
		ViewableRecordVOTablePanel viewerPanel = params.getPanel();
		if (Folder.SCHEMA_TYPE.equals(schemaTypeCode)) {
			navigationHandledByExtension = true;
			if (viewerPanel.isNested()) {
				String parentFolderId = recordVO.get(Folder.PARENT_FOLDER);
				if (StringUtils.isNotBlank(parentFolderId)) {
					navigateToDisplayFolder(parentFolderId);
				} else {
					navigateToDisplayFolder(recordId);
				}
			} else {
				navigateToDisplayFolder(recordId);
			}
		} else {
			navigationHandledByExtension = false;
		}
		return navigationHandledByExtension;
	}

	private void navigateToDisplayFolder(String recordId) {
		ConstellioUI.getCurrent().navigateTo(RMViews.class).displayFolder(recordId);
	}

}
