package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.AddUsersToCollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.AddUsersToGroupsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.ChangeUsersStatusWindowButton;
import com.constellio.app.modules.rm.ui.buttons.RemoveUsersFromCollectionsWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.TransferPermissionsButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.management.authorizations.TransferPermissionPresenter;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class UserRecordMenuItemActionBehaviors {

	private String collection;
	private AppLayerFactory appLayerFactory;

	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;
	private RecordServices recordServices;

	public UserRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	public void edit(User userRecord, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editUserCredential(userRecord.getUsername());
	}

	public void consult(User userRecord, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().displayUserCredential(userRecord.getUsername());
	}

	public void addToGroup(List<User> userRecords, MenuItemActionBehaviorParams params) {
		AddUsersToGroupsWindowButton addButton = new AddUsersToGroupsWindowButton(userRecords, params);
		addButton.click();
	}

	public void addToCollection(List<User> userRecords, MenuItemActionBehaviorParams params) {
		AddUsersToCollectionsWindowButton addButton = new AddUsersToCollectionsWindowButton(userRecords, params);
		addButton.click();
	}

	public void delete(List<User> userRecords, MenuItemActionBehaviorParams params) {
		RemoveUsersFromCollectionsWindowButton deleteButton = new RemoveUsersFromCollectionsWindowButton(userRecords, params);
		deleteButton.click();
	}

	public void changeStatus(List<User> userRecords, MenuItemActionBehaviorParams params) {
		ChangeUsersStatusWindowButton updateButton = new ChangeUsersStatusWindowButton(userRecords, params);
		updateButton.click();
	}

	public void manageSecurity(User user, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().listPrincipalAccessAuthorizations(user.getId());
	}

	public void manageRoles(User user, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editCollectionUserRoles(user.getId());
	}

	public void synchronize(List<User> userRecords, MenuItemActionBehaviorParams params, boolean isSynchronizing) {
		List<UserCredential> userCredentialsToUpdate = new ArrayList<>();
		for (User user : userRecords) {
			UserCredential userCredential = userServices.getUserCredential(user.getUsername());
			if (!userCredential.getSyncMode().equals(UserSyncMode.LOCALLY_CREATED)) {
				if (isSynchronizing) {
					userCredential.setSyncMode(UserSyncMode.SYNCED);
				} else {
					userCredential.setSyncMode(UserSyncMode.NOT_SYNCED);
				}
				userCredentialsToUpdate.add(userCredential);
			} else {
				params.getView().showErrorMessage($("CollectionSecurityManagement.userCreatedLocally", user.getUsername()));
			}
		}
		if (!userCredentialsToUpdate.isEmpty()) {
			try {
				recordServices.update(userCredentialsToUpdate.stream().map(x -> x.getWrappedRecord()).collect(Collectors.toList()), params.getUser());
				String confirmationMessage = isSynchronizing
											 ? $("CollectionSecurityManagement.changedSynchronized")
											 : $("CollectionSecurityManagement.changedDesynchronized");
				params.getView().showMessage(confirmationMessage);
			} catch (RecordServicesException e) {
				log.error("User.cannotChangeSynchronization", e);
				params.getView().showErrorMessage($("CollectionSecurityManagement.cannotChangeSynchronization"));
			}
		}
	}

	public void transferPermission(User user, MenuItemActionBehaviorParams params) {
		TransferPermissionPresenter transferPermissionPresenter = new TransferPermissionPresenter(params.getView(), user.getId());
		TransferPermissionsButton transferUserPermissions = new TransferPermissionsButton(
				$("TransferPermissionsButton.title"),
				$("TransferPermissionsButton.title"),
				transferPermissionPresenter);
		transferUserPermissions.click();
	}

	public void generateToken(User user, MenuItemActionBehaviorParams params) {
		WindowButton windowButton = new WindowButton($("DisplayUserCredentialView.generateTokenButton"),
				$("DisplayUserCredentialView.generateToken")) {
			@Override
			protected Component buildWindowContent() {
				//				final BaseIntegerField durationField = new BaseIntegerField($("DisplayUserCredentialView.Duration"));
				final BaseTextField durationField = new BaseTextField($("DisplayUserCredentialView.Duration"));

				final ComboBox unitTimeCombobox = new BaseComboBox();
				unitTimeCombobox.setNullSelectionAllowed(false);
				unitTimeCombobox.setCaption($("DisplayUserCredentialView.unitTime"));
				unitTimeCombobox.addItem("hours");
				unitTimeCombobox.setItemCaption("hours", $("DisplayUserCredentialView.hours"));
				unitTimeCombobox.setValue("hours");
				unitTimeCombobox.addItem("days");
				unitTimeCombobox.setItemCaption("days", $("DisplayUserCredentialView.days"));

				HorizontalLayout horizontalLayoutFields = new HorizontalLayout();
				horizontalLayoutFields.setSpacing(true);
				horizontalLayoutFields.addComponents(durationField, unitTimeCombobox);

				//
				final Label label = new Label($("DisplayUserCredentialView.serviceKey"));
				final Label labelValue = new Label(getServiceKey(user.getUsername()));
				final HorizontalLayout horizontalLayoutServiceKey = new HorizontalLayout();
				horizontalLayoutServiceKey.setSpacing(true);
				horizontalLayoutServiceKey.addComponents(label, labelValue);

				final Label tokenLabel = new Label($("DisplayUserCredentialView.token"));
				final Label tokenValue = new Label();
				final HorizontalLayout horizontalLayoutToken = new HorizontalLayout();
				horizontalLayoutToken.setSpacing(true);
				horizontalLayoutToken.addComponents(tokenLabel, tokenValue);

				final Link linkTest = new Link($("DisplayUserCredentialView.test"), new ExternalResource(""));
				linkTest.setTargetName("_blank");

				final VerticalLayout verticalLayoutGenerateValues = new VerticalLayout();
				verticalLayoutGenerateValues
						.addComponents(horizontalLayoutServiceKey, horizontalLayoutToken, linkTest);
				verticalLayoutGenerateValues.setSpacing(true);
				verticalLayoutGenerateValues.setVisible(false);

				final BaseButton generateTokenButton = new BaseButton($("DisplayUserCredentialView.generateToken")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						int durationValue;
						try {
							if (durationField.getValue() != null) {
								durationValue = Integer.valueOf(durationField.getValue());
								String serviceKey = getServiceKey(user.getUsername());
								labelValue.setValue(serviceKey);
								String token = generateToken(user.getUsername(), (String) unitTimeCombobox.getValue(),
										durationValue);
								tokenValue.setValue(token);
								String constellioUrl = getConstellioUrl();
								String linkValue = constellioUrl + "select?token=" + token + "&serviceKey=" + serviceKey
												   + "&fq=-type_s:index" + "&q=*:*";
								linkTest.setResource(new ExternalResource(linkValue));

								verticalLayoutGenerateValues.setVisible(true);
							}
						} catch (Exception e) {
						}
					}
				};
				generateTokenButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				generateTokenButton.setEnabled(false);

				durationField.addTextChangeListener(new TextChangeListener() {
					@Override
					public void textChange(TextChangeEvent event) {
						enableOrDisableButton(event.getText(), generateTokenButton);
					}
				});

				//
				VerticalLayout mainVerticalLayout = new VerticalLayout();
				mainVerticalLayout
						.addComponents(horizontalLayoutFields, generateTokenButton, verticalLayoutGenerateValues);
				mainVerticalLayout.setSpacing(true);

				return mainVerticalLayout;
			}
		};

		windowButton.click();
	}

	private void enableOrDisableButton(String value, BaseButton generateTokenButton) {
		boolean enable = false;
		if (value != null) {
			int durationValue;
			try {
				durationValue = Integer.valueOf(value);
				if (durationValue > 0) {
					enable = true;
				}
			} catch (NumberFormatException e) {
			}
		}
		generateTokenButton.setEnabled(enable);
	}

	public String generateToken(String username, String unitTime, int duration) {
		return userServices.generateToken(username, unitTime, duration);
	}

	public String getServiceKey(String username) {
		String serviceKey = userServices.getUserInfos(username).getServiceKey();
		if (serviceKey == null) {
			serviceKey = userServices.giveNewServiceKey(username);
		}
		return serviceKey;
	}

	public String getConstellioUrl() {
		return new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getConstellioUrl();
	}
}
