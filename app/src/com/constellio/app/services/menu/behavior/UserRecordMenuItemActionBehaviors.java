package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.ChangeEnumStatusRecordWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton.AddedToCollectionRecordType;
import com.constellio.app.modules.rm.ui.buttons.GroupWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
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

	public UserRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
		this.recordServices = modelLayerFactory.newRecordServices();
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
		GroupWindowButton groupWindowButton = new GroupWindowButton(userRecords, params);
		groupWindowButton.addToGroup();
	}

	public void addToCollection(List<User> userRecords, MenuItemActionBehaviorParams params) {
		List<Record> records = userRecords.stream().map(user -> user.getWrappedRecord()).collect(Collectors.toList());
		CollectionsWindowButton cartWindowButton = new CollectionsWindowButton(records, params, AddedToCollectionRecordType.USER);
		cartWindowButton.addToCollections();
	}

	public void delete(List<User> userRecords, MenuItemActionBehaviorParams params) {

		Button deleteUserButton = new DeleteButton($("CollectionSecurityManagement.deleteUsers"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				logicallyDeleteUsers(userRecords, params);
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithAllRecords", $("CollectionSecurityManagement.userLowerCase"));
			}
		};

		deleteUserButton.click();
	}

	public void changeStatus(List<User> userRecords, MenuItemActionBehaviorParams params) {
		UserCredentialStatus currentStatus = userRecords.size() == 1 ? userRecords.get(0).getStatus() : null;
		ChangeEnumStatusRecordWindowButton statusButton = new ChangeEnumStatusRecordWindowButton($("CollectionSecurityManagement.changeStatus"),
				$("CollectionSecurityManagement.changeStatus"), appLayerFactory, params, UserCredentialStatus.class, currentStatus) {
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

	public void manageRole(User user, MenuItemActionBehaviorParams params) {
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
			}
		}
		if (!userCredentialsToUpdate.isEmpty()) {
			try {
				recordServices.update(userCredentialsToUpdate.stream().map(x -> x.getWrappedRecord()).collect(Collectors.toList()), params.getUser());
			} catch (RecordServicesException e) {
				log.error("User.cannotChangeSynchronization", e);
				params.getView().showErrorMessage($("CollectionSecurityManagement.cannotChangeSynchronization"));
			}
		}
	}

	private void logicallyDeleteUsers(List<User> users, MenuItemActionBehaviorParams params) {
		SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(User.DEFAULT_SCHEMA,
				params.getView().getConstellioFactories(), params.getView().getSessionContext());
		//TODO
		//add when validate is done
		//Need reason?
		ValidationErrors validateLogicallyDeletable = new ValidationErrors();//userServices.validateLogicallyDeletable(users, params.getUser());

		if (validateLogicallyDeletable.isEmpty()) {

			boolean isDeleteSuccessful = delete(presenterUtils, params.getView(), users, "", false, 1);
			if (isDeleteSuccessful) {
				params.getView().navigate().to().collectionSecurity();
			}
		} else {
			MessageUtils.getCannotDeleteWindow(validateLogicallyDeletable).openWindow();
		}
	}

	private boolean delete(SchemaPresenterUtils presenterUtils, BaseView view, List<User> users, String reason,
						   boolean physically, int waitSeconds) {
		boolean isDeletetionSuccessful = false;
		try {
			for (User user : users) {
				presenterUtils.delete(user.getWrappedRecord(), reason, physically, waitSeconds);
			}
			isDeletetionSuccessful = true;
		} catch (RecordServicesRuntimeException_CannotLogicallyDeleteRecord exception) {
			view.showErrorMessage(MessageUtils.toMessage(exception));
		} catch (RecordDeleteServicesRuntimeException exception) {
			view.showErrorMessage($("deletionFailed") + "\n" + MessageUtils.toMessage(exception));
		}

		return isDeletetionSuccessful;
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
