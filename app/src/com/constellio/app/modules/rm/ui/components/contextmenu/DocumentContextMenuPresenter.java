package com.constellio.app.modules.rm.ui.components.contextmenu;

import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.utils.ReportGeneratorUtils;
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
		contextMenu.setDisplayDocumentButtonState(ComponentState.ENABLED);
		Content content = getContent();
		if (content != null) {
			ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content);
			contextMenu.setContentVersionVO(contentVersionVO);
			contextMenu.setDownloadDocumentButtonState(ComponentState.ENABLED);
			String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
			contextMenu.setOpenDocumentButtonState(agentURL != null ? ComponentState.ENABLED : ComponentState.INVISIBLE);
		} else {
			contextMenu.setContentVersionVO(null);
			contextMenu.setDownloadDocumentButtonState(ComponentState.INVISIBLE);
			contextMenu.setOpenDocumentButtonState(ComponentState.INVISIBLE);
		}
		contextMenu.buildMenuItems();
	}

	public void displayDocumentButtonClicked() {

		documentActionPresenterUtils.navigateToDisplayDocument(documentVO.getId(), ParamUtils.getCurrentParams());
		updateSearchResultClicked();
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
			this.documentVO = voBuilder.build(record, VIEW_MODE.DISPLAY, contextMenu.getSessionContext());
			setRecordVO(documentVO);
			contextMenu.setDocumentVO(documentVO);
			updateActionsComponent();
			showContextMenu = true;
		} else {
			showContextMenu = false;
		}
		contextMenu.setVisible(showContextMenu);
		return showContextMenu;
	}

	public boolean openForRequested(RecordVO recordVO) {
		return openForRequested(recordVO.getId());
	}

	public boolean hasMetadataReport() {
		return !ReportGeneratorUtils.getPrintableReportTemplate(presenterUtils.appLayerFactory(), presenterUtils.getCollection(), getDocumentVO().getSchema().getCode(), PrintableReportListPossibleType.DOCUMENT).isEmpty();
	}

	public void logOpenAgentUrl(RecordVO recordVO) {
		presenterUtils.loggingServices().openDocument(presenterUtils.getRecord(recordVO.getId()), presenterUtils.getCurrentUser());
	}

}
