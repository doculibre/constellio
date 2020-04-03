package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class DocumentDecommissioningListPresenter extends SingleSchemaBasePresenter<DocumentDecommissioningListView> {
	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient DecommissioningService decommissioningService;
	private transient DecommissioningList decommissioningList;

	String recordId;

	public DocumentDecommissioningListPresenter(DocumentDecommissioningListView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	public DocumentDecommissioningListPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		return this;
	}

	public RecordVODataProvider getDocuments() {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				schema(Document.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				LogicalSearchCondition condition = from(rmRecordsServices().documentSchemaType())
						.where(Schemas.IDENTIFIER).isIn(decommissioningList().getDocuments());
				return new LogicalSearchQuery(condition);
			}
		};
	}

	public boolean isProcessed() {
		return decommissioningList().isProcessed();
	}

	public boolean isApproved() {
		return decommissioningList().isApproved();
	}

	public boolean isInValidation() {
		return decommissioningList().getStatus() == DecomListStatus.IN_VALIDATION;
	}

	public boolean isEditable() {
		return decommissioningService().isEditable(decommissioningList(), getCurrentUser());
	}

	public void editButtonClicked() {
		view.navigate().to(RMViews.class).editDecommissioningList(recordId);
	}

	public boolean isDeletable() {
		return decommissioningService().isDeletable(decommissioningList(), getCurrentUser());
	}

	public void deleteButtonClicked() {
		delete(decommissioningList().getWrappedRecord());
		view.navigate().to(RMViews.class).decommissioning();
	}

	public boolean isProcessableByUser() {
		return decommissioningService().isProcessable(decommissioningList(), getCurrentUser());
	}

	public boolean processPermissionOnList() {
		return securityService().hasProcessPermissionOnList(getCurrentUser(), decommissioningList());
	}

	public boolean creationPermissionOnList() {
		return securityService().hasCreatePermissionOnList(getCurrentUser(), decommissioningList());
	}

	public void processButtonClicked() {
		decommissioningService().decommission(decommissioningList(), getCurrentUser());
		view.showMessage($("DecommissioningListView.processed"));
		view.navigate().to(RMViews.class).displayDocumentDecommissioningList(recordId);
	}

	public boolean isDocumentsCertificateButtonVisible() {
		return decommissioningList().getDocumentsReportContent() != null;
	}

	public String getDocumentsReportContentId() {
		if (decommissioningList().getDocumentsReportContent() != null) {
			return decommissioningList().getDocumentsReportContent().getCurrentVersion().getHash();
		} else {
			return null;
		}
	}

	public String getDocumentsReportContentName() {
		if (decommissioningList().getDocumentsReportContent() != null) {
			return decommissioningList().getDocumentsReportContent().getCurrentVersion().getFilename();
		} else {
			return null;
		}
	}

	public void refreshList() {
		decommissioningList = null;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(params);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		DecommissioningList decommissioningList = rmRecordsServices().wrapDecommissioningList(restrictedRecord);
		return securityService().hasAccessToDecommissioningListPage(decommissioningList, user);
	}

	public RecordVO getDecommissioningList() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		}
		return decommissioningService;
	}

	RMSchemasRecordsServices rmRecordsServices() {
		if (rmRecordsServices == null) {
			rmRecordsServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordsServices;
	}

	public DecommissioningList decommissioningList() {
		if (decommissioningList == null) {
			decommissioningList = rmRecordsServices().getDecommissioningList(recordId);
		}
		return decommissioningList;
	}

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, appLayerFactory);
	}

	public void refreshView() {
		view.navigate().to(RMViews.class).displayDocumentDecommissioningList(recordId);
	}

	public void addDocumentsButtonClicked() {
		if (calculateSearchType() != null) {
			view.navigate().to(RMViews.class).editDecommissioningListBuilder(recordId, calculateSearchType().toString());
		}
	}

	public SearchType calculateSearchType() {
		if (decommissioningList().getOriginArchivisticStatus().equals(OriginStatus.ACTIVE)) {
			switch (decommissioningList().getDecommissioningListType()) {
				case FOLDERS_TO_DEPOSIT:
					return SearchType.activeToDeposit;
				case FOLDERS_TO_DESTROY:
					return SearchType.activeToDestroy;
				case FOLDERS_TO_TRANSFER:
					return SearchType.transfer;
				case DOCUMENTS_TO_DEPOSIT:
					return SearchType.documentActiveToDeposit;
				case DOCUMENTS_TO_DESTROY:
					return SearchType.documentActiveToDestroy;
				case DOCUMENTS_TO_TRANSFER:
					return SearchType.documentTransfer;
			}
		} else if (decommissioningList().getOriginArchivisticStatus().equals(OriginStatus.SEMI_ACTIVE)) {
			switch (decommissioningList().getDecommissioningListType()) {
				case FOLDERS_TO_DEPOSIT:
					return SearchType.semiActiveToDeposit;
				case FOLDERS_TO_DESTROY:
					return SearchType.semiActiveToDestroy;
				case DOCUMENTS_TO_DEPOSIT:
					return SearchType.documentSemiActiveToDeposit;
				case DOCUMENTS_TO_DESTROY:
					return SearchType.documentSemiActiveToDestroy;
			}
		}
		return null;
	}

	public void removeDocumentsButtonClicked(HashMap<Integer, Boolean> selected) {
		Set<String> idsToRemove = new HashSet<>();
		for (Map.Entry<Integer, Boolean> entry : selected.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {
				RecordVO recordVO = getDocuments().getRecordVO(entry.getKey());
				idsToRemove.add(recordVO.getId());
			}
		}
		decommissioningList().removeDocuments(idsToRemove.toArray(new String[idsToRemove.size()]));
		addOrUpdate(decommissioningList().getWrappedRecord());
		refreshView();
	}
}
