package com.constellio.app.ui.pages.collection;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.builders.GlobalGroupToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.builders.UserCredentialToVOBuilder;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField.TextInputDataProvider;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

// After rename CollectionSecurityManagementPresenter
public class ListCollectionUserPresenter extends SingleSchemaBasePresenter<ListCollectionUserView> {
	public ListCollectionUserPresenter(ListCollectionUserView view) {
		super(view, User.DEFAULT_SCHEMA);
	}

	public RecordVODataProvider getDataProvider() {
		List<String> metadataCodes = new ArrayList<>();
		String schemaCode = getSchemaCode();
		metadataCodes.add(schemaCode + "_id");
		metadataCodes.add(schemaCode + "_title");
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder().build(
				defaultSchema(), VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		return new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory, view.getSessionContext()) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery()
						.setCondition(from(defaultSchema()).where(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
						.sortAsc(Schemas.TITLE);
			}
		};
	}

	public void addButtonClicked(UserCredentialVO userCredentialVO, String roleCode) {
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential userCredential = userServices.getUserCredential(userCredentialVO.getUsername());
		userServices.addUserToCollection(userCredential, view.getCollection());

		roleUserAdditionRequested(userCredentialVO.getUsername(), roleCode);

		LoggingServices loggingServices = modelLayerFactory.newLoggingServices();
		loggingServices.addUser(getUser(userCredentialVO.getUsername()), getCurrentUser());

		view.refreshTable();
	}

	public void displayButtonClicked(RecordVO entity) {
		view.navigate().to().displayCollectionUser(entity.getId());
	}

	public void accessAuthorizationsButtonClicked(RecordVO entity) {
		view.navigate().to().listPrincipalAccessAuthorizations(entity.getId());
	}

	public void deleteButtonClicked(RecordVO entity) {
		if (entity.getId().equals(getCurrentUserId())) {
			view.showErrorMessage($("ListCollectionUserView.cannotRemoveSelf"));
			return;
		}

		UserServices userServices = modelLayerFactory.newUserServices();
		User user = coreSchemas().getUser(entity.getId());
		removeRoles(user);
		UserCredential userCredential = userServices.getUserCredential(user.getUsername());
		userServices.removeUserFromCollection(userCredential, view.getCollection());

		LoggingServices loggingServices = modelLayerFactory.newLoggingServices();
		loggingServices.removeUser(user.getWrappedRecord(), getCurrentUser());

		view.refreshTable();
	}

	private void removeRoles(User user) {
		user.setUserRoles("");
		addOrUpdate(user.getWrappedRecord());
	}

	public TextInputDataProvider<UserCredentialVO> getUserLookupProvider() {
		final UserCredentialVODataProvider provider = new UserCredentialVODataProvider(
				new UserCredentialToVOBuilder(), modelLayerFactory, null);
		provider.setUserCredentialVOs(provider.listActiveUserCredentialVOs());
		return new TextInputDataProvider<UserCredentialVO>() {
			@Override
			public List<UserCredentialVO> getData(String text, int startIndex, int count) {
				provider.setFilter(text);
				int endIndex = Math.min(startIndex + count, provider.size());
				return provider.listUserCredentialVOs().subList(startIndex, endIndex);
			}

			@Override
			public int size(String text) {
				provider.setFilter(text);
				return provider.size();
			}

			@Override
			public User getCurrentUser() {
				return ListCollectionUserPresenter.this.getCurrentUser();
			}

			@Override
			public void setOnlyLinkables(boolean onlyLinkables) {

			}

			@Override
			public ModelLayerFactory getModelLayerFactory() {
				return modelLayerFactory;
			}
		};
	}

	public TextInputDataProvider<GlobalGroupVO> getGlobalGroupLookupProvider() {
		final GlobalGroupVODataProvider provider = new GlobalGroupVODataProvider(new GlobalGroupToVOBuilder(), modelLayerFactory,
				false);
		provider.setGlobalGroupVOs(provider.listBaseGlobalGroupsVOsWithStatus(GlobalGroupStatus.ACTIVE));
		return new TextInputDataProvider<GlobalGroupVO>() {
			@Override
			public List<GlobalGroupVO> getData(String text, int startIndex, int count) {
				provider.setFilter(text);
				int endIndex = Math.min(startIndex + count, provider.size());
				return provider.listGlobalGroupVOs().subList(startIndex, endIndex);
			}

			@Override
			public int size(String text) {
				provider.setFilter(text);
				return provider.size();
			}

			@Override
			public User getCurrentUser() {
				return ListCollectionUserPresenter.this.getCurrentUser();
			}

			@Override
			public void setOnlyLinkables(boolean onlyLinkables) {

			}

			@Override
			public ModelLayerFactory getModelLayerFactory() {
				return modelLayerFactory;
			}
		};
	}

	public GlobalGroupVODataProvider getGlobalGroupVODataProvider() {
		GlobalGroupToVOBuilder voBuilder = newGlobalGroupVOBuilder();
		return newGlobalGroupVODataProvider(voBuilder);
	}

	public void addGlobalGroupButtonClicked(GlobalGroupVO globalGroupVO, String roleCode) {
		UserServices userServices = modelLayerFactory.newUserServices();
		GlobalGroup globalGroup = userServices.getGroup(globalGroupVO.getCode());
		List<String> newCollections = new ArrayList<>();
		newCollections.addAll(globalGroup.getUsersAutomaticallyAddedToCollections());
		newCollections.add(view.getCollection());
		globalGroup = globalGroup.setUsersAutomaticallyAddedToCollections(newCollections);
		userServices.addUpdateGlobalGroup(globalGroup);

		roleGroupAdditionRequested(globalGroup.getCode(), roleCode);

		LoggingServices loggingServices = modelLayerFactory.newLoggingServices();
		loggingServices.addGroup(getGroup(globalGroupVO.getCode()), getCurrentUser());

		view.refreshTable();
	}

	public void displayGlobalGroupButtonClicked(GlobalGroupVO entity) {
		view.navigate().to().displayCollectionGroup(getGroupId(entity.getCode()));
	}

	public void accessAuthorizationsGlobalGroupButtonClicked(GlobalGroupVO entity) {
		view.navigate().to().listPrincipalAccessAuthorizations(getGroupId(entity.getCode()));
	}

	public void deleteGlobalGroupButtonClicked(GlobalGroupVO entity) {
		UserServices userServices = modelLayerFactory.newUserServices();
		GlobalGroup globalGroup = userServices.getGroup(entity.getCode());
		List<String> newCollections = new ArrayList<>();
		entity.getCollections().remove(view.getCollection());
		newCollections.addAll(entity.getCollections());
		globalGroup = globalGroup.setUsersAutomaticallyAddedToCollections(newCollections);
		userServices.addUpdateGlobalGroup(globalGroup);

		LoggingServices loggingServices = modelLayerFactory.newLoggingServices();
		loggingServices.removeGroup(getGroup(entity.getCode()), getCurrentUser());

		view.refreshTable();
	}

	GlobalGroupToVOBuilder newGlobalGroupVOBuilder() {
		return new GlobalGroupToVOBuilder();
	}

	GlobalGroupVODataProvider newGlobalGroupVODataProvider(GlobalGroupToVOBuilder voBuilder) {
		return new GlobalGroupVODataProvider(voBuilder, modelLayerFactory, false);
	}

	String getCurrentUserId() {
		return getCurrentUser().getId();
	}

	Record getUser(String username) {
		MetadataSchema users = schema(User.DEFAULT_SCHEMA);
		LogicalSearchCondition condition = from(users).where(users.getMetadata(User.USERNAME)).isEqualTo(username);
		return searchServices().searchSingleResult(condition);
	}

	Record getGroup(String code) {
		MetadataSchema groups = schema(Group.DEFAULT_SCHEMA);
		LogicalSearchCondition condition = from(groups).where(groups.getMetadata(Group.CODE)).isEqualTo(code);
		return searchServices().searchSingleResult(condition);
	}

	String getGroupId(String code) {
		return getGroup(code).getId();
	}

	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}

	public void permissionsButtonClicked(RecordVO entity) {
		view.navigate().to().editCollectionUserRoles(entity.getId());
	}

	public void permissionsGlobalGroupButtonClicked(GlobalGroupVO entity) {
		view.navigate().to().editCollectionGroupRoles(getGroupId(entity.getCode()));
	}

	public List<RoleVO> getRoles() {
		List<RoleVO> result = new ArrayList<>();
		for (Role role : roleManager().getAllRoles(view.getCollection())) {
			result.add(new RoleVO(role.getCode(), role.getTitle(), role.getOperationPermissions()));
		}
		return result;
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}

	void roleUserAdditionRequested(String username, String roleCode) {
		User user = userServices().getUserInCollection(username, view.getCollection());
		List<String> roles = new ArrayList<>(user.getUserRoles());
		if (!roles.contains(roleCode)) {
			roles.add(roleCode);
			user.setUserRoles(roles);
			addOrUpdate(user.getWrappedRecord());
		}
	}

	void roleGroupAdditionRequested(String groupCode, String roleCode) {
		Group group = userServices().getGroupInCollection(groupCode, view.getCollection());
		List<String> roles = new ArrayList<>(group.getRoles());
		roles.add(roleCode);
		group.setRoles(roles);
		addOrUpdate(group.getWrappedRecord());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SECURITY).globally();
	}

	public boolean isRMModuleEnabled() {
		return this.isSchemaExisting(AdministrativeUnit.DEFAULT_SCHEMA);
	}
}
