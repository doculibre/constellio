package com.constellio.app.modules.rm.ui.components.menuBar;

import java.util.Map;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.BatchNavUtil;
import com.constellio.app.modules.rm.util.DecommissionNavUtil;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.utils.ReportGeneratorUtils;
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
			String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
			menuBar.setOpenDocumentButtonVisible(agentURL != null);
		} else {
			menuBar.setDownloadDocumentButtonVisible(false);
			menuBar.setOpenDocumentButtonVisible(false);
		}
		menuBar.buildMenuItems();
	}

	public void displayDocumentButtonClicked() {
		Map<String,String> params = ParamUtils.getCurrentParams();

		boolean areSearchTypeAndSearchIdPresent = DecommissionNavUtil.areTypeAndSearchIdPresent(params);
		boolean isBatchIsPresent = BatchNavUtil.isBatchIdPresent(params);

		if(areSearchTypeAndSearchIdPresent) {
			menuBar.navigate().to(RMViews.class)
					.displayDocumentFromDecommission(documentVO.getId(), DecommissionNavUtil.getHomeUri(actionsComponent.getConstellioFactories().getAppLayerFactory()),
							false, DecommissionNavUtil.getSearchId(params), DecommissionNavUtil.getSearchType(params));
		} else if (isBatchIsPresent) {
			menuBar.navigate().to(RMViews.class).displayDocumentFromBatchImport(documentVO.getId(), BatchNavUtil.getBatchId(params));

		} else {
			menuBar.navigate().to(RMViews.class).displayDocument(documentVO.getId());
		}

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

	public boolean hasMetadataReport() {
		return !ReportGeneratorUtils.getPrintableReportTemplate(presenterUtils.appLayerFactory(), presenterUtils.getCollection(),
				getDocumentVO().getSchema().getCode(), PrintableReportListPossibleType.DOCUMENT).isEmpty();
	}

}
