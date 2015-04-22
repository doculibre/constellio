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

import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_FilingSpace;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_User;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

public class AdministrativeUnitReportPresenter {
	private String collection;
	private ModelLayerFactory modelLayerFactory;
	private MetadataSchemaTypes types;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices searchService;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private boolean withUsers;

	public AdministrativeUnitReportPresenter(String collection, ModelLayerFactory modelLayerFactory) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.withUsers = true;
	}

	public AdministrativeUnitReportPresenter(String collection, ModelLayerFactory modelLayerFactory, boolean withUsers) {

		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.withUsers = withUsers;
	}

	public AdministrativeUnitReportModel build() {
		init();

		AdministrativeUnitReportModel model = new AdministrativeUnitReportModel();

		List<TaxonomySearchRecord> taxonomySearchRecords = searchService.getLinkableRootConcept(User.GOD, collection,
				RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

		List<AdministrativeUnitReportModel_AdministrativeUnit> modelAdministrativeUnits = getUnits(taxonomySearchRecords);

		model.setAdministrativeUnits(modelAdministrativeUnits);

		return model;
	}

	private List<AdministrativeUnitReportModel_AdministrativeUnit> getUnits(
			List<TaxonomySearchRecord> taxonomySearchRecords) {

		List<AdministrativeUnitReportModel_AdministrativeUnit> modelAdministrativeUnits = new ArrayList<>();

		for (TaxonomySearchRecord taxonomySearchRecord : taxonomySearchRecords) {
			if (taxonomySearchRecord != null) {
				Record administrativeUnitRecord = taxonomySearchRecord.getRecord();
				if (administrativeUnitRecord != null) {
					AdministrativeUnit administrativeUnit = new AdministrativeUnit(administrativeUnitRecord, types);

					if (administrativeUnit != null) {
						AdministrativeUnitReportModel_AdministrativeUnit modelAdministrativeUnit = new AdministrativeUnitReportModel_AdministrativeUnit();

						String unitId = StringUtils.defaultString(administrativeUnit.getId());
						String title = StringUtils.defaultString(administrativeUnit.getTitle());
						String code = StringUtils.defaultString(administrativeUnit.getCode());
						String description = StringUtils.defaultString(administrativeUnit.getDescription());

						modelAdministrativeUnit.setUnitId(unitId);
						modelAdministrativeUnit.setLabel(title);
						modelAdministrativeUnit.setCode(code);
						modelAdministrativeUnit.setDescription(description);

						modelAdministrativeUnit.setFilingSpaces(getFilingSpacesFrom(administrativeUnit));

						modelAdministrativeUnit.setChildAdministrativeUnits(getChildUnits(administrativeUnitRecord));

						modelAdministrativeUnits.add(modelAdministrativeUnit);
					}
				}
			}
		}
		return modelAdministrativeUnits;
	}

	private List<AdministrativeUnitReportModel_AdministrativeUnit> getChildUnits(Record parentRecord) {
		List<AdministrativeUnitReportModel_AdministrativeUnit> children = new ArrayList<>();

		if (parentRecord != null) {
			List<TaxonomySearchRecord> childTaxonomySearchRecords = searchService.getLinkableChildConcept(User.GOD,
					parentRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

			if (childTaxonomySearchRecords != null) {
				for (TaxonomySearchRecord childTaxonomySearchRecord : childTaxonomySearchRecords) {
					if (childTaxonomySearchRecord != null) {
						Record administrativeUnitRecord = childTaxonomySearchRecord.getRecord();
						if (administrativeUnitRecord != null) {
							AdministrativeUnit administrativeUnit = new AdministrativeUnit(administrativeUnitRecord,
									types);

							AdministrativeUnitReportModel_AdministrativeUnit modelAdministrativeUnit = new AdministrativeUnitReportModel_AdministrativeUnit();

							String unitId = StringUtils.defaultString(administrativeUnit.getId());
							modelAdministrativeUnit.setUnitId(unitId);

							String title = StringUtils.defaultString(administrativeUnit.getTitle());
							modelAdministrativeUnit.setLabel(title);

							String code = StringUtils.defaultString(administrativeUnit.getCode());
							modelAdministrativeUnit.setCode(code);

							String description = StringUtils.defaultString(administrativeUnit.getDescription());
							modelAdministrativeUnit.setDescription(description);

							modelAdministrativeUnit.setFilingSpaces(getFilingSpacesFrom(administrativeUnit));
							modelAdministrativeUnit
									.setChildAdministrativeUnits(getChildUnits(administrativeUnitRecord));

							children.add(modelAdministrativeUnit);
						}
					}
				}
			}
		}

		return children;
	}

	private List<AdministrativeUnitReportModel_FilingSpace> getFilingSpacesFrom(AdministrativeUnit administrativeUnit) {
		List<AdministrativeUnitReportModel_FilingSpace> filingSpaces = new ArrayList<>();

		if (administrativeUnit != null) {
			for (String filingSpaceId : administrativeUnit.getFilingSpaces()) {

				if (filingSpaceId != null && !filingSpaceId.isEmpty()) {
					FilingSpace filingSpace = rmSchemasRecordsServices.getFilingSpace(filingSpaceId);
					if (filingSpace != null) {
						AdministrativeUnitReportModel_FilingSpace modelFilingSpace = new AdministrativeUnitReportModel_FilingSpace();

						String code = StringUtils.defaultString(filingSpace.getCode());
						modelFilingSpace.setCode(code);

						String description = StringUtils.defaultString(filingSpace.getDescription());
						modelFilingSpace.setDescription(description);

						String title = StringUtils.defaultString(filingSpace.getTitle());
						modelFilingSpace.setLabel(title);

						modelFilingSpace.setUsers(getUsersFrom(filingSpace));
						modelFilingSpace.setAdministrators(getAdministratorsFrom(filingSpace));

						filingSpaces.add(modelFilingSpace);
					}
				}
			}
		}

		return filingSpaces;
	}

	private List<AdministrativeUnitReportModel_User> getUsersFrom(FilingSpace filingSpace) {
		List<AdministrativeUnitReportModel_User> modelUsers = new ArrayList<>();

		if (withUsers) {
			if (filingSpace != null) {
				List<String> userIds = filingSpace.getUsers();
				if (userIds != null) {
					for (String userId : userIds) {
						if (userId != null && !userId.isEmpty()) {
							User user = rmSchemasRecordsServices.getUser(userId);
							if (user != null) {
								AdministrativeUnitReportModel_User modelUser = new AdministrativeUnitReportModel_User();

								String firstName = StringUtils.defaultString(user.getFirstName());
								modelUser.setFirstName(firstName);

								String lastName = StringUtils.defaultString(user.getLastName());
								modelUser.setLastName(lastName);

								String userName = StringUtils.defaultString(user.getUsername());
								modelUser.setUserName(userName);

								String email = StringUtils.defaultString(user.getEmail());
								modelUser.setEmail(email);

								modelUsers.add(modelUser);
							}
						}

					}
				}
			}
		}
		return modelUsers;
	}

	private List<AdministrativeUnitReportModel_User> getAdministratorsFrom(FilingSpace filingSpace) {
		List<AdministrativeUnitReportModel_User> modelAdministrators = new ArrayList<>();

		if (withUsers) {
			if (filingSpace != null) {
				List<String> administratorIds = filingSpace.getAdministrators();
				if (administratorIds != null) {
					for (String administratorId : administratorIds) {
						if (administratorId != null && !administratorId.isEmpty()) {
							User administrator = rmSchemasRecordsServices.getUser(administratorId);
							if (administrator != null) {
								AdministrativeUnitReportModel_User modelAdministrator = new AdministrativeUnitReportModel_User();

								String firstName = StringUtils.defaultString(administrator.getFirstName());
								modelAdministrator.setFirstName(firstName);

								String lastName = StringUtils.defaultString(administrator.getLastName());
								modelAdministrator.setLastName(lastName);

								String userName = StringUtils.defaultString(administrator.getUsername());
								modelAdministrator.setUserName(userName);

								String email = StringUtils.defaultString(administrator.getEmail());
								modelAdministrator.setEmail(email);

								modelAdministrators.add(modelAdministrator);
							}
						}
					}
				}
			}
		}
		return modelAdministrators;
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
		searchService = modelLayerFactory.newTaxonomiesSearchService();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public boolean isWithUsers() {
		return withUsers;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}
}