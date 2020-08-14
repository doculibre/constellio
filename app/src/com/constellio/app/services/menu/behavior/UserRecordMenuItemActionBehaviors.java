package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.ChangeEnumStatusRecordWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsSelectWindowButton;
import com.constellio.app.modules.rm.ui.buttons.DesynchronizationWarningDialog;
import com.constellio.app.modules.rm.ui.buttons.GroupWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class UserRecordMenuItemActionBehaviors {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;
	private RecordServices recordServices;
	private String collection;
	private SchemasRecordsServices core;

	public UserRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.core = new SchemasRecordsServices(collection, modelLayerFactory);
	}

	private Map<String, String> clone(Map<String, String> map) {
		if (map == null) {
			return null;
		}

		Map<String, String> newMap = new HashMap<>();

		newMap.putAll(map);

		return newMap;
	}


	public void edit(User userRecord, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editUserCredential(userRecord.getUsername());
	}

	public void consult(User userRecord, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().displayUserCredential(userRecord.getUsername());
	}

	public void addToGroup(List<User> userRecords, MenuItemActionBehaviorParams params) {
		List<User> synchronizedUsers = ListSelectedSynchronizedUsers(userRecords);

		List<User> recordsList = core.getUsers(userRecords.stream().map(record -> record.getId()).collect(Collectors.toList()));

		GroupWindowButton groupWindowButton = new GroupWindowButton(recordsList, params) {
			@Override
			public void addToGroup() {
				if (synchronizedUsers.isEmpty()) {
					super.addToGroup();
				} else {
					new DesynchronizationWarningDialog(synchronizedUsers).showConfirm(ConstellioUI.getCurrent(), (ConfirmDialog.Listener) warningDialog -> {
						if (warningDialog.isConfirmed()) {
							super.addToGroup();
						}
					});
				}

			}
		};
		groupWindowButton.addToGroup();
	}

	public void addToCollection(List<User> userRecords, MenuItemActionBehaviorParams params) {
		List<User> synchronizedUsers = ListSelectedSynchronizedUsers(userRecords);
		List<Record> records = userRecords.stream().map(user -> user.getWrappedRecord()).collect(Collectors.toList());
		CollectionsSelectWindowButton collectionSelectWindowButton = new CollectionsSelectWindowButton($("CollectionSecurityManagement.addedUserToCollections"), records, params) {
			@Override
			protected void saveButtonClick(BaseView baseView) {
				List<String> collectionCodes = getSelectedValues();

				for (Record record : records) {
					User currentUser = getCore().wrapUser(record);
					UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(currentUser.getUsername());
					userAddUpdateRequest.addToCollections(collectionCodes);
					userServices.execute(userAddUpdateRequest);
				}

				baseView.showMessage($("CollectionSecurityManagement.addedGroupToCollections"));
			}

			@Override
			public void addToCollections() {
				if (synchronizedUsers.isEmpty()) {
					super.addToCollections();
				} else {
					new DesynchronizationWarningDialog(synchronizedUsers).showConfirm(ConstellioUI.getCurrent(), (ConfirmDialog.Listener) warningDialog -> {
						if (warningDialog.isConfirmed()) {
							super.addToCollections();
						}
					});
				}
			}
		};
		collectionSelectWindowButton.addToCollections();
	}

	public void delete(List<User> userRecords, MenuItemActionBehaviorParams params) {
		List<User> synchronizedUsers = ListSelectedSynchronizedUsers(userRecords);
		Button deleteUserButton = new DeleteButton($("CollectionSecurityManagement.deleteGroups"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				if (synchronizedUsers.isEmpty()) {
					deleteUsersAction();
				} else {
					new DesynchronizationWarningDialog(synchronizedUsers).showConfirm(ConstellioUI.getCurrent(), (ConfirmDialog.Listener) warningDialog -> {
						if (warningDialog.isConfirmed()) {
							deleteUsersAction();
						}
					});
				}
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithAllRecords", $("CollectionSecurityManagement.userLowerCase"));
			}

			private void deleteUsersAction() {
				deleteUserFromCollection(userRecords);
				params.getView().navigate().to().collectionSecurity();
				params.getView().showMessage($("CollectionSecurityManagement.userRemovedFromCollection"));
			}
		};

		deleteUserButton.click();
	}

	private List<User> ListSelectedSynchronizedUsers(List<User> userRecords) {
		return userRecords.stream()
				.filter(u -> userServices.getUserCredential(u.getUsername()).getSyncMode().equals(UserSyncMode.SYNCED))
				.collect(Collectors.toList());
	}

	public void deleteUserFromCollection(List<User> userRecords) {
		for (User currentUser : userRecords) {
			UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(currentUser.getUsername());
			userAddUpdateRequest.removeFromCollection(collection);
			userServices.execute(userAddUpdateRequest);
		}
	}

	public void changeStatus(List<User> userRecords, MenuItemActionBehaviorParams params) {
		UserCredentialStatus currentStatus = userRecords.size() == 1 ? userRecords.get(0).getStatus() : null;
		List<User> synchronizedUsers = ListSelectedSynchronizedUsers(userRecords);
		ChangeEnumStatusRecordWindowButton statusButton = new ChangeEnumStatusRecordWindowButton($("CollectionSecurityManagement.changeStatus"),
				$("CollectionSecurityManagement.changeStatus"), appLayerFactory, params, UserCredentialStatus.class, currentStatus, synchronizedUsers) {
			@Override
			public void changeStatus(Object value) {

				try {
					userRecords.stream().forEach(user -> user.setStatus(UserCredentialStatus.valueOf((String) value)));
					recordServices.update(userRecords.stream().map(element -> element.getWrappedRecord()).collect(Collectors.toList()), params.getUser());
				} catch (RecordServicesException e) {
					log.error("User.cannotChangeStatus", e);
					params.getView().showErrorMessage($("CollectionSecurityManagement.cannotChangeUserStatus"));
				}
			}

		};
		statusButton.click();
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
			if (userCredential.getSyncMode().equals(UserSyncMode.LOCALLY_CREATED)) {        //todo remettre le !
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

	public void generateToken(MenuItemActionBehaviorParams params) {
		WindowButton windowButton = new WindowButton($("DisplayUserCredentialView.generateTokenButton"),
				$("DisplayUserCredentialView.generateToken")) {
			@Override
			protected Component buildWindowContent() {
				UserVO userCredentialVO = (UserVO) params.getRecordVO();
				//				final BaseIntegerField durationField = new BaseIntegerField($("DisplayUserCredentialView.Duration"));
				final TextField durationField = new TextField($("DisplayUserCredentialView.Duration"));

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
				final Label labelValue = new Label(getServiceKey(userCredentialVO.getUsername()));
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
								String serviceKey = getServiceKey(userCredentialVO.getUsername());
								labelValue.setValue(serviceKey);
								String token = generateToken(userCredentialVO.getUsername(), (String) unitTimeCombobox.getValue(),
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
