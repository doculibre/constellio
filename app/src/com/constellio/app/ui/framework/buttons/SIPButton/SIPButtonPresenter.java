package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.api.extensions.params.UpdateComponentExtensionParams;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class SIPButtonPresenter {

	private SIPButtonImpl button;
	private List<RecordVO> objectList;
	private Locale locale;

	public SIPButtonPresenter(SIPButtonImpl button, List<RecordVO> objectList, Locale locale) {
		this.button = button;
		this.objectList = objectList;
		this.locale = locale;

		if (button.getView() != null) {
			SessionContext sessionContext = this.button.getView().getSessionContext();
			AppLayerFactory appLayerFactory = this.button.getView().getConstellioFactories().getAppLayerFactory();
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			String username = sessionContext.getCurrentUser().getUsername();

			String collection = button.getView().getCollection();
			User user = modelLayerFactory.newUserServices().getUserInCollection(username, collection);
			if (!user.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally()) {
				button.setVisible(false);
			}
		}

		updateComponent();
	}

	private void updateComponent() {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		ConstellioHeader view = button.getView();
		SessionContext sessionContext = (view != null && view.getSessionContext() != null) ?
										view.getSessionContext() : ConstellioUI.getCurrentSessionContext();
		appLayerFactory.getExtensions().forCollection(sessionContext.getCurrentCollection())
				.updateComponent(new UpdateComponentExtensionParams(null, button));
	}

	protected List<String> getDocumentIDListFromObjectList() {
		List<String> documents = new ArrayList<>();
		for (RecordVO recordVO : this.objectList) {
			if (recordVO.getSchema().getTypeCode().equals(Document.SCHEMA_TYPE)) {
				documents.add(recordVO.getId());
			}
		}
		return documents;
	}

	protected List<String> getFolderIDListFromObjectList() {
		List<String> folders = new ArrayList<>();
		for (RecordVO recordVO : this.objectList) {
			if (recordVO.getSchema().getTypeCode().equals(Folder.SCHEMA_TYPE)) {
				folders.add(recordVO.getId());
			}
		}
		return folders;
	}

	protected boolean validateFolderHasDocument() {
		AppLayerFactory appLayerFactory = this.button.getView().getConstellioFactories().getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		String collection = button.getView().getCollection();

		MetadataSchemaType documentSchemaType = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchemaType(Document.SCHEMA_TYPE);
		for (String folderId : getFolderIDListFromObjectList()) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.from(documentSchemaType)
					.where(documentSchemaType.getDefaultSchema().get(Document.FOLDER)).isEqualTo(folderId);
			if (searchServices.getResultsCount(new LogicalSearchQuery(condition)) > 0) {
				return true;
			}
		}
		return !getDocumentIDListFromObjectList().isEmpty();
	}

	protected boolean validateBagInfoLine(BagInfoVO object) {
		for (MetadataVO field : object.getFormMetadatas()) {
			Object metadataValue = object.get(field);
			if (!"".equals(metadataValue) && metadataValue != null) {
				return true;
			}
		}
		return false;
	}

	protected void saveButtonClick(BagInfoVO viewObject) {
		boolean formIsComplete = validateBagInfoLine(viewObject);
		boolean folderHasDocument = validateFolderHasDocument();
		if (formIsComplete && validateFolderHasDocument()) {
			SessionContext sessionContext = this.button.getView().getSessionContext();
			AppLayerFactory appLayerFactory = this.button.getView().getConstellioFactories().getAppLayerFactory();
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();

			String sipFolderName = (viewObject.getArchiveTitle() != null ?
									viewObject.getArchiveTitle() :
									"archive-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
			List<String> packageInfoLines = new ArrayList<>();
			for (MetadataVO metadatavo : viewObject.getFormMetadatas()) {
				Object value = viewObject.get(metadatavo);
				if (metadatavo.getType().equals(MetadataValueType.REFERENCE)) {
					Record referencedRecord = appLayerFactory.getModelLayerFactory().newRecordServices()
							.getDocumentById(viewObject.<String>get(metadatavo));
					value = referencedRecord.getTitle();
				}
				if (value != null) {
					value = formatData(value.toString());
				}
				packageInfoLines.add(metadatavo.getLabel(this.button.getView().getSessionContext().getCurrentLocale()) + ":" + (
						value != null ?
						value :
						""));
			}
			List<String> documentList = getDocumentIDListFromObjectList();
			List<String> folderList = getFolderIDListFromObjectList();
			boolean limitSize = viewObject.isLimitSize();
			boolean deleteFiles = viewObject.isDeleteFiles();
			String warVersion = appLayerFactory.newApplicationService().getWarVersion();
			String username = sessionContext.getCurrentUser().getUsername();
			String collection = button.getView().getCollection();

			SIPBuildAsyncTask task = new SIPBuildAsyncTask(sipFolderName, packageInfoLines, documentList, folderList, limitSize,
					username, deleteFiles, warVersion, locale.getLanguage());
			AsyncTaskBatchProcess asyncTaskBatchProcess = batchProcessesManager
					.addAsyncTask(new AsyncTaskCreationRequest(task, collection, "SIPArchives").setUsername(username));
			this.button.showMessage($("SIPButton.SIPArchivesAddedToBatchProcess"));
			this.button.closeAllWindows();
			this.button.navigate().to().batchProcesses();
		} else if (folderHasDocument) {
			this.button.showErrorMessage($("SIPButton.atLeastOneBagInfoLineMustBeThere"));
		} else {
			this.button.showErrorMessage($("SIPButton.noDocumentToExport"));
		}
	}

	private String formatData(String value) {
		return value.replaceAll("(<(\\/?)p>|(&nbsp;))", "");

	}
}
