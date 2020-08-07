package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.ChangeEnumStatusRecordWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton.AddedToCollectionRecordType;
import com.constellio.app.modules.rm.ui.buttons.GroupWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.DeleteButton;
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
import com.constellio.model.services.records.RecordDeleteServicesRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotLogicallyDeleteRecord;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Button;
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


	public void edit(List<User> userRecords, MenuItemActionBehaviorParams params) {

	}

	public void consult(List<User> userRecords, MenuItemActionBehaviorParams params) {
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
		ChangeEnumStatusRecordWindowButton statusButton = new ChangeEnumStatusRecordWindowButton($("CollectionSecurityManagement.changeStatus"),
				$("CollectionSecurityManagement.changeStatus"), appLayerFactory, params, UserCredentialStatus.class) {
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

	public void manageSecurity(List<User> userRecords, MenuItemActionBehaviorParams params) {
	}

	public void manageRole(List<User> userRecords, MenuItemActionBehaviorParams params) {
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
}
