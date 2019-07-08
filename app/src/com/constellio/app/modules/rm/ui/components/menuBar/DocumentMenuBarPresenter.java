package com.constellio.app.modules.rm.ui.components.menuBar;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.ui.components.document.DocumentActionsPresenterUtils;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.Map;

public class DocumentMenuBarPresenter extends DocumentActionsPresenterUtils<DocumentMenuBar> {

	private DocumentMenuBar menuBar;
	private RMModuleExtensions rmModuleExtensions;
	private User user;

	public DocumentMenuBarPresenter(DocumentMenuBar menuBar) {
		super(menuBar);
		this.menuBar = menuBar;
		rmModuleExtensions = menuBar.getConstellioFactories().getAppLayerFactory().getExtensions()
				.forCollection(menuBar.getSessionContext().getCurrentCollection())
				.forModule(ConstellioRMModule.ID);
	}

	@Override
	public void setRecordVO(RecordVO recordVO) {
		super.setRecordVO(recordVO);
	}

	void lazyLoadingChildren() {
		updateActionsComponent();
	}

	@Override
	public void updateActionsComponent() {
		super.updateActionsComponent();
		menuBar.setDisplayDocumentButtonState(ComponentState.ENABLED);
		Content content = getContent();
		if (content != null) {
			ContentVersionVO contentVersionVO = contentVersionVOBuilder.build(content);
			menuBar.setContentVersionVO(contentVersionVO);
			menuBar.setDownloadDocumentButtonState(ComponentState.ENABLED);
			String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
			menuBar.setOpenDocumentButtonState(agentURL != null ? ComponentState.ENABLED : ComponentState.INVISIBLE);
		} else {
			menuBar.setDownloadDocumentButtonState(ComponentState.INVISIBLE);
			menuBar.setOpenDocumentButtonState(ComponentState.INVISIBLE);
		}
		menuBar.buildMenuItems();
	}

	public boolean hasPermissionToUserCart() {
		return getCurrentUser().has(RMPermissionsTo.USE_MY_CART).globally();
	}

	public void displayDocumentButtonClicked() {
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled() && menuBar.isInViewer()) {
			menuBar.displayInWindow();
		} else {
			Map<String, String> params = ParamUtils.getCurrentParams();

			RMNavigationUtils.navigateToDisplayDocument(documentVO.getId(),
					params, menuBar.getConstellioFactories().getAppLayerFactory(),
					menuBar.getSessionContext().getCurrentCollection());
		}
		updateSearchResultClicked();
	}

	@Override
	public void editDocumentButtonClicked() {
		if (Toggle.SEARCH_RESULTS_VIEWER.isEnabled() && menuBar.isInViewer()) {
			menuBar.editInWindow();
		} else {
			super.editDocumentButtonClicked();
		}
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
			menuBar.setRecordVO(documentVO);
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
				getRecordVO().getSchema().getCode(), PrintableReportListPossibleType.DOCUMENT).isEmpty();
	}

}
