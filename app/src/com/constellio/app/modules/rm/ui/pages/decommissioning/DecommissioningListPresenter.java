package com.constellio.app.modules.rm.ui.pages.decommissioning;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.DecommissioningListFolderTableExtension;
import com.constellio.app.modules.rm.extensions.api.DecommissioningListPresenterExtension;
import com.constellio.app.modules.rm.extensions.api.DecommissioningListPresenterExtension.ValidateDecommissioningListProcessableParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.enums.DecomListStatus;
import com.constellio.app.modules.rm.model.enums.OriginStatus;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListExcelReportParameters;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.*;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningServiceException.DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission;
import com.constellio.app.modules.rm.ui.builders.FolderDetailToVOBuilder;
import com.constellio.app.modules.rm.ui.builders.FolderToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.ContainerVO;
import com.constellio.app.modules.rm.ui.entities.FolderComponent;
import com.constellio.app.modules.rm.ui.entities.FolderDetailVO;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordServicesWrapperRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.util.BeanItemContainer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.*;
import static java.util.Arrays.asList;

public class DecommissioningListPresenter extends SingleSchemaBasePresenter<DecommissioningListView>
		implements NewReportPresenter {
	private final String pdfReport = "Reports.DecommissioningList";
	private final String excelReport = "Reports.DecommissioningListExcelFormat";

	private transient RMSchemasRecordsServices rmRecordsServices;
	private transient DecommissioningService decommissioningService;
	private transient DecommissioningList decommissioningList;
	private transient FolderDetailToVOBuilder folderDetailToVOBuilder;
	private DecommissioningEmailService decommissioningEmailService = new DecommissioningEmailService(collection, modelLayerFactory);
	private SearchServices searchServices;

	String recordId;

	private Set<String> missingFolders = new HashSet<>();

	private RMModuleExtensions rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);

	public DecommissioningListPresenter(DecommissioningListView view) {
		super(view, DecommissioningList.DEFAULT_SCHEMA);
		searchServices = modelLayerFactory.newSearchServices();
	}

	public DecommissioningListPresenter forRecordId(String recordId) {
		this.recordId = recordId;
		return this;
	}

	public List<String> getReports() {
		return asList(excelReport, pdfReport);
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
		DecommissioningSecurityService securityService = new DecommissioningSecurityService(restrictedRecord.getCollection(), appLayerFactory);
		RMSchemasRecordsServices rmRecordsServices = new RMSchemasRecordsServices(restrictedRecord.getCollection(), appLayerFactory);
		DecommissioningList decommissioningList = rmRecordsServices.wrapDecommissioningList(restrictedRecord);
		return securityService.hasAccessToDecommissioningListPage(decommissioningList, user);
	}

	public RecordVO getDecommissioningList() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public List<User> getAvailableManagers() throws DecommissioningEmailServiceException {
		SchemasRecordsServices schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);

		Record administrativeUnit = schemasRecordsServices.get(decommissioningList().getAdministrativeUnit());

		List<User> managerEmailForAdministrativeUnit = decommissioningEmailService.filterUserWithoutEmail(modelLayerFactory.newAuthorizationsServices()
				.getUsersWithPermissionOnRecord(
						RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST, administrativeUnit));
		List<User> managerEmailForAdministrativeUnitWithoutGlobalPermission = new ArrayList<>();
		for (User user : managerEmailForAdministrativeUnit) {
			if (!user.has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).globally()) {
				managerEmailForAdministrativeUnitWithoutGlobalPermission.add(user);
			}
		}

		List<User> managerEmailForList = decommissioningEmailService.getManagerEmailForList(decommissioningList());
		HashSet<User> uniqueUsers = new HashSet<>();
		if (managerEmailForList != null) {
			uniqueUsers.addAll(managerEmailForList);
		}
		if (managerEmailForAdministrativeUnit != null) {
			if (!managerEmailForAdministrativeUnitWithoutGlobalPermission.isEmpty()) {
				uniqueUsers.addAll(managerEmailForAdministrativeUnitWithoutGlobalPermission);
			} else {
				uniqueUsers.addAll(managerEmailForAdministrativeUnit);
			}
		}

		if (uniqueUsers.contains(getCurrentUser())) {
			uniqueUsers.remove(getCurrentUser());
		}
		return new ArrayList<>(uniqueUsers);
	}

	public List<String> getAvailableManagerIds() throws DecommissioningEmailServiceException {
		List<String> availableManagerIds = new ArrayList<>();
		for (User availableManager : getAvailableManagers()) {
			availableManagerIds.add(availableManager.getId());
		}
		return availableManagerIds;
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

	public List<String> haveCheckoutedDocument() {
		List<String> folders = decommissioningList().getFolders();

		List<Record> totalDocument = new ArrayList<>();
		List<String> checkoutedDocument = new ArrayList<>();

		for (String folder : folders) {
			LogicalSearchCondition condition = from(rmRecordsServices().documentSchemaType()).where(
					rmRecordsServices().documentSchemaType().getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FOLDER)).isEqualTo(folder);
			List<Record> listDocument = searchServices.search(new LogicalSearchQuery(condition));
			totalDocument.addAll(listDocument);
		}

		for (Record documentRecord : totalDocument) {
			Document document = rmRecordsServices.wrapDocument(documentRecord);

			if (document.getContent() != null && document.getContent().getCheckoutUserId() != null) {
				checkoutedDocument.add(document.getTitle());
			}
		}

		return checkoutedDocument;
	}

	public void processButtonClicked() {
		HashMap<String, Double> sizeToBePlacedPerContainer = new HashMap<>();
		RMSchemasRecordsServices rm = rmRecordsServices();
		for (FolderDetailVO folder : getFoldersToValidate()) {
			String newContainerRecordId = folder.getContainerRecordId();
			Double newLinearSize = folder.getLinearSize();
			if (StringUtils.isNotBlank(newContainerRecordId) && newLinearSize != null && sizeToBePlacedPerContainer
					.containsKey(newContainerRecordId)) {
				sizeToBePlacedPerContainer
						.put(newContainerRecordId, sizeToBePlacedPerContainer.get(newContainerRecordId) + newLinearSize);
			} else if (StringUtils.isNotBlank(newContainerRecordId) && newLinearSize != null && !sizeToBePlacedPerContainer
					.containsKey(newContainerRecordId)) {
				sizeToBePlacedPerContainer.put(newContainerRecordId, newLinearSize);
			}

			Folder oldFolder = rm.getFolder(folder.getFolderId());
			String oldContainerRecordId = oldFolder.getContainer();
			Double oldLinearSize = oldFolder.getLinearSize();
			if (StringUtils.isNotBlank(oldContainerRecordId) && oldLinearSize != null && sizeToBePlacedPerContainer
					.containsKey(oldContainerRecordId)) {
				sizeToBePlacedPerContainer
						.put(oldContainerRecordId, sizeToBePlacedPerContainer.get(oldContainerRecordId) - oldLinearSize);
			} else if (StringUtils.isNotBlank(oldContainerRecordId) && oldLinearSize != null && !sizeToBePlacedPerContainer
					.containsKey(oldContainerRecordId)) {
				sizeToBePlacedPerContainer.put(oldContainerRecordId, oldLinearSize);
			}
		}
		List<ContainerRecord> containersToValidate = rm
				.getContainerRecords(new ArrayList<String>(sizeToBePlacedPerContainer.keySet()));
		for (ContainerRecord container : containersToValidate) {
			if (container.getAvailableSize() != null && container.getAvailableSize() < sizeToBePlacedPerContainer.get(container.getId())) {
				view.showErrorMessage($("DecommissioningListView.notEnoughSpaceInContainer", container.getTitle()));
				return;
			}
		}

		List<String> checkoutedDocument = haveCheckoutedDocument();

		if (checkoutedDocument.size() > 0) {
			String message = $("DecommissioningListView.someDocumentsAreBorrowed");
			for (String documentTitle : checkoutedDocument) {
				message += " " + documentTitle + ",";
			}

			message.substring(0, message.length() - 1);

			view.showMessage(message);
			return;
		}

		if (!isListReadyToBeProcessed()) {
			view.showErrorMessage($("DecommissioningListView.someFoldersAreBorrowed"));
			return;
		}

		if (rmModuleExtensions != null) {
			for (DecommissioningListPresenterExtension extension : rmModuleExtensions.getDecommissioningListPresenterExtensions()) {
				ValidateDecommissioningListProcessableParams params = new ValidateDecommissioningListProcessableParams(decommissioningList());
				extension.validateProcessable(params);
				if (!params.getValidationErrors().isEmpty()) {
					view.showErrorMessage($(params.getValidationErrors().getValidationErrors().get(0)));
					return;
				}
			}
		}

		//TODO show error message if exception is thrown
		try {
			decommissioningService().decommission(decommissioningList(), getCurrentUser());
			view.showMessage($(mayContainAnalogicalMedia() ?
							   "DecommissioningListView.processedWithReminder" : "DecommissioningListView.processed"));
			view.navigate().to(RMViews.class).displayDecommissioningList(recordId);
		} catch (RecordServicesWrapperRuntimeException e) {
			RecordServicesException wrappedException = e.getWrappedException();
			if (wrappedException instanceof RecordServicesException.ValidationException) {
				view.showErrorMessage($(((RecordServicesException.ValidationException) wrappedException).getErrors()));
			} else {
				view.showErrorMessage(wrappedException.getMessage());
				e.printStackTrace();
			}
		} catch (ValidationException e) {
			view.showMessage($(e));
		} catch (Exception ex) {
			view.showErrorMessage(ex.getMessage());
			ex.printStackTrace();
		}
	}

	public boolean isListReadyToBeProcessed() {
		return !(searchServices().getResultsCount(
				from(rmRecordsServices().folder.schemaType()).where(rmRecordsServices().folder.borrowed()).isTrue()
						.andWhere(Schemas.IDENTIFIER).isIn(decommissioningList().getFolders())) > 0);
	}

	public void validateButtonClicked() {
		decommissioningList().getValidationFor(getCurrentUser().getId()).setValidationDate(TimeProvider.getLocalDate());
		addOrUpdate(decommissioningList().getWrappedRecord());
		view.navigate().to(RMViews.class).decommissioning();
	}

	public void approvalButtonClicked() {
		try {
			decommissioningService().approveList(decommissioningList(), getCurrentUser());

			// TODO: Do not hard-refresh the whole page
			view.showMessage($("DecommissioningListView.approvalClicked"));
			refreshView();
		} catch (RecordServicesWrapperRuntimeException e) {
			RecordServicesException wrappedException = e.getWrappedException();
			if (wrappedException instanceof RecordServicesException.ValidationException) {
				view.showErrorMessage($(((RecordServicesException.ValidationException) wrappedException).getErrors()));
			} else {
				view.showErrorMessage(wrappedException.getMessage());
			}
		} catch (Exception ex) {
			view.showErrorMessage(ex.getMessage());
		}
	}

	public void approvalRequestButtonClicked(List<User> managerList) {
		try {
			if (managerList.size() <= 0) {
				view.showErrorMessage($("DecommissioningListView.noManagerEmail"));
			} else {
				decommissioningService().approvalRequest(managerList, decommissioningList(), getCurrentUser());
				view.showMessage($("DecommissioningListView.approvalRequestSent"));
				refreshView();
			}
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

	public void folderPlacedInContainer(FolderDetailVO folder, ContainerVO container)
			throws Exception {
		Double containerAvailableSize = container.getAvailableSize();
		Double folderLinearSize = folder.getLinearSize();
		if (containerAvailableSize != null && folderLinearSize != null && containerAvailableSize < folderLinearSize) {
			String messageToShow = $("DecommissioningListView.containerCannotContainFolder", folderLinearSize,
					containerAvailableSize);
			view.showErrorMessage(messageToShow);
			throw new Exception(messageToShow);
		}
		folder.setContainerRecordId(container.getId());
		folder.setPackageable(false);
		DecommissioningList decommissioningList = decommissioningList();
		if (containerAvailableSize != null && folderLinearSize != null) {
			containerAvailableSize = containerAvailableSize - folderLinearSize;
		}
		DecomListContainerDetail newContainerDetail = decommissioningList.getContainerDetail(container.getId());
		if (newContainerDetail != null) {
			newContainerDetail.setAvailableSize(containerAvailableSize);
		}
		decommissioningList.getFolderDetail(folder.getFolderId()).setFolderLinearSize(folderLinearSize)
				.setContainerRecordId(container.getId()).setIsPlacedInContainer(true);
		addOrUpdate(decommissioningList.getWrappedRecord());
		view.addUpdateContainer(new ContainerVO(container.getId(), container.getCaption(), containerAvailableSize, container.getAdministrativeUnits()),
				newContainerDetail);

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
				try {
					FolderDetailVO folderDetailVO = builder.build(folder, FolderComponent.FOLDERS_TO_VALIDATE_COMPONENT);
					result.add(folderDetailVO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					missingFolders.add(folder.getFolderId());
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public List<FolderDetailVO> getPackageableFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && !decommissioningService().isFolderProcessable(decommissioningList(), folder)
				&& !isFolderPlacedInContainer(folder)) {
				try {
					FolderDetailVO folderVO = builder.build(folder, FolderComponent.PACKAGEABLE_FOLDER_COMPONENT);
					addOtherMetadatasToFolderDetailVO(folderVO);
					result.add(folderVO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					missingFolders.add(folder.getFolderId());
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private void addOtherMetadatasToFolderDetailVO(FolderDetailVO folderVO) {
		DecommissioningListFolderTableExtension folderDetailTableExtension = getFolderDetailTableExtension();
		if (folderDetailTableExtension != null) {
			folderDetailTableExtension.addPreviousIdToFolderVO(folderVO);
		}
	}

	public List<FolderDetailVO> getProcessableFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && (decommissioningService().isFolderProcessable(decommissioningList(), folder)
										|| isFolderPlacedInContainer(folder))) {
				try {
					FolderDetailVO folderDetailVO = builder.build(folder, FolderComponent.PROCESSABLE_FOLDER_COMPONENT);
					result.add(folderDetailVO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					missingFolders.add(folder.getFolderId());
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public List<FolderDetailVO> getSelectedFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isSelected()) {
				try {
					FolderDetailVO folderDetailVO = builder.build(folder, FolderComponent.SELECTED_FOLDERS_COMPONENT);
					result.add(folderDetailVO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					missingFolders.add(folder.getFolderId());
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	protected boolean isFolderPlacedInContainer(FolderDetailWithType folder) {
		return folder.getDetail().isPlacedInContainer();
	}

	public List<FolderDetailVO> getExcludedFolders() {
		FolderDetailToVOBuilder builder = folderDetailToVOBuilder();
		List<FolderDetailVO> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isExcluded()) {
				try {
					FolderDetailVO folderDetailVO = builder.build(folder, FolderComponent.EXCLUDED_FOLDER_COMPONENT);
					result.add(folderDetailVO);
				} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
					missingFolders.add(folder.getFolderId());
					e.printStackTrace();
				}
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
			if (folder.isIncluded() && !decommissioningService().isFolderProcessable(decommissioningList(), folder) && !folder
					.getDetail().isPlacedInContainer()) {
				result.add(folder.getFolderId());
			}
		}
		return result;
	}

	public List<String> getProcessableFoldersIds() {
		List<String> result = new ArrayList<>();
		for (FolderDetailWithType folder : decommissioningList().getFolderDetailsWithType()) {
			if (folder.isIncluded() && (decommissioningService().isFolderProcessable(decommissioningList(), folder) || folder
					.getDetail().isPlacedInContainer())) {
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
		DecommissioningList decommissioningList = decommissioningList();
		return decommissioningList.getContainerDetails();
	}

	public DecomListContainerDetail getContainerDetail(String containerId) {
		DecommissioningList decommissioningList = decommissioningList();
		return decommissioningList.getContainerDetail(containerId);
	}

	public List<ContainerVO> getContainers() {
		List<ContainerVO> result = new ArrayList<>();
		DecommissioningList decommissioningList = decommissioningList();
		if (decommissioningList.getContainers().isEmpty()) {
			return result;
		}

		Map<String, Double> usedSpaceMap = new HashMap<>();
		for (FolderDetailVO folderDetailVO : getProcessableFolders()) {
			String containerRecordId = folderDetailVO.getContainerRecordId();
			Double linearSize = folderDetailVO.getLinearSize();
			if (containerRecordId != null && linearSize != null) {
				Double oldSize = usedSpaceMap.get(containerRecordId);
				oldSize = oldSize == null ? 0.0 : oldSize;
				usedSpaceMap.put(containerRecordId, oldSize + linearSize);
			}
		}
		for (ContainerRecord container : rmRecordsServices.wrapContainerRecords(
				recordServices().getRecordsById(view.getCollection(), decommissioningList.getContainers()))) {
			ContainerVO containerVO;
			if (container.getAvailableSize() == null) {
				containerVO = new ContainerVO(container.getId(), container.getTitle(), null, container.getAdministrativeUnits());
			} else {
				Double usedSpace = usedSpaceMap.get(container.getId());
				usedSpace = usedSpace == null ? 0.0 : usedSpace;
				containerVO = new ContainerVO(container.getId(), container.getTitle(), container.getAvailableSize() - usedSpace, container.getAdministrativeUnits());
			}
			decommissioningList.getContainerDetail(containerVO.getId()).setAvailableSize(containerVO.getAvailableSize());
			result.add(containerVO);
		}
		//		List<DecomListContainerDetail> containerDetails = decommissioningList.getContainerDetails();
		//		decommissioningList.setContainerDetails(containerDetails);
		addOrUpdate(decommissioningList.getWrappedRecord(), RecordUpdateOptions.validationExceptionSafeOptions());
		this.decommissioningList = decommissioningList;
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

	public boolean shouldDisplayOrder() {
		return true;
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

	public void setValidationStatusAndRefreshView(FolderDetailVO folder, Boolean valid) {
		decommissioningList().getFolderDetail(folder.getFolderId()).setFolderExcluded(Boolean.FALSE.equals(valid));
		addOrUpdate(decommissioningList().getWrappedRecord());
		// TODO: Do not hard-refresh the whole page
		refreshView();
	}

	public void setValidationStatusForSelectedFoldersAndRefreshView(List<FolderDetailVO> folders, boolean valid) {
		for (FolderDetailVO folder : folders) {
			if (folder.isSelected()) {
				decommissioningList().getFolderDetail(folder.getFolderId()).setFolderExcluded(Boolean.FALSE.equals(valid));
			}
		}
		addOrUpdate(decommissioningList().getWrappedRecord());
		// TODO: Do not hard-refresh the whole page
		refreshView();
	}

	public boolean validationRequested(List<String> users, String comments, boolean saveComment) {
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
		decommissioningService().sendValidationRequest(decommissioningList(), getCurrentUser(), users, comments, saveComment);
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

	public boolean canUserSendValidationRequest() {
		return decommissioningService().isValidationRequestPossible(decommissioningList(), getCurrentUser());
	}

	public boolean hasProcessPermissionOnList() {
		return securityService().hasProcessPermissionOnList(getCurrentUser(), decommissioningList());
	}

	public boolean hasCreatePermissionOnList() {
		return securityService().hasCreatePermissionOnList(getCurrentUser(), decommissioningList());
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

	public boolean isInApprobation() {
		return decommissioningList().getStatus() == DecomListStatus.IN_APPROVAL;
	}

	public boolean isGenerated() {
		return decommissioningList.getStatus() == DecomListStatus.GENERATED;
	}

	public boolean isValidationRequestedForCurrentUser() {
		return decommissioningService().isValidationRequestedFor(decommissioningList(), getCurrentUser());
	}

	@Override
	public List<ReportWithCaptionVO> getSupportedReports() {
		List<ReportWithCaptionVO> supportedReports = new ArrayList<>();
		ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
		List<String> userReports = reportServices.getUserReportTitles(getCurrentUser(), Folder.SCHEMA_TYPE);
		if (userReports != null) {
			for (String reportTitle : userReports) {
				supportedReports.add(new ReportWithCaptionVO(reportTitle, reportTitle));
			}
		}
		return supportedReports;
	}

	@Override
	public NewReportWriterFactory getReport(String report) {

		if (report.equals(pdfReport)) {
			return getRmReportBuilderFactories().decommissioningListBuilderFactory.getValue();
		} else {
			return getRmReportBuilderFactories().decommissioningListExcelBuilderFactory.getValue();
		}
	}

	@Override
	public Object getReportParameters(String report) {
		if (report.equals(pdfReport)) {
			return new DecommissioningListReportParameters(decommissioningList.getId());
		} else {
			return new DecommissioningListExcelReportParameters(decommissioningList.getId(),
					Folder.SCHEMA_TYPE, collection, report, getCurrentUser());
		}
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
		if (calculateSearchType() != null) {
			view.navigate().to(RMViews.class).editDecommissioningListBuilder(recordId, calculateSearchType().toString());
		}
	}

	public SearchType calculateSearchType() {
		if (OriginStatus.ACTIVE.equals(decommissioningList().getOriginArchivisticStatus())) {
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
		} else if (OriginStatus.SEMI_ACTIVE.equals(decommissioningList().getOriginArchivisticStatus())) {
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
		for (FolderDetailVO folderDetailVO : selected) {
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
		if (modelLayerFactory.newSearchServices().getResultsCount(query) == 0) {
			view.showErrorMessage($("DecommissioningListView.noContainerFound"));
			return;
		}
		List<ContainerRecord> containerRecordList = rm.wrapContainerRecords(modelLayerFactory.newSearchServices().search(query));
		List<Map.Entry<String, Double>> listOfEntry = new LinkedList<>(linearSize.entrySet());
		for (Map.Entry<String, Double> entry : listOfEntry) {
			while (!containerRecordList.isEmpty() && !fill(containerRecordList.get(0), entry)) {
				containerRecordList.remove(0);
			}
		}
	}

	protected boolean fill(ContainerRecord containerRecord, Map.Entry<String, Double> entry) {
		recordServices().recalculate(containerRecord);
		try {
			folderPlacedInContainer(view.getPackageableFolder(entry.getKey()), view.getContainer(containerRecord));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public LogicalSearchQuery buildContainerQuery(Double minimumSize) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		return new LogicalSearchQuery(from(rm.containerRecord.schemaType()).whereAllConditions(
				where(rm.containerRecord.administrativeUnits()).isContaining(asList(decommissioningList().getAdministrativeUnit())),
				where(rm.containerRecord.availableSize()).isGreaterOrEqualThan(minimumSize),
				anyConditions(
						where(rm.containerRecord.decommissioningType())
								.isEqualTo(decommissioningList().getDecommissioningListType().getDecommissioningType()),
						where(rm.containerRecord.decommissioningType()).isNull()
				)
		)).sortAsc(rm.containerRecord.availableSize());
	}

	private Map<String, Double> sortByValue(Map<String, Double> linearSize) {
		List<Map.Entry<String, Double>> listOfEntry = new LinkedList<>(linearSize.entrySet());
		Collections.sort(listOfEntry, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}
		});

		Map<String, Double> result = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry : listOfEntry) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public void addContainerToDecommissioningList(ContainerVO containerVO) {
		try {
			recordServices().update(decommissioningList()
					.addContainerDetailsFrom(asList(rmRecordsServices.getContainerRecord(containerVO.getId()))));
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
		orderNumber = orderNumber == -1 ? getProcessableFoldersIds().indexOf(folderId) : orderNumber;
		orderNumber = orderNumber == -1 ? getFoldersToValidateIds().indexOf(folderId) : orderNumber;
		return String.valueOf(orderNumber + 1);
	}

	public boolean canCurrentUserManageContainers() {
		return presenterService().getCurrentUser(view.getSessionContext()).has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething();
	}

	public void removeFromContainer(FolderDetailVO detail) {
		DecommissioningList decommissioningList = decommissioningList();
		FolderDetailWithType folderDetailWithType = decommissioningList.getFolderDetailWithType(detail.getFolderId());
		folderDetailWithType.getDetail().setIsPlacedInContainer(false);
		try {
			recordServices().update(decommissioningList);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
		detail.setPackageable(decommissioningService().isFolderRemovableFromContainer(decommissioningList,
				decommissioningList.getFolderDetailWithType(detail.getFolderId())));
	}

	public DecommissioningListFolderTableExtension getFolderDetailTableExtension() {
		return rmModuleExtensions.getDecommissioningListFolderTableExtension();
	}

	public boolean areContainersHidden() {
		return decommissioningList().getDecommissioningListType().isDestroyal() && getFolderDetailTableExtension() != null;
	}

	public List<FolderVO> getFoldersVO() {
		List<Folder> foldersIds = rmRecordsServices().getFolders(decommissioningList().getFolders());
		List<FolderVO> folderVOList = new ArrayList<>();
		FolderToVOBuilder builder = new FolderToVOBuilder();
		for (Folder folder : foldersIds) {
			folderVOList.add(builder.build(folder.getWrappedRecord(), VIEW_MODE.TABLE, view.getSessionContext()));
		}
		return folderVOList;
	}

	public Set<String> getMissingFolders() {
		return missingFolders;
	}

	public boolean hasAccessToSIPGeneration() {
		return getCurrentUser().has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally();
	}

	public void denyApproval(String commentString) {
		if (commentString != null) {
			Comment comment = new Comment();
			comment.setMessage(commentString);
			comment.setUser(getCurrentUser());
			comment.setDateTime(LocalDateTime.now());

			List<Comment> comments = new ArrayList<>(decommissioningList().getComments());
			comments.add(comment);
			decommissioningList().setComments(comments);
		}

		try {
			decommissioningService().denyApprovalOnList(decommissioningList(), getCurrentUser(), commentString);
		} catch (DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission e) {
			view.showMessage($("DecommissioningListView.tooMuchOptimisticLocking"));
		} catch (DecommissioningServiceException e) {
			view.showMessage($(e));
		}
	}

	public void showErrorMessage(String message) {
		view.showErrorMessage(message);
	}

	public List<ContainerVO> removeContainersCurrentUserCannotManage(List<ContainerVO> filteredContainers) {
		if (filteredContainers != null) {
			Iterator<ContainerVO> iterator = filteredContainers.iterator();
			while (iterator.hasNext()) {
				ContainerVO currentContainer = iterator.next();

				if (!hasUserPermissionToManageContainersOn(currentContainer)) {
					iterator.remove();
				}
			}
		}
		return filteredContainers;
	}

	public boolean hasUserPermissionToManageContainersOn(ContainerVO currentContainer) {
		boolean hasPermissionToManageContainers = getCurrentUser().has(RMPermissionsTo.MANAGE_CONTAINERS).globally();

		List<String> administrativeUnitIds = currentContainer.getAdministrativeUnits();
		if (administrativeUnitIds != null && !hasPermissionToManageContainers) {
			List<AdministrativeUnit> administrativeUnits = rmRecordsServices.getAdministrativeUnits(administrativeUnitIds);
			for (AdministrativeUnit unit : administrativeUnits) {
				hasPermissionToManageContainers |= getCurrentUser().has(RMPermissionsTo.MANAGE_CONTAINERS).on(unit);
			}
		}
		return hasPermissionToManageContainers;
	}

	public List<String> getUsersWithReadPermissionOnAdministrativeUnit() {
		Record administrativeUnit = getRecord(decommissioningList().getAdministrativeUnit());
		if (administrativeUnit != null) {
			return modelLayerFactory.newAuthorizationsServices().getUsersIdsWithRoleForRecord(Role.READ, administrativeUnit);
		} else {
			return modelLayerFactory.newAuthorizationsServices().getUsersIdsWithGlobalReadRightInCollection(collection);
		}
	}

	public String getUsername(String userId) {
		Record record = recordServices().getDocumentById(userId);
		return rmRecordsServices.wrapUser(record).getUsername();
	}
}
