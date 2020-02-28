package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.RMRecordsMenuItemBehaviors;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.number.BaseIntegerField;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BorrowRequestWindowButton extends WindowButton {

	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rm;
	private RMConfigs rmConfigs;
	private TasksSchemasRecordsServices taskServices;
	private RMRecordsMenuItemBehaviors recordsMenuItemBehaviors;

	private List<Record> records;
	private MenuItemActionBehaviorParams params;
	private boolean isFolder;

	public BorrowRequestWindowButton(AppLayerFactory appLayerFactory, String collection, List<Record> records,
									 MenuItemActionBehaviorParams params, boolean isFolder) {
		super($("RMRequestTaskButtonExtension.borrowRequest"),
				$("RMRequestTaskButtonExtension.requestBorrowButtonTitle"));

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		rmConfigs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		taskServices = new TasksSchemasRecordsServices(collection, appLayerFactory);
		recordsMenuItemBehaviors = new RMRecordsMenuItemBehaviors(collection, appLayerFactory);

		this.records = records;
		this.params = params;
		this.isFolder = isFolder;
	}

	@Override
	protected Component buildWindowContent() {
		getWindow().setHeight("250px");

		VerticalLayout mainLayout = new VerticalLayout();
		final BaseIntegerField borrowDurationField = new BaseIntegerField(
				$("RMRequestTaskButtonExtension.borrowDuration"));

		borrowDurationField.setValue(String.valueOf(rmConfigs.getFolderBorrowingDurationDays()));
		HorizontalLayout buttonLayout = new HorizontalLayout();

		BaseButton borrowButton = new BaseButton("") {
			@Override
			protected void buttonClick(ClickEvent event) {
				borrowRecordsRequest(borrowDurationField.getValue());
				getWindow().close();
			}
		};
		borrowButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		borrowButton.setCaption($(isFolder ? "RMRequestTaskButtonExtension.confirmBorrowMultipleFolder"
										   : "RMRequestTaskButtonExtension.confirmBorrowMultipleContainer"));
		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		};

		TextArea messageField = new TextArea();
		messageField.setHeight("50%");
		messageField.setWidth("100%");
		messageField.addStyleName(ValoTheme.TEXTAREA_BORDERLESS);
		messageField.setValue($(isFolder ? "RMRequestTaskButtonExtension.requestBorrowMultipleFolderMessage"
										 : "RMRequestTaskButtonExtension.requestBorrowMultipleContainerMessage"));
		messageField.setReadOnly(true);

		buttonLayout.setSpacing(true);
		buttonLayout.addComponents(borrowButton, cancelButton);

		mainLayout.setHeight("100%");
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);
		mainLayout.addComponents(messageField, borrowDurationField, buttonLayout);

		return mainLayout;
	}

	private void borrowRecordsRequest(String inputForNumberOfDays) {
		int numberOfDays = 1;
		if (inputForNumberOfDays != null && inputForNumberOfDays.matches("^-?\\d+$")) {
			numberOfDays = Integer.parseInt(inputForNumberOfDays);

			if (numberOfDays <= 0) {
				params.getView().showErrorMessage($("RMRequestTaskButtonExtension.invalidBorrowDuration"));
				return;
			}
		} else {
			params.getView().showErrorMessage($("RMRequestTaskButtonExtension.invalidBorrowDuration"));
			return;
		}
		try {
			List<String> linkedRecordIds = new ArrayList<>();
			Metadata metadataLinkedRecord = taskServices.taskSchemaType().getAllMetadatas()
					.getMetadataWithLocalCode(isFolder ? Task.LINKED_FOLDERS : Task.LINKED_CONTAINERS);
			List<String> pendingRequestIds = recordsMenuItemBehaviors.getRequestFromUser(BorrowRequest.FULL_SCHEMA_NAME,
					params.getUser(), records, metadataLinkedRecord);
			if (pendingRequestIds.size() > 0) {
				List<Record> pendingRequests = rm.get(pendingRequestIds);
				for (Record pendingRequest : pendingRequests) {
					linkedRecordIds.addAll(pendingRequest.getList(metadataLinkedRecord));
				}
			}

			List<Task> tasks = new ArrayList<>();
			for (Record record : records) {
				if (!linkedRecordIds.contains(record.getId())) {
					Task request;
					if (isFolder) {
						request = taskServices.newBorrowFolderRequestTask(params.getUser().getId(),
								recordsMenuItemBehaviors.getAssigneesForFolder(record.getId()), record.getId(),
								numberOfDays, record.getTitle());
					} else {
						request = taskServices.newBorrowContainerRequestTask(params.getUser().getId(),
								recordsMenuItemBehaviors.getAssigneesForContainer(record.getId()), record.getId(),
								numberOfDays, record.getTitle());
					}
					tasks.add(request);
				}
			}

			if (tasks.size() > 0) {
				recordsMenuItemBehaviors.addTasksWithUserSafeOption(tasks);
				params.getView().showMessage($("RMRequestTaskButtonExtension.borrowSuccess"));
			} else {
				params.getView().showErrorMessage($("RMRequestTaskButtonExtension.taskAlreadyCreated"));
			}

		} catch (RecordServicesException e) {
			e.printStackTrace();
			params.getView().showErrorMessage($("RMRequestTaskButtonExtension.errorWhileCreatingTask"));
		}
	}
}