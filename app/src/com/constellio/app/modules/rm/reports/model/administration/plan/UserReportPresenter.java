package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_Group;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_Role;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_RoleTarget;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.util.DateFormatUtils;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserReportPresenter {
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private AuthorizationsServices authorizationsServices;
	private List<AdministrativeUnit> administrativeUnits;
	private Locale locale;
	private MetadataSchemasManager metadataSchemasManager;

	public UserReportPresenter(String collection, ModelLayerFactory modelLayerFactory, Locale locale) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.locale = locale;
	}

	public UserReportModel build() {
		init();

		UserReportModel model = new UserReportModel();

		List<UserReportModel_User> modelUsers = getModelUsers();

		model.setUsers(modelUsers);

		return model;
	}

	private void init() {
		searchServices = modelLayerFactory.newSearchServices();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory, locale);
		recordServices = modelLayerFactory.newRecordServices();
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		administrativeUnits = getAdministrativeUnits();
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	private List<UserReportModel_User> getModelUsers() {
		List<UserReportModel_User> modelUsers = new ArrayList<>();

		List<User> users = getUsers();
		if (users != null) {
			for (User user : users) {
				if (user != null) {
					UserReportModel_User modelUser = new UserReportModel_User();

					String firstName = StringUtils.defaultString(user.getFirstName());
					modelUser.setFirstName(firstName);

					String lastName = StringUtils.defaultString(user.getLastName());
					modelUser.setLastName(lastName);

					String userName = StringUtils.defaultString(user.getUsername());
					modelUser.setUserName(userName);

					modelUser.setCreationDate(DateFormatUtils.format(user.getCreatedOn()));
					modelUser.setModificationDate(DateFormatUtils.format(user.getModifiedOn()));

					modelUser.setRoles(getSpecificRolesAndTarget(user));

					modelUser.setGroups(getGroupModel(user));

					String userId = StringUtils.defaultString(user.getId());
					userId = StringUtils.stripStart(userId, "0");
					modelUser.setUserId(userId);

					String status = StringUtils.defaultString(isActive(user));
					modelUser.setStatus(status);

					modelUser.setAdministrativeUnits(getAdministrativeUnitsModel(user));

					modelUsers.add(modelUser);
				}
			}
		}
		return modelUsers;
	}

	private boolean isAccessRole(String role) {
		return role.equals(Role.READ) || role.equals(Role.WRITE) || role.equals(Role.DELETE);
	}

	protected boolean isRoleAuthorization(Authorization auth) {
		for (String role : auth.getRoles()) {
			if (!isAccessRole(role)) {
				return true;
			}
		}
		return false;
	}


	private boolean isOwnAuthorization(Authorization authorization, String userId) {
		return authorization.getPrincipals().contains(userId);
	}

	public List<UserReportModel_Group> getGroupModel(User user) {
		List<UserReportModel_Group> userReportModelGroups = new ArrayList<>();
		List<String> groups = user.getUserGroups();

		Map<String, List<Authorization>> mapAuthorizationByGroup = new HashMap<>();

		for(String currentGroup : user.getUserGroups()) {
			mapAuthorizationByGroup.put(currentGroup, new ArrayList<Authorization>());
		}

		for(Authorization authorization : rmSchemasRecordsServices.getAllAuthorizationsInUnmodifiableState()) {
			List<String> groupAffected = doesListHaveOneItemInCommon(groups, authorization.getPrincipals());
			if(groupAffected != null) {
				for(String currentGroup : groupAffected) {
					ReportUtil.addItemToMapList(mapAuthorizationByGroup, currentGroup, authorization);
				}
			}
		}

		for(String group : mapAuthorizationByGroup.keySet()) {
			List<Authorization> authorizationByGroupList = mapAuthorizationByGroup.get(group);
			UserReportModel_Group userReportModel_group = new UserReportModel_Group(group, recordServices.getDocumentById(group).getTitle());

			for(Authorization authorization : authorizationByGroupList) {
				Record record = recordServices.getDocumentById(authorization.getTarget());
				String label = metadataSchemasManager.getSchemaTypeOf(record).getLabel(Language.withLocale(locale));
				userReportModel_group.addTarget(label + " : " + record.getTitle() + " (" + ReportUtil.accessAbreviation(ReportUtil.getAccess(authorization, modelLayerFactory)) + ")");
			}

			userReportModelGroups.add(userReportModel_group);
		}

		return userReportModelGroups;
	}



	private List<String> doesListHaveOneItemInCommon(List<String> itemList1, List<String> itemList2) {
		List<String> commonItem = new ArrayList<>();

		for(String currentItem1 : itemList1) {
			for(String currentItem2 :itemList2) {
				if(currentItem1.equals(currentItem2)){
					commonItem.add(currentItem1);
				}
			}
		}

		if(commonItem.size() > 0) {
			return commonItem;
		} else {
			return null;
		}
	}

	public Map<UserReportModel_Role, List<UserReportModel_RoleTarget>> getSpecificRolesAndTarget(User user) {
		Map<UserReportModel_Role, List<UserReportModel_RoleTarget>> specificRoles = new HashMap<>();

		for (String globalRole : user.getUserRoles()) {
			addToListOfMapUserReportModelRoleTarget(specificRoles, globalRole, null);
		}

		for (Authorization roleAuth : modelLayerFactory.newAuthorizationsServices().getRecordAuthorizations(user)) {
			if (isRoleAuthorization(roleAuth) && isOwnAuthorization(roleAuth, user.getId())) {
				for(String role : roleAuth.getRoles()) {
					Record record = rmSchemasRecordsServices.get(roleAuth.getTarget());
					UserReportModel_RoleTarget reportModel_administrativeUnit = new UserReportModel_RoleTarget(roleAuth.getTarget(), record.getTitle(), roleAuth.getTargetSchemaType());
					addToListOfMapUserReportModelRoleTarget(specificRoles, role, reportModel_administrativeUnit);
				}

			}
		}
		return specificRoles;
	}

	private RolesManager roleManager() {
		return modelLayerFactory.getRolesManager();
	}

	public String getRoleTitle(String roleCode) {
		return roleManager().getRole(collection, roleCode).getTitle();
	}

	private void addToListOfMapUserReportModelRoleTarget(Map<UserReportModel_Role, List<UserReportModel_RoleTarget>> specificRoles, String key, UserReportModel_RoleTarget userReportModel_roleTarget) {
		UserReportModel_Role userReportModel_role = new UserReportModel_Role(key);
		List<UserReportModel_RoleTarget> userReportModel_roleTargetList = specificRoles.get(userReportModel_role);

		if(userReportModel_roleTargetList == null) {
			userReportModel_roleTargetList = new ArrayList<>();
			userReportModel_role.setLabel(getRoleTitle(key));
			specificRoles.put(userReportModel_role, userReportModel_roleTargetList);
		}

		userReportModel_roleTargetList.add(userReportModel_roleTarget);
	}

	private List<User> getUsers() {
		LogicalSearchQuery allUsersQuery = new LogicalSearchQuery(LogicalSearchQueryOperators.from(
				rmSchemasRecordsServices.userSchemaType()).returnAll()).filteredByStatus(StatusFilter.ACTIVES);
		return rmSchemasRecordsServices.wrapUsers(searchServices.search(allUsersQuery));

	}

	private String isActive(User user) {
		boolean isDisabled = true;
		if (user != null) {
			String userId = user.getId();
			if (StringUtils.isNotEmpty(userId)) {
				Record userRecord = recordServices.getDocumentById(user.getId());
				if (userRecord != null) {
					Boolean disabledValue = userRecord.get(Schemas.LOGICALLY_DELETED_STATUS);
					isDisabled = disabledValue != null && disabledValue;
				}
			}
		}

		return isDisabled ? "Inactif" : "Actif";
	}

	List<UserReportModel_AdministrativeUnit> getAdministrativeUnitsModel(User user) {
		List<UserReportModel_AdministrativeUnit> modelAdministrativeUnits = new ArrayList<>();

		for (AdministrativeUnit administrativeUnit : administrativeUnits) {
			List<User> users = authorizationsServices.getUsersWithRoleForRecord(Role.WRITE,
					rmSchemasRecordsServices.getAdministrativeUnit(administrativeUnit.getId()).getWrappedRecord());

			List<String> idList = new ArrayList<>();

			for(User currentUser : users) {
				idList.add(currentUser.getId());
			}

			if (idList.contains(user.getId())) {
				UserReportModel_AdministrativeUnit modelAdministrativeUnit = new UserReportModel_AdministrativeUnit();

				String code = StringUtils.defaultString(administrativeUnit.getCode());
				String description = StringUtils.defaultString(administrativeUnit
						.getDescription());
				String title = StringUtils.defaultString(administrativeUnit.getTitle());

				modelAdministrativeUnit.setCode(code);
				modelAdministrativeUnit.setDescription(description);
				modelAdministrativeUnit.setLabel(title);

				modelAdministrativeUnits.add(modelAdministrativeUnit);
			}
		}

		return modelAdministrativeUnits;
	}

	List<AdministrativeUnit> getAdministrativeUnits() {
		LogicalSearchQuery allAdminUnitsQuery = new LogicalSearchQuery(LogicalSearchQueryOperators.from(
				rmSchemasRecordsServices.administrativeUnit.schemaType()).returnAll()).filteredByStatus(StatusFilter.ACTIVES)
				.sortAsc(Schemas.CODE);
		return rmSchemasRecordsServices.wrapAdministrativeUnits(searchServices.search(allAdminUnitsQuery));

	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}

}