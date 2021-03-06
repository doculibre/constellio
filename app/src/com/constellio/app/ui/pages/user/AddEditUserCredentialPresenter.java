package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserAddUpdateRequest;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserAlreadyExists;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditUserCredentialPresenter extends BasePresenter<AddEditUserCredentialView> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AddEditUserCredentialPresenter.class);
	private transient UserServices userServices;
	private transient AuthenticationService authenticationService;
	private transient CollectionsListManager collectionsListManager;
	private transient LoggingServices loggingServices;
	private boolean editMode = false;
	private String username;
	private Set<String> collections;

	public AddEditUserCredentialPresenter(AddEditUserCredentialView view) {
		super(view);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		userServices = modelLayerFactory.newUserServices();
		collectionsListManager = modelLayerFactory.getCollectionsListManager();
		authenticationService = modelLayerFactory.newAuthenticationService();
		loggingServices = modelLayerFactory.newLoggingServices();
	}

	public UserCredentialVO getUserCredentialVO(String username) {
		SystemWideUserInfos userCredential = null;
		this.username = username;
		if (!username.isEmpty()) {
			editMode = true;
			userCredential = userServices.getUserInfos(username);
		}
		UserCredentialToVOBuilder voBuilder = new UserCredentialToVOBuilder();
		UserCredentialVO userCredentialVO = userCredential != null ? voBuilder.build(userCredential) : new UserCredentialVO();
		collections = userCredentialVO.getCollections();
		return userCredentialVO;
	}

	public void saveButtonClicked(UserCredentialVO entity) {
		String username = entity.getUsername();

		if (!validateEntityInfos(entity, username)) {
			return;
		}

		try {
			if (!entity.getUsername().equals(this.username) && getUserInfos(entity.getUsername()) != null) {
				throw new UserServicesRuntimeException_UserAlreadyExists(entity.getUsername());
			}

			if (!isLDAPAuthentication() && !isEditMode() || entity.getPassword() != null && !entity.getPassword().isEmpty()) {
				authenticationService.changePassword(entity.getUsername(), entity.getPassword());
			}


			if (isEditMode()) {
				userServices.execute(toUserRequest(entity));
			} else {
				final List<String> personalEmails = entity.getPersonalEmails() == null
													? new ArrayList<>()
													: Arrays.asList(entity.getPersonalEmails().split("\n"));

				userServices.createUser(entity.getUsername(), (req) -> req
						.setFirstName(entity.getFirstName())
						.setLastName(entity.getLastName())
						.setEmail(entity.getEmail())
						.setPersonalEmails(personalEmails)
						.setServiceKey(entity.getServiceKey())
						.setSystemAdmin(entity.isSystemAdmin())
						.setCollections(new ArrayList<>(entity.getCollections()))
						.setStatusForAllCollections(entity.getStatus())
						.setDomain(entity.getDomain())
						.setMsExchDelegateListBL(Arrays.asList(""))
						.setDn(null)
						.setJobTitle(entity.getJobTitle())
						.setAddress(entity.getAddress())
						.setPhone(entity.getPhone())
						.setFax(entity.getFax())
				);
			}

			SystemWideUserInfos userInfos = userServices.getUserInfos(entity.getUsername());
			if (!editMode) {
				for (String collection : userInfos.getCollections()) {
					User userInCollection = userServices.getUserInCollection(entity.getUsername(), collection);
					loggingServices.addUserOrGroup(userInCollection.getWrappedRecord(), getCurrentUser(), collection);
				}
			} else {
				for (String collection : userInfos.getCollections()) {
					User userInCollection = userServices.getUserInCollection(entity.getUsername(), collection);
					if (entity.getCollections().contains(collection) && !collections.contains(collection)) {
						loggingServices.addUserOrGroup(userInCollection.getWrappedRecord(), getCurrentUser(), collection);
					}
				}
			}

		} catch (Exception e) {
			view.showErrorMessage(MessageUtils.toMessage(e));
			return;
		}
		view.navigate().to().previousView();
	}

	private boolean validateEntityInfos(UserCredentialVO entity, String username) {
		if (isEditMode()) {
			if (isUsernameChanged(username)) {
				showErrorMessageView("AddEditUserCredentialView.cannotChangeUsername");
				return false;
			}
		} else {
			if (userExists(username)) {
				showErrorMessageView("AddEditUserCredentialView.usernameAlredyExists");
				return false;
			}
			if (!isLDAPAuthentication() && !(entity.getPassword() != null && StringUtils.isNotBlank(entity.getPassword())
											 && entity.getPassword()
													 .equals(entity.getConfirmPassword()))) {
				showErrorMessageView("AddEditUserCredentialView.passwordsFieldsMustBeEquals");
				return false;
			} else {
				return true;
			}
		}
		return true;
	}

	void showErrorMessageView(String text) {
		view.showErrorMessage($(text));
	}

	private boolean userExists(String username) {
		try {
			UserCredential userCredential = userServices.getUserCredential(username);
			if (userCredential != null) {
				return true;
			}
		} catch (Exception e) {
			//Ok
			LOGGER.info(e.getMessage(), e);
		}
		return false;
	}

	private boolean isUsernameChanged(String username) {
		if (getUsername() != null && !getUsername().isEmpty() && !getUsername().equals(username)) {
			return true;
		}
		return false;
	}

	private UserAddUpdateRequest toUserRequest(UserCredentialVO userCredentialVO) {
		List<String> collections = new ArrayList<>();
		Map<String, LocalDateTime> tokens = new HashMap<>();

		if (userCredentialVO.getCollections() != null) {
			collections.addAll(userCredentialVO.getCollections());
		}
		if (userCredentialVO.getTokensMap() != null) {
			tokens = userCredentialVO.getTokensMap();
		}
		UserCredentialStatus status = userCredentialVO.getStatus();
		String domain = userCredentialVO.getDomain();

		List<String> personalEmails = new ArrayList<>();
		if (userCredentialVO.getPersonalEmails() != null) {
			personalEmails = Arrays.asList(userCredentialVO.getPersonalEmails().split("\n"));
		}

		UserAddUpdateRequest request = userServices.addUpdate(userCredentialVO.getUsername())
				.setFirstName(userCredentialVO.getFirstName())
				.setLastName(userCredentialVO.getLastName())
				.setEmail(userCredentialVO.getEmail())
				.setPersonalEmails(personalEmails)
				.setServiceKey(userCredentialVO.getServiceKey())
				.setSystemAdmin(userCredentialVO.isSystemAdmin())
				.setCollections(new ArrayList<>(userCredentialVO.getCollections()))
				.setDomain(domain)
				.setMsExchDelegateListBL(Arrays.asList(""))
				.setDn(null)
				.setJobTitle(userCredentialVO.getJobTitle())
				.setAddress(userCredentialVO.getAddress())
				.setPhone(userCredentialVO.getPhone())
				.setFax(userCredentialVO.getFax());

		for (String collection : userCredentialVO.getCollections()) {
			request.setStatusForCollection(status, collection);
		}

		Set<String> currentTokens = userServices.getUser(userCredentialVO.getUsername()).getAccessTokens().keySet();
		for (Map.Entry<String, LocalDateTime> entry : tokens.entrySet()) {
			if (!currentTokens.contains(entry.getKey())) {
				request.addAccessToken(entry.getKey(), entry.getValue());
			}
		}

		for (String currentToken : currentTokens) {
			if (!tokens.containsKey(currentToken)) {
				request.removeAccessToken(currentToken);
			}
		}

		return request;

	}

	public void cancelButtonClicked() {
		view.navigate().to().previousView();
	}

	public boolean isEditMode() {
		return editMode;
	}

	public List<String> getAllCollections() {
		return collectionsListManager.getCollectionsExcludingSystem();
	}

	public String getUsername() {
		return username;
	}

	public boolean canAndOrModify(String usernameInEdition) {
		UserCredential userInEdition = userServices.getUserCredential(usernameInEdition);
		UserCredential currentUser = userServices.getUserCredential(view.getSessionContext().getCurrentUser().getUsername());
		if (userInEdition != null && userInEdition.getUsername().equals("admin") && currentUser.getUsername().equals("admin")) {
			return true;
		} else {
			return userServices.canAddOrModifyUserAndGroup();
		}

	}

	public boolean canModifyPassword(String usernameInEdition) {
		UserCredential userInEdition = userServices.getUserCredential(usernameInEdition);
		UserCredential currentUser = userServices.getUserCredential(view.getSessionContext().getCurrentUser().getUsername());
		return userServices.canModifyPassword(userInEdition, currentUser);
	}

	public boolean isLDAPAuthentication() {
		return userServices.isLDAPAuthentication();
	}

	public boolean userNotLDAPSynced(String username) {
		LDAPConfigurationManager ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();
		return User.ADMIN.equals(username) || !(ldapConfigurationManager.isLDAPAuthentication() && ldapConfigurationManager.isUsersSynchActivated());
	}

	@Override
	protected boolean hasPageAccess(String params, final User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_USERS).globally();
	}

	public boolean isPasswordChangeEnabled() {
		return !User.ADMIN.equals(username) || new ConstellioEIMConfigs(modelLayerFactory).isAdminPasswordChangeEnabled();
	}

	public String getCollectionTitle(String collection) {
		return appLayerFactory.getCollectionsManager().getCollection(collection).getTitle();
	}

	private SystemWideUserInfos getUserInfos(String username) {
		try {
			return userServices.getUserInfos(username);
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			return null;
		}
	}

	public void validateUsername(String username) {
		SystemWideUserInfos userInfos = getUserInfos(username);
		if (userInfos != null && !username.equals(this.username)) {
			List<String> collections = userInfos.getCollections();
			if (collections.contains(collection)) {
				showAlreadyUsedDialog();
			} else {
				showAddToCollectionDialog(username);
			}
		}
	}

	private void showAlreadyUsedDialog() {
		view.showMessage($("AddEditUserCredentialView.usernameAlredyExists"));
		view.resetUsername();
	}

	private void showAddToCollectionDialog(String username) {
		ConfirmDialog.show(ConstellioUI.getCurrent(), $("CollectionSecurityManagement.addToCollections"),
				$("UserCredentialView.addToCollectionMessage"), $("Ok"), $("cancel"),
				(ConfirmDialog.Listener) dialog -> {
					if (dialog.isConfirmed()) {
						addToCollection(username);
					} else {
						view.resetUsername();
					}
				});
	}

	private void addToCollection(String username) {
		UserAddUpdateRequest userAddUpdateRequest = userServices.addUpdate(username);
		userAddUpdateRequest.addToCollections(collection);
		userServices.execute(userAddUpdateRequest);

		view.showMessage($("CollectionSecurityManagement.addedUserToCollections"));
		view.navigate().to().previousView();
	}
 
	public boolean isSetStatusPossible(UserCredentialVO userCredentialVO) {
		return userNotLDAPSynced(userCredentialVO.getUsername()) 
				&& !User.ADMIN.equals(userCredentialVO.getUsername()) 
				&& canAndOrModify(userCredentialVO.getUsername()) 
				&& userCredentialVO.getSyncMode() != UserSyncMode.SYNCED;
	}
}
