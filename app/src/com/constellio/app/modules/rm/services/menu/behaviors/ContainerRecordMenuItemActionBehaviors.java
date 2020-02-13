package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.SendReturnReminderEmailButton;
import com.constellio.app.modules.rm.services.menu.behaviors.util.BehaviorsUtil;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton.AddedRecordType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class ContainerRecordMenuItemActionBehaviors {
	private RMModuleExtensions rmModuleExtensions;
	private ModelLayerCollectionExtensions extensions;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private LoggingServices loggingServices;
	private SearchServices searchServices;
	private DecommissioningService decommissioningService;
	private BorrowingServices borrowingServices;


	public ContainerRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.extensions = modelLayerFactory.getExtensions().forCollection(collection);
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
		this.borrowingServices = new BorrowingServices(collection, modelLayerFactory);
	}

	public void getConsultationLink(ContainerRecord containerRecord, MenuItemActionBehaviorParams params) {
		String constellioURL = getConstellioUrl(modelLayerFactory);

		CopyToClipBoard.copyToClipBoard(constellioURL + RMUrlUtil.getPathToConsultLinkForContainerRecord(containerRecord.getId()));
	}

	public void consult(ContainerRecord container, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to(RMViews.class).displayContainer(container.getId());
	}

	public void edit(ContainerRecord container, MenuItemActionBehaviorParams params) {
		params.getFormParams();
		if (getFavoriteGroupId(params) != null) {
			params.getView().navigate().to(RMViews.class).editContainerFromFavorites(container.getId(), getFavoriteGroupId(params));
		} else {
			params.getView().navigate().to(RMViews.class).editContainer(container.getId());
		}
	}

	public void report(ContainerRecord container, MenuItemActionBehaviorParams params) {
		ReportButton reportButton = new ReportButton(new ReportWithCaptionVO("Reports.ContainerRecordReport",
				$("Reports.ContainerRecordReport")),
				new ContainerReportPresenter(rm.getContainerRecord(container.getId()))) {
			@Override
			protected Component buildWindowContent() {
				saveIfFirstTimeReportCreated(container.getId(), params.getUser(), params.getView());
				return super.buildWindowContent();
			}
		};

		reportButton.click();
	}

	public void printLabel(ContainerRecord container, MenuItemActionBehaviorParams params) {
		Factory<List<LabelTemplate>> customLabelTemplatesFactory = (Factory<List<LabelTemplate>>)
				() -> appLayerFactory.getLabelTemplateManager().listExtensionTemplates(ContainerRecord.SCHEMA_TYPE);

		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = (Factory<List<LabelTemplate>>)
				() -> appLayerFactory.getLabelTemplateManager().listTemplates(ContainerRecord.SCHEMA_TYPE);

		SessionContext sessionContext = params.getView().getSessionContext();
		Button labels = new LabelButtonV2($("SearchView.printLabels"), $("SearchView.printLabels"), customLabelTemplatesFactory,
				defaultLabelTemplatesFactory, appLayerFactory,
				sessionContext.getCurrentCollection(), sessionContext.getCurrentUser(), params.getRecordVO());

		labels.click();
	}

	public void checkIn(ContainerRecord container, MenuItemActionBehaviorParams params) {
		Button returnButton = new WindowButton($("DisplayContainerView.checkIn"),
				$("DisplayContainerView.checkIn")) {
			@Override
			protected Component buildWindowContent() {

				final JodaDateField returnDatefield = new JodaDateField();
				returnDatefield.setCaption($("DisplayContainerView.returnDate"));
				returnDatefield.setRequired(false);
				returnDatefield.setId("returnDate");
				returnDatefield.addStyleName("returnDate");
				returnDatefield.setValue(TimeProvider.getLocalDate().toDate());

				BaseButton returnFolderButton = new BaseButton($("DisplayContainerView.checkIn")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						LocalDate returnLocalDate = null;
						if (returnDatefield.getValue() != null) {
							returnLocalDate = LocalDate.fromDateFields(returnDatefield.getValue());
						}
						if (returnContainer(container, returnLocalDate, params)) {
							getWindow().close();
						}
					}
				};
				returnFolderButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				BaseButton cancelButton = new BaseButton($("cancel")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						getWindow().close();
					}
				};
				cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setSpacing(true);
				horizontalLayout.addComponents(returnFolderButton, cancelButton);

				VerticalLayout verticalLayout = new VerticalLayout();
				verticalLayout
						.addComponents(returnDatefield, horizontalLayout);
				verticalLayout.setSpacing(true);

				return verticalLayout;
			}
		};
		returnButton.click();
	}

	private boolean returnContainer(ContainerRecord container, LocalDate returnDate,
									MenuItemActionBehaviorParams params) {
		String errorMessage = borrowingServices.validateReturnDate(returnDate, container.getBorrowDate());
		if (errorMessage != null) {
			params.getView().showErrorMessage($(errorMessage));
			return false;
		}
		try {
			borrowingServices.returnContainer(container.getId(), params.getUser(), returnDate, true);
			deletePendingReturnRequestForRecord(container, params.getUser());
			new Navigation().to(RMViews.class).displayContainer(container.getId());
			return true;
		} catch (RecordServicesException e) {
			params.getView().showErrorMessage($("DisplayContainerView.cannotReturnContainer"));
			return false;
		}
	}

	private void deletePendingReturnRequestForRecord(ContainerRecord container, User user) {
		List<String> pendingRequests = getPendingReturnRequestForRecord(container);

		if (CollectionUtils.isNotEmpty(pendingRequests)) {
			for (String recordId : pendingRequests) {
				Record record = recordServices.getDocumentById(recordId);
				recordServices.logicallyDelete(record, user);
				loggingServices.logDeleteRecordWithJustification(record, user, "");
			}
		}
	}

	private List<String> getPendingReturnRequestForRecord(ContainerRecord container) {
		TasksSchemasRecordsServices tasksSchemasRecordsServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		MetadataSchemaType taskSchemaType = tasksSchemasRecordsServices.taskSchemaType();
		Metadata metadataLinkedContainer = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.LINKED_CONTAINERS);
		Metadata metadataStatus = taskSchemaType.getAllMetadatas().getMetadataWithLocalCode(Task.STATUS_TYPE);
		LogicalSearchCondition logicalSearchCondition = from(taskSchemaType)
				.where(Schemas.SCHEMA).isEqualTo(ReturnRequest.FULL_SCHEMA_NAME)
				.andWhere(metadataLinkedContainer).isEqualTo(container)
				.andWhere(metadataStatus).isEqualTo(TaskStatusType.STANDBY);

		return searchServices.searchRecordIds(logicalSearchCondition);
	}

	public void sendReturnRemainder(ContainerRecord container, MenuItemActionBehaviorParams params) {
		User borrower = null;
		if (container.getBorrower() != null) {
			borrower = rm.getUser(container.getBorrower());
		}
		String previewReturnDate = container.getPlanifiedReturnDate().toString();

		Button reminderReturnContainerButton = new SendReturnReminderEmailButton(collection, appLayerFactory,
				params.getView(), ContainerRecord.SCHEMA_TYPE, container.get(), borrower, previewReturnDate);
		reminderReturnContainerButton.click();
	}

	public void addToCart(ContainerRecord container, MenuItemActionBehaviorParams params) {
		CartWindowButton cartWindowButton = new CartWindowButton(container.getWrappedRecord(), params, AddedRecordType.CONTAINER);
		cartWindowButton.addToCart();
	}

	private void saveIfFirstTimeReportCreated(String recordId, User user, BaseView view) {
		ContainerRecord containerRecord = rm.getContainerRecord(recordId);
		ContainerRecordReportParameters reportParameters =
				new ContainerRecordReportParameters(containerRecord.getId(), containerRecord.getDecommissioningType());
		if (reportParameters.isTransfer()) {
			containerRecord.setFirstTransferReportDate(LocalDate.now());
		} else {
			containerRecord.setFirstDepositReportDate(LocalDate.now());
		}

		containerRecord.setDocumentResponsible(user.getId());
		try {
			recordServices.update(containerRecord);
		} catch (RecordServicesException e) {
			view.showErrorMessage("Could not update report creation time");
			e.printStackTrace();
		}
	}

	private String getFavoriteGroupId(MenuItemActionBehaviorParams params) {
		if (params != null) {
			return params.getFormParams().get(RMViews.FAV_GROUP_ID_KEY);
		} else {
			return null;
		}
	}

	public void delete(ContainerRecord container, MenuItemActionBehaviorParams params) {
		Button deleteButton = new DeleteButton($("delete"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				try {
					recordServices.logicallyDelete(container.getWrappedRecord(), params.getUser());
					if (BehaviorsUtil.reloadIfSearchView(params.getView())) {
						return;
					} else {
						params.getView().navigate().to(CoreViews.class).home();
					}
				} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
					params.getView().showErrorMessage(MessageUtils.toMessage(e));
				}
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithRecord", container.getTitle());
			}
		};
		deleteButton.click();
	}

	public void empty(ContainerRecord container, MenuItemActionBehaviorParams params) {
		try {
			decommissioningService.recycleContainer(container, params.getUser());
		} catch (Exception e) {
			params.getView().showErrorMessage(MessageUtils.toMessage(e));
		}
		params.getView().navigate().to(RMViews.class).displayContainer(container.getId());
	}

	private class ContainerReportPresenter implements NewReportPresenter {

		private ContainerRecord containerRecord;

		public ContainerReportPresenter(ContainerRecord containerRecord) {
			this.containerRecord = containerRecord;
		}

		@Override
		public List<ReportWithCaptionVO> getSupportedReports() {
			return asList(new ReportWithCaptionVO("Reports.ContainerRecordReport", $("Reports.ContainerRecordReport")));
		}

		@Override
		public NewReportWriterFactory getReport(String report) {
			return rmModuleExtensions.getReportBuilderFactories().transferContainerRecordBuilderFactory.getValue();
		}

		@Override
		public ContainerRecordReportParameters getReportParameters(String report) {
			return new ContainerRecordReportParameters(containerRecord.getId(), containerRecord.getDecommissioningType());
		}
	}
}
