package com.constellio.app.modules.rm.services.menu.behaviors;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.reports.builders.decommissioning.ContainerRecordReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.services.menu.behaviors.ui.SendReturnReminderEmailButton;
import com.constellio.app.modules.rm.services.menu.behaviors.util.BehaviorsUtil;
import com.constellio.app.modules.rm.services.menu.behaviors.util.RMUrlUtil;
import com.constellio.app.modules.rm.ui.buttons.BorrowWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CartWindowButton.AddedRecordType;
import com.constellio.app.modules.rm.ui.buttons.ReturnWindowButton;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.clipboard.CopyToClipBoard;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.SelectionPanelReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.util.UrlUtil.getConstellioUrl;
import static java.util.Arrays.asList;

public class ContainerRecordMenuItemActionBehaviors {
	private RMModuleExtensions rmModuleExtensions;
	private String collection;
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private DecommissioningService decommissioningService;


	public ContainerRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.recordServices = modelLayerFactory.newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.decommissioningService = new DecommissioningService(collection, appLayerFactory);
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
		Button returnButton = new ReturnWindowButton(appLayerFactory, collection,
				Collections.singletonList(container.getWrappedRecord()), params, false);
		returnButton.click();
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

	public void deleteContent(ContainerRecord containerRecord, MenuItemActionBehaviorParams params) {
		deleteContent(asList(containerRecord), params);
	}

	public void deleteContent(List<ContainerRecord> containerRecords, MenuItemActionBehaviorParams params) {
		Button deleteButton = new ConfirmDialogButton($("ContainerRecordMenuItemActionBehaviors.deleteContent.confirmationTitle")) {

			@Override
			protected String getConfirmDialogMessage() {
				return $("ContainerRecordMenuItemActionBehaviors.deleteContent.confirmationMessage");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {

			}
		};

		deleteButton.click();
	}

	public void delete(ContainerRecord container, MenuItemActionBehaviorParams params) {
		Button deleteButton = new DeleteButton($("delete"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				BaseView view = params.getView();
				try {
					recordServices.logicallyDelete(container.getWrappedRecord(), params.getUser());
					if (BehaviorsUtil.reloadIfSearchView(view) || BehaviorsUtil.reloadIfWasSearchView(view)) {
						return;
					} else {
						view.navigate().to(CoreViews.class).home();
					}
				} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord e) {
					view.showErrorMessage(MessageUtils.toMessage(e));
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

	public void borrow(ContainerRecord container, MenuItemActionBehaviorParams params) {
		borrow(Arrays.asList(container), params);
	}

	public void borrow(List<ContainerRecord> containers, MenuItemActionBehaviorParams params) {
		List<Record> records = new ArrayList<>();
		for (ContainerRecord container : containers) {
			records.add(container.getWrappedRecord());
		}

		Button borrowButton = new BorrowWindowButton(records, params);
		borrowButton.click();
	}

	public void generateReport(ContainerRecord container, MenuItemActionBehaviorParams params) {
		SelectionPanelReportPresenter reportPresenter =
				new SelectionPanelReportPresenter(appLayerFactory, collection, params.getUser()) {
					@Override
					public String getSelectedSchemaType() {
						return ContainerRecord.SCHEMA_TYPE;
					}

					@Override
					public List<String> getSelectedRecordIds() {
						return asList(container.getId());
					}
				};

		ReportTabButton reportGeneratorButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), appLayerFactory,
				params.getView().getCollection(), reportPresenter, params.getView().getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				setRecordVoList(params.getRecordVO());
				super.buttonClick(event);
			}
		};

		reportGeneratorButton.click();
	}

	public void addToSelection(ContainerRecord container, MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().addSelectedRecordId(container.getId(),
				params.getRecordVO().getSchema().getTypeCode());
	}

	public void removeToSelection(ContainerRecord container, MenuItemActionBehaviorParams params) {
		params.getView().getSessionContext().removeSelectedRecordId(container.getId(),
				params.getRecordVO().getSchema().getTypeCode());
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
