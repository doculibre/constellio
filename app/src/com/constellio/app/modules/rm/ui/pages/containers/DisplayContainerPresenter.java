package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
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
	private static Logger LOGGER = LoggerFactory.getLogger(DisplayContainerPresenter.class);
	private transient RMSchemasRecordsServices rmRecordServices;
	private transient DecommissioningService decommissioningService;

	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	private String containerId;
	private String tabName;
	private String administrativeUnitId;
	private Map<String, String> params = null;

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
			public LogicalSearchQuery getQuery() {
				return getFoldersQuery();
			}
		};
	}

	public RecordVO getContainer() {
		return new RecordToVOBuilder()
				.build(recordServices().getDocumentById(containerId), VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void displayFolderButtonClicked(RecordVO folder) {
		if (params != null && params.get(RMViews.FAV_GROUP_ID_KEY) != null) {
			view.navigate().to(RMViews.class).displayFolderFromFavorites(folder.getId(), params.get(RMViews.FAV_GROUP_ID_KEY));
		} else if (view.getUIContext().getAttribute(BaseBreadcrumbTrail.SEARCH_ID) != null && containerId != null) {
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

	public void forParams(String containerId) {
		if (containerId.contains(RMViews.FAV_GROUP_ID_KEY)) {
			this.params = ParamUtils.getParamsMap(containerId);
			this.containerId = params.get(RMViews.ID_KEY);
		} else {
			String[] parts = containerId.split("/");
			if (parts.length == 1) {
				this.containerId = containerId;
			} else {
				this.containerId = parts[0];
				this.tabName = parts[1];
				this.administrativeUnitId = parts[2];
			}
		}
	}

	public Map<String, String> getParams() {
		return params;
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
		LogicalSearchCondition condition = from(rmRecordServices().folder.schemaType())
				.where(rmRecordServices().folder.container()).isEqualTo(containerId)
				.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		return new LogicalSearchQuery(condition).filteredWithUserRead(getCurrentUser()).filteredByStatus(StatusFilter.ACTIVES);
	}

	private RMSchemasRecordsServices rmRecordServices() {
		if (rmRecordServices == null) {
			rmRecordServices = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return rmRecordServices;
	}

	public RecordVODataProvider getSharedCartsDataProvider() {
		final MetadataSchemaVO cartSchemaVO = schemaVOBuilder
				.build(rmRecordServices().cartSchema(), VIEW_MODE.TABLE, view.getSessionContext());
		return new RecordVODataProvider(cartSchemaVO, new RecordToVOBuilder(), modelLayerFactory, view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(
						from(rmRecordServices().cartSchema()).where(rmRecordServices().cartSharedWithUsers())
								.isContaining(asList(getCurrentUser().getId()))).sortAsc(Schemas.TITLE);
			}
		};
	}

	public List<Cart> getOwnedCarts() {
		return rmRecordServices().wrapCarts(searchServices().search(new LogicalSearchQuery(from(rmRecordServices().cartSchema()).where(rmRecordServices().cart.owner())
				.isEqualTo(getCurrentUser().getId())).sortAsc(Schemas.TITLE)));
	}

	public MetadataSchemaVO getSchema() {
		return new MetadataSchemaToVOBuilder().build(schema(ContainerRecord.DEFAULT_SCHEMA), RecordVO.VIEW_MODE.TABLE, view.getSessionContext());
	}

}
