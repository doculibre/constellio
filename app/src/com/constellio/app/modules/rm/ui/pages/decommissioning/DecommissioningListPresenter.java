package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningEmailServiceException;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningSecurityService;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static java.util.Arrays.asList;

public class DecommissioningListPresenter extends SingleSchemaBasePresenter<DecommissioningListView>
		implements NewReportPresenter {
	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient DecommissioningService decommissioningService;
	private transient DecommissioningList decommissioningList;
	private transient FolderDetailToVOBuilder folderDetailToVOBuilder;

	String recordId;

	public DecommissioningListPresenter(DecommissioningListView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
	}

	public DecommissioningListPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		return this;
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

	public boolean isProcessable() {
		return decommissioningService().isProcessable(decommissioningList(), getCurrentUser());
	}

	public void processButtonClicked() {
		decommissioningService().decommission(decommissioningList(), getCurrentUser());
		view.showMessage($(mayContainAnalogicalMedia() ?
				"DecommissioningListView.processedWithReminder" : "DecommissioningListView.processed"));
		view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
	}

	public void validateButtonClicked() {
		decommissioningList().getValidationFor(getCurrentUser().getId()).setValidationDate(TimeProvider.getLocalDate());
		addOrUpdate(decommissioningList().getWrappedRecord());
		view.navigate().to(RMViews.class).decommissioning();
	}

	public void approvalButtonClicked() {
		decommissioningService().approveList(decommissioningList(), getCurrentUser());
		// TODO: Do not hard-refresh the whole page
		view.showMessage($("DecommissioningListView.approvalClicked"));
		refreshView();
	}

	public void approvalRequestButtonClicked() {
		try {
			decommissioningService().approvalRequest(decommissioningList(), getCurrentUser());
			view.showMessage($("DecommissioningListView.approvalRequestSent"));
			refreshView();
		} catch (DecommissioningEmailServiceException e) {
			view.showErrorMessage($("DecommissioningListView.noManagerEmail"));
		} catch (RecordServicesException e) {
			view.showErrorMessage($("DecommissioningListView.approvalRequestNotSentCorrectly"));
		}
	}

	public boolean isProcessed() {
		return decommissioningList().isProcessed();
	}

	public boolean isApproved() {
		return decommissioningList().isApproved();
	}

	public void containerCreationRequested() {
		view.navigate().to(RMViews.class).createContainerForDecommissioningList(recordId);
	}

	public void containerSearchRequested() {
		view.navigate().to(RMViews.class).searchContainerForDecommissioningList(recordId);
	}

	public void folderPlacedInContainer(FolderDetailVO folder, ContainerVO container) {
		folder.setContainerRecordId(container.getId());
		decommissioningList().getFolderDetail(folder.getFolderId()).setFolderLinearSize(folder.getLinearSize()).setContainerRecordId(container.getId());
		addOrUpdate(decommissioningList().getWrappedRecord());

		view.setProcessable(folder);
		view.updateProcessButtonState(isProcessable());
	}

	public void folderSorted(FolderDetailVO folderVO, boolean value) {
		folderVO.setReversedSort(value);
		FolderDetailWithType folder = decommissioningList().getFolderDetailWithType(folderVO.getFolderId());
		folder.getDetail().setReversedSort(value);
		addOrUpdate(decommissioningList().getWrappedRecord());

		if (decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
			folderVO.setPackageable(false);
			view.setProcessable(folderVO);
		} else {
			folderVO.setPackageable(true);
			view.setPackageable(folderVO);
		}
		view.updateProcessButtonState(isProcessable());
	}

	public void linearSizeUpdated(FolderDetailVO folder, Double linearSize) {
		folder.setLinearSize(linearSize);
		decommissioningList().getFolderDetail(folder.getFolderId()).setFolderLinearSize(linearSize);
		addOrUpdate(decommissioningList().getWrappedRecord());
	}

	public Double getLinearSize(FolderDetailVO folder) {
		return decommissioningList().getFolderDetail(folder.getFolderId()).getFolderLinearSize();
	}

	public void validationRemoved(DecomListValidation validation) {
		addOrUpdate(decommissioningList().removeValidationRequest(validation).getWrappedRecord());
		// TODO: Do not hard-refresh the whole page
		view.showMessage($("DecommissioningListView.validatorRemoved"));
		view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
	}

	public void containerStatusChanged(DecomListContainerDetail detail, boolean full) {
		detail.setFull(full);
		addOrUpdate(decommissioningList().getWrappedRecord());
	}

	public List<DecomListValidation> getValidations() {
		return decommissioningList().getValidations();
	}

	public List<FolderDetailVO> getFoldersToValidate() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded()) {
				result.add(builder.build(folder));
			}
		}
		return result;
	}

	public List<FolderDetailVO> getPackageableFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && !decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
				result.add(builder.build(folder));
			}
		}
		return result;
	}

	public List<FolderDetailVO> getProcessableFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
				result.add(builder.build(folder));
			}
		}
		return result;
	}

	public List<FolderDetailVO> getExcludedFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isExcluded()) {
				result.add(builder.build(folder));
			}
		}
		return result;
	}

	public List<String> getFoldersToValidateIds() {
		List<String> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded()) {
				result.add(folder.getFolderId());
			}
		}
		return result;
	}

	public List<String> getPackageableFoldersIds() {
		List<String> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && !decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
				result.add(folder.getFolderId());
			}
		}
		return result;
	}

	public List<String> getProcessableFoldersIds() {
		List<String> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && decommissioningService().isFolderProcessable(decommissioningList(), folder)) {
				result.add(folder.getFolderId());
			}
		}
		return result;
	}

	public List<String> getExcludedFoldersIds() {
		List<String> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isExcluded()) {
				result.add(folder.getFolderId());
			}
		}
		return result;
	}

	public List<DecomListContainerDetail> getContainerDetails() {
		return decommissioningList().getContainerDetails();
	}

	public List<ContainerVO> getContainers() {
		List<ContainerVO> result = new ArrayList<>();
		if (decommissioningList().getContainers().isEmpty()) {
			return result;
		}
		for (Record record : recordServices().getRecordsById(view.getCollection(), decommissioningList().getContainers())) {
			ContainerVO containerVO = new ContainerVO(record.getId(), (String) record.get(Schemas.TITLE));
			result.add(containerVO);
		}
		return result;
	}

	public String getSortAction() {
		switch (decommissioningList().getDecommissioningListType()) {
		case FOLDERS_TO_DEPOSIT:
			return "destroy";
		case FOLDERS_TO_DESTROY:
			return "deposit";
		}
		return null;
	}

	public boolean shouldAllowContainerEditing() {
		return decommissioningService().canEditContainers(decommissioningList(), getCurrentUser());
	}

	public boolean shouldDisplayRetentionRuleInDetails() {
		return StringUtils.isBlank(decommissioningList().getUniformRule());
	}

	public boolean shouldDisplayCategoryInDetails() {
		return StringUtils.isBlank(decommissioningList().getUniformCategory());
	}

	public boolean shouldDisplaySort() {
		return decommissioningService().isSortable(decommissioningList());
	}

	public boolean shouldDisplayValidation() {
		return !(decommissioningList().isApproved() || decommissioningList().isProcessed());
	}

	private boolean mayContainAnalogicalMedia() {
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.getType().potentiallyHasAnalogMedium()) {
				return true;
			}
		}
		return false;
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

	FolderDetailToVOBuilder folderDetailToVOBuilder() {
		if (folderDetailToVOBuilder == null) {
			folderDetailToVOBuilder = new FolderDetailToVOBuilder(rmRecordsServices());
		}
		return folderDetailToVOBuilder;
	}

	public DecommissioningList decommissioningList() {
		if (decommissioningList == null) {
			decommissioningList = rmRecordsServices().getDecommissioningList(recordId);
		}
		return decommissioningList;
	}

	public void setValidationStatus(FolderDetailVO folder, Boolean valid) {
		decommissioningList().getFolderDetail(folder.getFolderId()).setFolderExcluded(Boolean.FALSE.equals(valid));
		addOrUpdate(decommissioningList().getWrappedRecord());
		// TODO: Do not hard-refresh the whole page
		refreshView();
	}

	public boolean validationRequested(List<String> users, String comments) {
		if (users.contains(getCurrentUser().getId())) {
			view.showErrorMessage($("DecommissioningListView.cannotSendValidationToItself"));
			return false;
		}
		// TODO: Restore this functionality
		//		List<String> existingValidators = intersection(decommissioningList().getValidationRequests(), users);
		//		if (!existingValidators.isEmpty()) {
		//			view.showErrorMessage($("DecommissioningListView.validationAlreadySentToUsers") + " " +
		//					join(getUsersNames(existingValidators), ", "));
		//			return false;
		//		}
		decommissioningService().sendValidationRequest(decommissioningList(), getCurrentUser(), users, comments);
		view.showMessage($("DecommissioningListView.validationMessageSent"));
		refreshView();
		return true;
	}

	private DecommissioningSecurityService securityService() {
		return new DecommissioningSecurityService(collection, appLayerFactory);
	}

	public boolean canValidate() {
		return decommissioningService().isValidationPossible(decommissioningList(), getCurrentUser());
	}

	public boolean canSendValidationRequest() {
		return decommissioningService().isValidationRequestPossible(decommissioningList(), getCurrentUser());
	}

	public boolean canApprove() {
		return decommissioningService().isApprovalPossible(decommissioningList(), getCurrentUser());
	}

	public boolean canSendApprovalRequest() {
		return decommissioningService().isApprovalRequestPossible(decommissioningList(), getCurrentUser());
	}

	public void refreshView() {
		view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
	}

	public boolean canRemoveValidationRequest() {
		return securityService().canAskValidation(decommissioningList(), getCurrentUser());
	}

	public void refreshList() {
		decommissioningList = null;
	}

	public boolean isInValidation() {
		return decommissioningList().getStatus() == DecomListStatus.IN_VALIDATION;
	}

	public boolean isValidationRequestedForCurrentUser() {
		return decommissioningService().isValidationRequestedFor(decommissioningList(), getCurrentUser());
	}

	@Override
	public List<String> getSupportedReports() {
		return asList($("Reports.DecommissioningList"));
	}

	@Override
	public NewReportWriterFactory getReport(String report) {

		if (report.equals("Reports.DecommissioningList")) {
			return getRmReportBuilderFactories().decommissioningListBuilderFactory.getValue();
		} else {//Reports.documentsCertificate //Reports.foldersCertificate
			throw new RuntimeException("BUG: Unknown report: " + report);
		}
	}

	@Override
	public Object getReportParameters(String report) {
		return new DecommissioningListReportParameters(decommissioningList.getId());
	}

	public boolean isDocumentsCertificateButtonVisible() {
		return decommissioningList().getDocumentsReportContent() != null;
	}

	public boolean isFoldersCertificateButtonVisible() {
		return decommissioningList().getFoldersReportContent() != null;
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

	public String getFoldersReportContentId() {
		if (decommissioningList().getFoldersReportContent() != null) {
			return decommissioningList().getFoldersReportContent().getCurrentVersion().getHash();
		} else {
			return null;
		}
	}

	public String getFoldersReportContentName() {
		if (decommissioningList().getFoldersReportContent() != null) {
			return decommissioningList().getFoldersReportContent().getCurrentVersion().getFilename();
		} else {
			return null;
		}
	}

	public String getDeleteConfirmMessage() {
		String deleteConfirmMessage;
		if (decommissioningList().isApproved()) {
			deleteConfirmMessage = $("DecommissioningListView.deleteApprovedList");
		} else {
			deleteConfirmMessage = $("DecommissioningListView.deleteList");
		}
		return deleteConfirmMessage;
	}

	public void addFoldersButtonClicked() {
		if(calculateSearchType() != null) {
			view.navigate().to(RMViews.class).editDecommissioningListBuilder(recordId, calculateSearchType().toString());
		}
	}

	public SearchType calculateSearchType() {
		if(decommissioningList().getOriginArchivisticStatus().equals(OriginStatus.ACTIVE)) {
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
		} else if(decommissioningList().getOriginArchivisticStatus().equals(OriginStatus.SEMI_ACTIVE)) {
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

	public void removeFoldersButtonClicked(List<FolderDetailVO> selected) {
		for(FolderDetailVO folderDetailVO: selected) {
			decommissioningList().removeFolderDetail(folderDetailVO.getFolderId());
		}
		addOrUpdate(decommissioningList().getWrappedRecord());
		refreshView();
	}

	public void autoFillContainersRequested(Map<String, Double> linearSize) {
		linearSize = sortByValue(linearSize);
		LogicalSearchQuery query = buildContainerQuery(linearSize.values().iterator().next());
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		//TODO CAN BE NULL
		if(modelLayerFactory.newSearchServices().getResultsCount(query) == 0) {
			view.showErrorMessage($("DecommissioningListView.noContainerFound"));
			return;
		}
		List<ContainerRecord> containerRecordList = rm.wrapContainerRecords(modelLayerFactory.newSearchServices().search(query));
		List<Map.Entry<String, Double>> listOfEntry = new LinkedList<>(linearSize.entrySet());
		for(Map.Entry<String, Double> entry: listOfEntry) {
			while(!containerRecordList.isEmpty() && !fill(containerRecordList.get(0), entry)) {
				containerRecordList.remove(0);
			}
		}
	}

	private boolean fill(ContainerRecord containerRecord, Map.Entry<String, Double> entry) {
		recordServices().recalculate(containerRecord);
		if(containerRecord.getAvailableSize() >= entry.getValue()) {
			try {
				recordServices().update(rmRecordsServices().getFolder(entry.getKey()).setContainer(containerRecord).setLinearSize(entry.getValue()));
				folderPlacedInContainer(view.getPackageableFolder(entry.getKey()), view.getContainer(containerRecord));
				return true;
			} catch (RecordServicesException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public LogicalSearchQuery buildContainerQuery(Double minimumSize) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return new LogicalSearchQuery(from(rm.containerRecord.schemaType()).whereAllConditions(
				where(rm.containerRecord.administrativeUnit()).isEqualTo(decommissioningList().getAdministrativeUnit()),
				where(rm.containerRecord.availableSize()).isGreaterOrEqualThan(minimumSize),
				anyConditions(
						where(rm.containerRecord.decommissioningType()).isEqualTo(decommissioningList().getDecommissioningListType().getDecommissioningType()),
						where(rm.containerRecord.decommissioningType()).isNull()
				)
		)).sortAsc(rm.containerRecord.availableSize());
	}

	private Map<String, Double> sortByValue(Map<String, Double> linearSize) {
		List<Map.Entry<String, Double>> listOfEntry = new LinkedList<>( linearSize.entrySet() );
		Collections.sort(listOfEntry, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}
		});

		Map<String, Double> result = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry : listOfEntry)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public void addContainerToDecommissioningList(ContainerRecord containerRecord) {
		try {
			recordServices().update(decommissioningList().addContainerDetailsFrom(asList(containerRecord)));
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	public void reorderRequested(OrderDecommissioningListPresenter.TableType type) {
		view.navigate().to(RMViews.class).orderDecommissioningList(decommissioningList().getId(), type);
	}

	public String getOrderNumber(String folderId) {
		int orderNumber = getExcludedFoldersIds().indexOf(folderId);
		orderNumber = orderNumber == -1 ? getPackageableFoldersIds().indexOf(folderId) : orderNumber;
		orderNumber = orderNumber == -1? getProcessableFoldersIds().indexOf(folderId) : orderNumber;
		orderNumber = orderNumber == -1? getFoldersToValidateIds().indexOf(folderId) : orderNumber;
		return String.valueOf(orderNumber + 1);
	}

	public boolean canCurrentUserManageStorageSpaces() {
		return presenterService().getCurrentUser(view.getSessionContext()).has(RMPermissionsTo.MANAGE_CONTAINERS).globally();
	}
}
