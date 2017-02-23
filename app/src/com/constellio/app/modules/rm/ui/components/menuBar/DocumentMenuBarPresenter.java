package com.constellio.app.modules.rm.ui.components.menuBar;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.schemas.SchemaUtils;

public class DocumentMenuBarPresenter extends DocumentActionsPresenterUtils<DocumentMenuBar> {
	
	private DocumentMenuBar menuBar;

	public DocumentMenuBarPresenter(DocumentMenuBar menuBar) {
		super(menuBar);
		this.menuBar = menuBar;
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		super.setRecordVO(recordVO);
		updateActionsComponent();
	}

	@Override
	public void updateActionsComponent() {
		super.updateActionsComponent();
		Content content = getContent();
		if (content != null) {
			ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content);
			menuBar.setContentVersionVO(contentVersionVO);
			menuBar.setDownloadDocumentButtonVisible(true);
		} else {
			menuBar.setDownloadDocumentButtonVisible(false);
		}
		menuBar.buildMenuItems();
	}

	public void displayDocumentButtonClicked() {
		menuBar.navigate().to(RMViews.class).displayDocument(documentVO.getId());
	}

	public boolean openForRequested(String recordId) {
		boolean showContextMenu;
		Record record = presenterUtils.getRecord(recordId);
		String recordSchemaCode = record.getSchemaCode();
		String recordSchemaTypeCode = SchemaUtils.getSchemaTypeCode(recordSchemaCode);

		if (Event.SCHEMA_TYPE.equals(recordSchemaTypeCode)) {
			Event event = new Event(record, presenterUtils.types());
			recordSchemaCode = event.getType().split("_")[1];
			recordSchemaTypeCode = SchemaUtils.getSchemaTypeCode(recordSchemaCode);
			String linkedRecordId = event.getRecordId();
			record = presenterUtils.getRecord(linkedRecordId);
		}

		if (Document.SCHEMA_TYPE.equals(recordSchemaTypeCode)) {
			this.documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, menuBar.getSessionContext());
			menuBar.setDocumentVO(documentVO);
			updateActionsComponent();
			showContextMenu = true;
		} else {
			showContextMenu = false;
		}
		menuBar.setVisible(showContextMenu);
		return showContextMenu;
	}

	public boolean openForRequested(RecordVO recordVO) {
		return openForRequested(recordVO.getId());
	}

}
