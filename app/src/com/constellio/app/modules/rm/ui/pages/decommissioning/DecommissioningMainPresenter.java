package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningListQueryFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSearchConditionFactory;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningMainPresenter extends SingleSchemaBasePresenter<DecommissioningMainView> {
	public static final String CREATE = "create";
	public static final String GENERATED = "generated";
	public static final String PENDING_VALIDATION = "pendingValidation";
	public static final String TO_VALIDATE = "toValidate";
	public static final String VALIDATED = "validated";
	public static final String PENDING_APPROVAL = "pendingApproval";
	public static final String TO_APPROVE = "toApprove";
	public static final String APPROVED = "approved";
	public static final String PROCESSED = "processed";

	private transient RMSchemasRecordsServices rmRecordServices;

	public DecommissioningMainPresenter(DecommissioningMainView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return securityService().hasAccessToDecommissioningMainPage(user);
	}

	public boolean hasCreatePermissionOnList(RecordVO recordVO) {
		DecommissioningList decommissioningList = rmRecordServices.getDecommissioningList(recordVO.getId());
		return securityService().hasCreatePermissionOnList(getCurrentUser(), decommissioningList);
	}


	public List<String> getTabs() {
		SearchServices service = searchServices();
		List<String> result = new ArrayList<>();
		for (String tabId : securityService().getVisibleTabsInDecommissioningMainPage(getCurrentUser())) {
			if (CREATE.equals(tabId) || service.hasResults(getQueryForTab(tabId))) {
				result.add(tabId);
			}
		}
		return result;
	}

	public void tabSelected(String tabId) {
		switch (tabId) {
			case CREATE:
				view.displayListCreation();
				break;
			case GENERATED:
			case VALIDATED:
			case APPROVED:
				view.displayEditableTable(buildDataProvider(tabId));
				break;
			case PENDING_VALIDATION:
			case TO_VALIDATE:
			case PENDING_APPROVAL:
			case TO_APPROVE:
			case PROCESSED:
				view.displayReadOnlyTable(buildDataProvider(tabId));
				break;
			default:
				throw new ImpossibleRuntimeException("Unknown tabId + " + tabId);
		}
	}
	public boolean isDeletable(RecordVO recordVO) {
		return decommissioningService().isDeletable(rmRecordServices.getDecommissioningList(recordVO.getId()), getCurrentUser());
	}

	public boolean isEditable(RecordVO recordVO) {
		return decommissioningService().isEditable(rmRecordServices.getDecommissioningList(recordVO.getId()), getCurrentUser());
	}

	private DecommissioningService decommissioningService() {
		return new DecommissioningService(collection, appLayerFactory);
	}


	public List<SearchType> getCriteriaForFoldersWithoutPlanifiedDate() {
		return DecommissioningSearchConditionFactory.availableCriteriaForFoldersWithoutPlanifiedDate();
	}

	public List<SearchType> getCriteriaForFoldersWithPlanifiedDate() {
		return DecommissioningSearchConditionFactory.availableCriteriaForFoldersWithPlanifiedDate();
	}

	public boolean isDocumentDecommissioningSupported() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).areDocumentRetentionRulesEnabled();
	}

	public List<SearchType> getCriteriaForDocuments() {
		return DecommissioningSearchConditionFactory.availableCriteriaForDocuments();
	}

	public void creationRequested(SearchType type) {
		view.getUIContext().clearAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING);
		view.getUIContext().clearAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE);
		view.navigate().to(RMViews.class).decommissioningListBuilder(type.toString());
	}

	public void displayButtonClicked(RecordVO entity) {
		if (rmRecordServices().getDecommissioningList(entity.getId()).getDecommissioningListType().isFolderList()) {
			view.navigate().to(RMViews.class).displayDecommissioningList(entity.getId());
		} else {
			view.navigate().to(RMViews.class).displayDocumentDecommissioningList(entity.getId());
		}
	}

	public void editButtonClicked(RecordVO entity) {
		view.navigate().to(RMViews.class).editDecommissioningList(entity.getId());
	}

	public void deleteButtonClicked(RecordVO entity) {
		Record record = getRecord(entity.getId());
		delete(record);
		view.reloadCurrentTab();
	}

	public void backButtonClicked() {
		view.navigate().to(RMViews.class).archiveManagement();
	}

	private LogicalSearchQuery getQueryForTab(String tabId) {
		switch (tabId) {
			case GENERATED:
				if (presenterService().getCurrentUser(view.getSessionContext()).has(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST).onSomething() ||
						presenterService().getCurrentUser(view.getSessionContext()).has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething()) {
					return queryFactory().getGeneratedListsQuery(getCurrentUser());
				} else {
					return queryFactory().getGeneratedTransferListsQuery(getCurrentUser());
				}
			case PENDING_VALIDATION:
				return queryFactory().getListsPendingValidationQuery(getCurrentUser());
			case TO_VALIDATE:
				return queryFactory().getListsToValidateQuery(getCurrentUser());
			case VALIDATED:
				return queryFactory().getValidatedListsQuery(getCurrentUser());
			case PENDING_APPROVAL:
				return queryFactory().getListsPendingApprovalQuery(getCurrentUser());
			case TO_APPROVE:
				return queryFactory().getListsToApproveQuery(getCurrentUser());
			case APPROVED:
				return queryFactory().getApprovedListsQuery(getCurrentUser());
			case PROCESSED:
				return queryFactory().getProcessedListsQuery(getCurrentUser());
			default:
				throw new ImpossibleRuntimeException("Unknown tabId: " + tabId);
		}
	}

	private RecordVODataProvider buildDataProvider(final String tabId) {
		MetadataSchema schema = rmRecordServices().decommissioningList.schema();
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(schema, VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return getQueryForTab(tabId);
			}
		};
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), modelLayerFactory);
		}
		return rmRecordServices;
	}

	DecommissioningListQueryFactory queryFactory() {
		return new DecommissioningListQueryFactory(collection, appLayerFactory);
	}

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, appLayerFactory);
	}

	String getDeleteConfirmMessage(RecordVO entity) {
		String deleteConfirmMessage;
		if (rmRecordServices.getDecommissioningList(entity.getId()).isApproved()) {
			deleteConfirmMessage = $("DecommissioningMainView.deleteApprovedList");
		} else {
			deleteConfirmMessage = $("DecommissioningMainView.deleteList");
		}
		return deleteConfirmMessage;
	}

	public User getUser() {
		return getCurrentUser();
	}

	public void clearSavedSearchFromSession() {
		ConstellioUI uiContext = ConstellioUI.getCurrent();
		uiContext.clearAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING);
		uiContext.clearAttribute(DecommissioningBuilderViewImpl.DECOMMISSIONING_BUILDER_TYPE);
	}
}
