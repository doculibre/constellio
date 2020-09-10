package com.constellio.app.modules.rm.ui.buttons;

import com.constellio.app.modules.rm.ui.field.CollectionSelectOptionField;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections.CollectionUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.constellio.app.ui.framework.components.BaseForm.BUTTONS_LAYOUT;
import static com.constellio.app.ui.i18n.i18n.$;

public class RemoveUsersFromCollectionsWindowButton extends WindowButton {

	private List<User> users;
	private MenuItemActionBehaviorParams params;

	private AppLayerFactory appLayerFactory;
	private List<Record> userRecords;
	private UserServices userServices;

	private CollectionSelectOptionField collectionsField;
	private Button deleteButton;

	public RemoveUsersFromCollectionsWindowButton(List<User> users, MenuItemActionBehaviorParams params) {
		super($("CollectionSecurityManagement.removeFromCollection"), $("CollectionSecurityManagement.removeFromCollection"),
				WindowConfiguration.modalDialog("550px", "300px"));

		this.users = users;
		this.params = params;

		appLayerFactory = params.getView().getConstellioFactories().getAppLayerFactory();
		userRecords = users.stream().map(u -> u.getWrappedRecord()).collect(Collectors.toList());
		userServices = params.getView().getConstellioFactories().getAppLayerFactory().getModelLayerFactory().newUserServices();
	}


	@Override
	protected Component buildWindowContent() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);

		buildCollectionSelectionField();
		mainLayout.addComponents(collectionsField);

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

	private Component buildCollectionSelectionField() {
		collectionsField = new CollectionSelectOptionField(appLayerFactory, userRecords,
				$("CollectionSecurityManagement.deleteUsersFromCollection", users.size())) {
			@Override
			protected List<String> getAvailableCollections() {
				Set<String> collections = new HashSet<>();
				for (User user : users) {
					collections.addAll(userServices.getUserInfos(user.getUsername()).getCollections());
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
				updateDeleteButtonAvailability();
			}
		});

		return collectionsField;
	}

	private Component buildButtonLayout() {
		HorizontalLayout buttonsLayout = new HorizontalLayout();

		deleteButton = new DeleteButton(null, $("CollectionSecurityManagement.delete"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				try {
					executeAction(false);
				} catch (UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection e) {
					showStopSyncDialog();
				}
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithAllRecords", $("CollectionSecurityManagement.userLowerCase"));
			}
		};
		deleteButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		updateDeleteButtonAvailability();

		Button cancelButton = new Button($("cancel"));
		cancelButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
			}
		});

		buttonsLayout.addComponents(deleteButton, cancelButton);
		buttonsLayout.addStyleName(BUTTONS_LAYOUT);
		buttonsLayout.setSpacing(true);
		return buttonsLayout;
	}

	private void updateDeleteButtonAvailability() {
		boolean isCollectionSelected = !CollectionUtils.isEmpty(collectionsField.getSelectedValues());
		deleteButton.setEnabled(isCollectionSelected);
	}

	private void showStopSyncDialog() {
		ConfirmDialog.show(ConstellioUI.getCurrent(), $("CollectionSecurityManagement.desynchronizationWarningTitle"),
				$("CollectionSecurityManagement.userDesynchronizationWarningMessage"), $("Ok"), $("cancel"), (ConfirmDialog.Listener) dialog -> {
					if (dialog.isConfirmed()) {
						executeAction(true);
					}
				});
	}

	private void executeAction(boolean stopSync) {
		deleteUsersFromCollections(collectionsField.getSelectedValues(), stopSync);

		params.getView().navigate().to().collectionSecurity();
		params.getView().showMessage($("CollectionSecurityManagement.userRemovedFromCollection"));
		getWindow().close();
	}

	private void deleteUsersFromCollections(List<String> collections, boolean stopSync) {
		for (String collection : collections) {
			for (User currentUser : users) {
				UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(currentUser.getUsername());
				userAddUpdateRequest.removeFromCollection(collection);
				if (stopSync) {
					userAddUpdateRequest.stopSyncingLDAP();
				}
				userServices.execute(userAddUpdateRequest);
			}
		}
	}
}
