package com.constellio.app.modules.rm.ui.components.contextmenu;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.schemas.SchemaUtils;

public class DocumentContextMenuPresenter extends DocumentActionsPresenterUtils<DocumentContextMenu> {

	private DocumentContextMenu contextMenu;
	DocumentActionsPresenterUtils<DocumentContextMenu> documentActionPresenterUtils;

	public DocumentContextMenuPresenter(DocumentContextMenu contextMenu) {
		super(contextMenu);
		this.contextMenu = contextMenu;
		documentActionPresenterUtils = new DocumentActionsPresenterUtils<>(contextMenu);
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
			contextMenu.setContentVersionVO(contentVersionVO);
			String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
		} else {
			contextMenu.setContentVersionVO(null);
		}
		contextMenu.buildMenuItems();
	}

	public boolean openForRequested(String recordId) {
		if (recordId != null) {
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
				this.documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, contextMenu.getSessionContext());
				setRecordVO(documentVO);
				contextMenu.setRecordVO(documentVO);
				updateActionsComponent();
				showContextMenu = true;
			} else {
				showContextMenu = false;
			}
			contextMenu.setVisible(showContextMenu);
			return showContextMenu;
		} else {
			contextMenu.setVisible(false);
			return false;
		}

	}

	public boolean openForRequested(RecordVO recordVO) {
		return openForRequested(recordVO.getId());
	}

	public String getCollection() {
		return presenterUtils.getCollection();
	}
}
