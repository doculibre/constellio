package com.constellio.app.services.menu;

import com.constellio.app.services.action.UserCredentialActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.services.menu.behavior.UserCredentialMenuItemActionBehaviors;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.services.menu.UserCredentialMenuItemServices.UserCredentialMenuItemActionType.USER_CREDENTIAL_EDIT;
import static com.constellio.app.services.menu.UserCredentialMenuItemServices.UserCredentialMenuItemActionType.USER_CREDENTIAL_GENERATE_TOKEN;
import static com.constellio.app.ui.i18n.i18n.$;

public class UserCredentialMenuItemServices {
	private AppLayerFactory appLayerFactory;
	private UserCredentialActionsServices userCredentialActionsServices;
	private UserServices userServices;

	public UserCredentialMenuItemServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.userCredentialActionsServices = new UserCredentialActionsServices(appLayerFactory);
		this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
	}

	public List<MenuItemAction> getActionsForRecord(UserCredential userCredential, User user,
													List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(USER_CREDENTIAL_EDIT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(USER_CREDENTIAL_EDIT.name(),
					isMenuItemActionPossible(USER_CREDENTIAL_EDIT.name(), userCredential, user, params),
					$("DisplayUserCredentialView.editButton"), null, -1, 100,
					(ids) -> new UserCredentialMenuItemActionBehaviors(appLayerFactory).edit(params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(USER_CREDENTIAL_GENERATE_TOKEN.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(USER_CREDENTIAL_GENERATE_TOKEN.name(),
					isMenuItemActionPossible(USER_CREDENTIAL_GENERATE_TOKEN.name(), userCredential, user, params),
					$("DisplayUserCredentialView.generateTokenButton"), null, -1, 200,
					(ids) -> new UserCredentialMenuItemActionBehaviors(appLayerFactory).generateToken(params));

			menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public UserCredential getUserCredential(UserCredentialVO userCredentialVO) {
		return userServices.getUserCredential(userCredentialVO.getUsername());
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, UserCredential userCredential, User user,
											MenuItemActionBehaviorParams params) {
		Record record = userCredential.getWrappedRecord();

		switch (UserCredentialMenuItemActionType.valueOf(menuItemActionType)) {
			case USER_CREDENTIAL_EDIT:
				return userCredentialActionsServices.isEditActionPossible(record, user);
			case USER_CREDENTIAL_GENERATE_TOKEN:
				return userCredentialActionsServices.isGenerateTokenActionPossibe(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(String type, boolean possible, String caption, Resource icon,
											   int group, int priority, Consumer<List<String>> command) {
		return MenuItemAction.builder()
				.type(type)
				.state(possible ? new MenuItemActionState(MenuItemActionStateStatus.VISIBLE) : new MenuItemActionState(MenuItemActionStateStatus.HIDDEN))
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.recordsLimit(1)
				.build();
	}

	enum UserCredentialMenuItemActionType {
		USER_CREDENTIAL_EDIT,
		USER_CREDENTIAL_GENERATE_TOKEN,
	}

}
