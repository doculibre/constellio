/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.reports.model.administration.plan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_FilingSpace;
import com.constellio.app.modules.rm.reports.model.administration.plan.UserReportModel.UserReportModel_User;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningService;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class UserReportPresenter {
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private SearchServices searchServices;
	private RecordServices recordServices;
	private DecommissioningService decommissioningService;
	private List<FilingSpace> userFilingSpacesToPreserveOrderingWithAdminUnits;

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
		decommissioningService = new DecommissioningService(collection, modelLayerFactory);
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

					modelUser.setFilingSpaces(getFilingSpacesFor(user));

					modelUser.setAdministrativeUnits(getAdministrativeUnitsAssumingGetFilingSpaceWasCalledBefore(user));

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

	private List<UserReportModel_FilingSpace> getFilingSpacesFor(User user) {
		List<UserReportModel_FilingSpace> modelFilingSpaces = new ArrayList<>();

		if (user != null) {
			userFilingSpacesToPreserveOrderingWithAdminUnits = getUserFilingSpaces(user);
			if (userFilingSpacesToPreserveOrderingWithAdminUnits != null) {
				for (FilingSpace filingSpace : userFilingSpacesToPreserveOrderingWithAdminUnits) {
					if (filingSpace != null) {
						UserReportModel_FilingSpace modelFilingSpace = new UserReportModel_FilingSpace();

						String code = StringUtils.defaultString(filingSpace.getCode());
						modelFilingSpace.setCode(code);

						String title = StringUtils.defaultString(filingSpace.getTitle());
						modelFilingSpace.setLabel(title);

						String description = StringUtils.defaultString(filingSpace.getDescription());
						modelFilingSpace.setDescription(description);

						modelFilingSpaces.add(modelFilingSpace);
					}
				}
			}
		}

		return modelFilingSpaces;
	}

	private List<FilingSpace> getUserFilingSpaces(User user) {
		List<FilingSpace> filingSpaces = new ArrayList<>();

		List<String> filingSpaceIds = decommissioningService.getUserFilingSpaces(user);
		if (filingSpaceIds != null) {
			for (String filingSpaceId : filingSpaceIds) {
				if (filingSpaceId != null && !filingSpaceId.isEmpty()) {
					Record record = recordServices.getDocumentById(filingSpaceId);
					if (record != null) {
						FilingSpace filingSpace = rmSchemasRecordsServices.wrapFilingSpace(record);
						if (filingSpace != null) {
							filingSpaces.add(filingSpace);
						}
					}
				}
			}
		}

		return filingSpaces;
	}

	private List<UserReportModel_AdministrativeUnit> getAdministrativeUnitsAssumingGetFilingSpaceWasCalledBefore(
			User user) {
		List<UserReportModel_AdministrativeUnit> modelAdministrativeUnits = new ArrayList<>();

		if (user != null) {
			if (userFilingSpacesToPreserveOrderingWithAdminUnits != null) {
				for (FilingSpace filingSpace : userFilingSpacesToPreserveOrderingWithAdminUnits) {
					if (filingSpace != null) {
						List<String> unitIds = decommissioningService.getAdministrativeUnitsWithFilingSpaceForUser(
								filingSpace, user);
						if (unitIds != null) {
							for (String unitId : unitIds) {
								if (unitId != null && !unitId.isEmpty()) {
									Record record = recordServices.getDocumentById(unitId);
									if (record != null) {
										AdministrativeUnit administrativeUnit = rmSchemasRecordsServices
												.wrapAdministrativeUnit(record);
										if (administrativeUnit != null) {
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
								}
							}
						}
					}
				}
			}
		}

		return modelAdministrativeUnits;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}

}