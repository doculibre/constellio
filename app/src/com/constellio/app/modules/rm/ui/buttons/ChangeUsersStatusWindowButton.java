package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.field.CollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.collections.CollectionUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.framework.components.BaseForm.SAVE_BUTTON;
import static com.constellio.app.ui.i18n.i18n.$;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

public class ChangeUsersStatusWindowButton extends WindowButton {

	private List<User> users;
	private MenuItemActionBehaviorParams params;

	private AppLayerFactory appLayerFactory;
	private List<Record> userRecords;
	private UserServices userServices;
	private String collection;
	private SchemasRecordsServices schemaRecordsServices;

	private OptionGroup statusField;
	private CollectionSelectOptionField collectionsField;
	private Button saveButton;

	public ChangeUsersStatusWindowButton(List<User> users, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.changeStatus"), $("CollectionSecurityManagement.changeStatus"),
				WindowConfiguration.modalDialog("400px", "420px"));

		this.users = users;
		this.params = params;

		appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		userRecords = users.stream().map(u -> u.getWrappedRecord()).collect(Collectors.toList());
		userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
		collection = params.getView().getSessionContext().getCurrentCollection();
		schemaRecordsServices = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		buildStatusSelectionField();
		buildCollectionSelectionField();
		mainLayout.addComponents(statusField, collectionsField);

		VerticalLayout spacer = new VerticalLayout();
		mainLayout.addComponent(spacer);
		mainLayout.setExpandRatio(spacer, 1);

		Component buttonLayout = buildButtonLayout();
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_CENTER);

		mainLayout.setSizeFull();
		mainLayout.setMargin(true);

		return mainLayout;
	}

	private OptionGroup buildStatusSelectionField() {
		statusField = new OptionGroup();
		statusField.addStyleName("collections");
		statusField.addStyleName("collections-username");
		statusField.setId("enumStatus");
		statusField.setMultiSelect(false);

		for (UserCredentialStatus enumObject : UserCredentialStatus.class.getEnumConstants()) {
			String statusValue = enumObject.toString();
			statusField.addItem(statusValue);
			statusField.setItemCaption(statusValue, $("UserCredentialView.status." + enumObject.getCode()));
		}

		UserCredentialStatus commonStatus = users.get(0).getStatus();
		for (User user : users) {
			if (user.getStatus() != commonStatus) {
				commonStatus = null;
				break;
			}
		}

		if (commonStatus != null) {
			statusField.select(commonStatus.toString());
		}

		statusField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateSaveButtonAvailability();
			}
		});

		return statusField;
	}

	private Component buildCollectionSelectionField() {
		collectionsField = new CollectionSelectOptionField(appLayerFactory, userRecords) {
			@Override
			protected List<String> getAvailableCollections() {
				Collection<String> collections = new HashSet<>(userServices.getUserInfos(users.get(0).getUsername()).getCollections());
				for (User user : users) {
					collections = CollectionUtils.intersection(collections, userServices.getUserInfos(user.getUsername()).getCollections());
				}
				return new ArrayList<>(collections);
			}

			@Override
			protected void processCommonCollection(String collection) {

			}
		};

		collectionsField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateSaveButtonAvailability();
			}
		});

		return collectionsField;
	}

	private Component buildButtonLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();

		saveButton = new Button($("save"));
		saveButton.addStyleName(SAVE_BUTTON);
		saveButton.addStyleName(BUTTON_PRIMARY);
		saveButton.addClickListener((ClickListener) event -> {
			if (!hasSyncUser()) {
				executeAction(false);
			} else {
				showStopSyncDialog();
			}
		});

		updateSaveButtonAvailability();

		Button cancelButton = new Button($("cancel"));
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});

		buttonsLayout.addComponents(saveButton, cancelButton);
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		return buttonsLayout;
	}

	private boolean hasSyncUser() {
		for (User user : users) {
			if (userServices.getUserCredential(user.getUsername()).getSyncMode() == UserSyncMode.SYNCED) {
				return true;
			}
		}
		return false;
	}

	private void updateSaveButtonAvailability() {
		boolean isCollectionSelected = !CollectionUtils.isEmpty(collectionsField.getSelectedValues());
		boolean isStatusSelected = statusField.getValue() != null;
		saveButton.setEnabled(isCollectionSelected && isStatusSelected);
	}

	private void showStopSyncDialog() {
		ConfirmDialog.show(ConstellioUI.getCurrent(), $("CollectionSecurityManagement.desynchronizationWarningTitle"),
				$("CollectionSecurityManagement.groupUserDesynchronizationWarningMessage"), $("Ok"), $("cancel"), (ConfirmDialog.Listener) dialog -> {
					if (dialog.isConfirmed()) {
						executeAction(true);
					}
				});
	}

	private void executeAction(boolean stopSync) {
		updateUserStatus((String) statusField.getValue(), collectionsField.getSelectedValues(), stopSync);

		params.getView().navigate().to().collectionSecurity();
		params.getView().showMessage($("CollectionSecurityManagement.changedStatus"));
		getWindow().close();
	}

	private void updateUserStatus(String status, List<String> collections, boolean stopSync) {
		List<String> userCodeList = users.stream().map(user -> user.getUsername()).collect(Collectors.toList());

		for (String collection : collections) {
			for (String username : userCodeList) {
				UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(username);
				userAddUpdateRequest.setStatusForCollection(UserCredentialStatus.valueOf(status), collection);
				if (stopSync) {
					userAddUpdateRequest.stopSyncingLDAP();
				}
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
