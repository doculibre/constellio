package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DisplayContainerPresenter extends BasePresenter<DisplayContainerView> implements NewReportPresenter {
	private static final long NUMBER_OF_FOLDERS_IN_CART_LIMIT = 1000;
	private static Logger LOGGER = LoggerFactory.getLogger(DisplayContainerPresenter.class);
	private transient RMSchemasRecordsServices rmRecordServices;
	private transient DecommissioningService decommissioningService;

	private String containerId;
	private String tabName;
	private String administrativeUnitId;

	public DisplayContainerPresenter(DisplayContainerView view) {
		this(view, null, false);
	}

	public DisplayContainerPresenter(DisplayContainerView view, RecordVO recordVO, boolean popup) {
		super(view);
		if (recordVO != null) {
			forParams(recordVO.getId());
		}
	}

	String getBorrowMessageState(RecordVO containerRecord) {
		String borrowedMessage = null;
		if (containerRecord != null) {
			boolean borrowed = Boolean.TRUE.equals(containerRecord.get(ContainerRecord.BORROWED));
			String borrower = containerRecord.get(ContainerRecord.BORROWER);
			if (borrowed && borrower != null) {
				String userTitle = rmRecordServices.getUser(borrower).getTitle();
				LocalDate borrowDate = containerRecord.get(ContainerRecord.BORROW_DATE);
				borrowedMessage = $("DisplayContainerView.borrowedContainer", userTitle, borrowDate);
			} else if (borrowed) {
				borrowedMessage = $("DisplayContainerView.borrowedByNullUserContainer");
			}
		}
		return borrowedMessage;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	static public boolean hasRestrictedRecordAccess(RMSchemasRecordsServices rmRecordServices, User user,
													Record restrictedRecord) {
		boolean access = false;
		ContainerRecord containerRecord = rmRecordServices.wrapContainerRecord(restrictedRecord);
		List<String> adminUnitIds = new ArrayList<>(containerRecord.getAdministrativeUnits());
		if (adminUnitIds.isEmpty() && containerRecord.getAdministrativeUnit() != null) {
			adminUnitIds.add(containerRecord.getAdministrativeUnit());
		}
		if (!adminUnitIds.isEmpty()) {
			for (String adminUnitId : adminUnitIds) {
				AdministrativeUnit adminUnit = rmRecordServices.getAdministrativeUnit(adminUnitId);
				access = user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).on(adminUnit);
				if (access) {
					break;
				}
			}
		} else {
			access = user.hasAny(RMPermissionsTo.DISPLAY_CONTAINERS, RMPermissionsTo.MANAGE_CONTAINERS).onSomething();
		}
		return access;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		return hasRestrictedRecordAccess(rmRecordServices(), user, restrictedRecord);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return asList(containerId);
	}

	public void backButtonClicked() {
		view.navigate().to().previousView();
	}

	public RecordVODataProvider getFolders() {
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(schema(Folder.DEFAULT_SCHEMA), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(schemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return getFoldersQuery();
			}
		};
	}

	public RecordVO getContainer() {
		return new RecordToVOBuilder()
				.build(recordServices().getDocumentById(containerId), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void editContainer() {
		view.navigate().to(RMViews.class).editContainer(containerId);
	}

	public ComponentState getEmptyButtonState() {
		return isContainerRecyclingAllowed() ? ComponentState.enabledIf(canEmpty()) : ComponentState.INVISIBLE;
	}

	public void emptyButtonClicked() {
		ContainerRecord container = rmRecordServices().getContainerRecord(containerId);
		try {
			decommissioningService().recycleContainer(container, getCurrentUser());
		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
		view.navigate().to(RMViews.class).displayContainer(containerId);
	}

	public void deleteButtonClicked() {
		try {
			ContainerRecord container = rmRecordServices().getContainerRecord(containerId);
			recordServices().logicallyDelete(container.getWrappedRecord(), getCurrentUser());
			view.navigate().to(CoreViews.class).home();
		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
		}
	}

	public void displayFolderButtonClicked(RecordVO folder) {
		if(view.getUIContext().getAttribute(BaseBreadcrumbTrail.SEARCH_ID) != null && containerId != null) {
			view.navigate().to(RMViews.class).displayFolderFromContainer(folder.getId(), containerId);
		} else {
			view.navigate().to(RMViews.class).displayFolder(folder.getId());
		}

	}

	@Override
	public List<ReportWithCaptionVO> getSupportedReports() {
		return asList(new ReportWithCaptionVO("Reports.ContainerRecordReport", $("Reports.ContainerRecordReport")));
	}

	@Override
	public NewReportWriterFactory getReport(String report) {
		Record record = modelLayerFactory.newRecordServices().getDocumentById(containerId);
		ContainerRecord containerRecord = new ContainerRecord(record, types());

		RMModuleExtensions rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);
		return rmModuleExtensions.getReportBuilderFactories().transferContainerRecordBuilderFactory.getValue();
	}

	@Override
	public ContainerRecordReportParameters getReportParameters(String report) {
		ContainerRecord record = rmRecordServices().getContainerRecord(containerId);

		return new ContainerRecordReportParameters(containerId, record.getDecommissioningType());
	}

	public boolean canPrintReports() {
		try {
			getReport("");
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void forParams(String containerId) {
		String[] parts = containerId.split("/");

		if(parts.length == 1) {
			this.containerId = containerId;
		} else {
			this.containerId = parts[0];
			this.tabName = parts[1];
			this.administrativeUnitId = parts[2];
		}
	}

	public String getTabName() {
		return tabName;
	}

	public String getAdministrativeUnitId() {
		return administrativeUnitId;
	}

	public String getContainerId() {
		return containerId;
	}

	public List<LabelTemplate> getCustomTemplates() {
		return appLayerFactory.getLabelTemplateManager().listExtensionTemplates(ContainerRecord.SCHEMA_TYPE);
	}

	public List<LabelTemplate> getDefaultTemplates() {
		return appLayerFactory.getLabelTemplateManager().listTemplates(ContainerRecord.SCHEMA_TYPE);
	}

	public Double getFillRatio(RecordVO container)
			throws ContainerWithoutCapacityException, RecordInContainerWithoutLinearMeasure {
		MetadataVO fillRatioMetadata = container.getMetadata(ContainerRecord.FILL_RATIO_ENTRED);
		Double fillRatioEntered = container.get(fillRatioMetadata);
		if (fillRatioEntered != null) {
			return fillRatioEntered;
		}
		MetadataVO capacityMetadata = container.getMetadata(ContainerRecord.CAPACITY);
		Double capacity = container.get(capacityMetadata);
		if (capacity == null || capacity == 0.0) {
			throw new ContainerWithoutCapacityException();
		}

		MetadataVO linearSizeMetadata = container.getMetadata(ContainerRecord.LINEAR_SIZE);
		Double linearSize = container.get(linearSizeMetadata) == null ? 0.0 : (Double) container.get(linearSizeMetadata);

		return (Double) Math.rint(100.0 * linearSize / capacity);
	}

	private LogicalSearchQuery getFoldersQuery() {
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(rmRecordServices().folder.schemaType())
				.where(rmRecordServices().folder.container()).isEqualTo(containerId)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		return new LogicalSearchQuery(condition).filteredWithUser(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES);
	}

	private boolean isContainerRecyclingAllowed() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isContainerRecyclingAllowed();
	}

	private boolean canEmpty() {
		boolean approveDecommissioningListPermission = false;
		if (getCurrentUser().has(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST).globally()) {
			approveDecommissioningListPermission = true;
		} else {
			ContainerRecord containerRecord = rmRecordServices().getContainerRecord(containerId);
			List<String> adminUnitIdsWithPermissions = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.APPROVE_DECOMMISSIONING_LIST);
			List<String> adminUnitIds = new ArrayList<>(containerRecord.getAdministrativeUnits());
			if (adminUnitIds.isEmpty() && containerRecord.getAdministrativeUnit() != null) {
				adminUnitIds.add(containerRecord.getAdministrativeUnit());
			}
			for (String adminUnitId : adminUnitIds) {
				if (adminUnitIdsWithPermissions.contains(adminUnitId)) {
					approveDecommissioningListPermission = true;
					break;
				}
			}
		}
		return approveDecommissioningListPermission && searchServices().hasResults(getFoldersQuery());
	}

	public boolean canDelete() {
		ContainerRecord containerRecord = rmRecordServices().getContainerRecord(containerId);
		List<String> adminUnitIdsWithPermissions = getConceptsWithPermissionsForCurrentUser(RMPermissionsTo.DELETE_CONTAINERS);
		List<String> adminUnitIds = new ArrayList<>(containerRecord.getAdministrativeUnits());
		if (adminUnitIds.isEmpty() && containerRecord.getAdministrativeUnit() != null) {
			adminUnitIds.add(containerRecord.getAdministrativeUnit());
		}

		if (adminUnitIdsWithPermissions.isEmpty()) {
			return false;
		}

		for (String adminUnitId : adminUnitIds) {
			if (!adminUnitIdsWithPermissions.contains(adminUnitId)) {
				return false;
			}
		}
		return !containerRecord.isLogicallyDeletedStatus();
	}

	private Double getSum(Map<String, Object> result) {
		Object sum = result.get("sum");
		return Double.valueOf(sum.toString());
	}

	private boolean includesMissing(Map<String, Object> result) {
		Object missing = result.get("missing");
		return missing != null && !missing.equals(0L);
	}

	private DecommissioningService decommissioningService() {
		if (decommissioningService == null) {
			decommissioningService = new DecommissioningService(view.getCollection(), appLayerFactory);
		}
		return decommissioningService;
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordServices;
	}

	public NewReportWriterFactory<LabelsReportParameters> getLabelsReportFactory() {
		return getRmReportBuilderFactories().labelsBuilderFactory.getValue();
	}

	public void saveIfFirstTimeReportCreated() {
		ContainerRecord containerRecord = rmRecordServices().getContainerRecord(containerId);
		ContainerRecordReportParameters reportParameters = getReportParameters(null);
		if (reportParameters.isTransfer()) {
			containerRecord.setFirstTransferReportDate(LocalDate.now());
		} else {
			containerRecord.setFirstDepositReportDate(LocalDate.now());
		}
		containerRecord.setDocumentResponsible(getCurrentUser().getId());
		try {
			recordServices().update(containerRecord);
		} catch (RecordServicesException e) {
			view.showErrorMessage("Could not update report creation time");
			e.printStackTrace();
		}
	}

	public boolean isEditButtonVisible() {
		ContainerRecord record = rmRecordServices().getContainerRecord(containerId);
		return getCurrentUser().hasWriteAccess().on(record);
	}

	public void addToDefaultFavorite() {
		if (numberOfContainersInFavoritesReachesLimit(getCurrentUser().getId())) {
			view.showMessage($("DisplayContainerViewImpl.cartCannotContainMoreThanAThousandContainers"));
		} else {
			ContainerRecord containerRecord = getContainer(containerId);
			containerRecord.addFavorite(getCurrentUser().getId());
			try {
				recordServices().update(containerRecord);
			} catch (RecordServicesException e) {
				e.printStackTrace();
			}
			view.showMessage($("DisplayContainerViewImpl.containerAddedToDefaultFavorites"));
		}
	}

	public void removeFromDefaultFavorites() {
		ContainerRecord containerRecord = getContainer(containerId);
		containerRecord.removeFavorite(getCurrentUser().getId());
		try {
			recordServices().update(containerRecord);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
		view.showMessage($("DisplayContainerViewImpl.containerRemovedFromDefaultFavorites"));
	}

	public boolean containerInDefaultFavorites() {
		ContainerRecord container = getContainer(containerId);
		return container.getFavoritesList().contains(getCurrentUser().getId());
	}

	private boolean numberOfContainersInFavoritesReachesLimit(String cartId) {
		final Metadata metadata = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getMetadata(Folder.DEFAULT_SCHEMA + "_" + Folder.FAVORITES_LIST);
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(rmRecordServices().folder.schemaType()).where(metadata).isContaining(asList(cartId)));
		return searchServices().getResultsCount(logicalSearchQuery) >= NUMBER_OF_FOLDERS_IN_CART_LIMIT;
	}

	private ContainerRecord getContainer(String containerId) {
		return rmRecordServices().getContainerRecord(containerId);
	}
}
