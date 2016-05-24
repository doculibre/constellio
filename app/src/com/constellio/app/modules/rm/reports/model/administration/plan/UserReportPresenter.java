package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.security.AuthorizationsServices;

public class UserReportPresenter {
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private AuthorizationsServices authorizationsServices;
	private List<AdministrativeUnit> administrativeUnits;

	public UserReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
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
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		recordServices = modelLayerFactory.newRecordServices();
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		administrativeUnits = getAdministrativeUnits();
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
			if (users.contains(user)) {
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
				.sortAsc(Schemas.CODE).setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.CODE));
		return rmSchemasRecordsServices.wrapAdministrativeUnits(searchServices.search(allAdminUnitsQuery));

	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}

}