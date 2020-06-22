package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.reports.builders.administration.plan.AdministrativeUnitReportParameters;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_AdministrativeUnit;
import com.constellio.app.modules.rm.reports.model.administration.plan.AdministrativeUnitReportModel.AdministrativeUnitReportModel_User;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class AdministrativeUnitReportPresenter implements NewReportPresenter {
	private String collection;
	private MetadataSchemaTypes types;
	private TaxonomiesSearchOptions searchOptions;
	private TaxonomiesSearchServices searchService;
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private AuthorizationsServices authorizationsServices;
	private boolean withUsers;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerCollectionExtensions appCollectionExtentions;
	protected transient AppLayerSystemExtensions appSystemExtentions;
	private Locale locale;
	private User currentUser = null;

	public AdministrativeUnitReportPresenter(String collection, AppLayerFactory appLayerFactory, Locale locale,
											 User currentUser) {
		this(collection, appLayerFactory, true, locale, currentUser);
	}

	public AdministrativeUnitReportPresenter(String collection, AppLayerFactory appLayerFactory, boolean withUsers,
											 Locale locale, User currentUser) {
		this.collection = collection;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
		this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
		this.withUsers = withUsers;
		this.locale = locale;
		this.currentUser = currentUser;
	}

	public AdministrativeUnitReportModel build() {
		init();

		AdministrativeUnitReportModel model = new AdministrativeUnitReportModel();

		if (withUsers) {
			model.setDetailed(true);
		} else {
			model.setDetailed(false);
		}

		List<TaxonomySearchRecord> taxonomySearchRecords = searchService.getLinkableRootConcept(currentUser, collection,
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
					AdministrativeUnit administrativeUnit = new AdministrativeUnit(administrativeUnitRecord, types, locale);

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

						modelAdministrativeUnit.setUsers(getUsersFrom(administrativeUnit));

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

			List<TaxonomySearchRecord> childTaxonomySearchRecords = searchService.getLinkableChildConcept(currentUser,
					parentRecord, RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

			if (childTaxonomySearchRecords != null) {
				for (TaxonomySearchRecord childTaxonomySearchRecord : childTaxonomySearchRecords) {
					if (childTaxonomySearchRecord != null) {
						Record administrativeUnitRecord = childTaxonomySearchRecord.getRecord();
						if (administrativeUnitRecord != null) {
							AdministrativeUnit administrativeUnit = new AdministrativeUnit(administrativeUnitRecord,
									types, locale);

							AdministrativeUnitReportModel_AdministrativeUnit modelAdministrativeUnit = new AdministrativeUnitReportModel_AdministrativeUnit();

							String unitId = StringUtils.defaultString(administrativeUnit.getId());
							modelAdministrativeUnit.setUnitId(unitId);

							String title = StringUtils.defaultString(administrativeUnit.getTitle());
							modelAdministrativeUnit.setLabel(title);

							String code = StringUtils.defaultString(administrativeUnit.getCode());
							modelAdministrativeUnit.setCode(code);

							String description = StringUtils.defaultString(administrativeUnit.getDescription());
							modelAdministrativeUnit.setDescription(description);

							modelAdministrativeUnit.setUsers(getUsersFrom(administrativeUnit));

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

	private List<AdministrativeUnitReportModel_User> getUsersFrom(AdministrativeUnit administrativeUnit) {
		Map<String, AdministrativeUnitReportModel_User> modelUsers = new TreeMap<>();

		if (withUsers) {
			List<User> users = authorizationsServices.getUsersWithRoleForRecord(Role.WRITE,
					rmSchemasRecordsServices.getAdministrativeUnit(administrativeUnit.getId()).getWrappedRecord());
			for (User user : users) {
				AdministrativeUnitReportModel_User modelUser = new AdministrativeUnitReportModel_User();

				String firstName = StringUtils.defaultString(user.getFirstName());
				modelUser.setFirstName(firstName);

				String lastName = StringUtils.defaultString(user.getLastName());
				modelUser.setLastName(lastName);

				String userName = StringUtils.defaultString(user.getUsername());
				modelUser.setUserName(userName);

				String email = StringUtils.defaultString(user.getEmail());
				modelUser.setEmail(email);

				modelUsers.put(modelUser.getUserName(), modelUser);
			}
		}
		List<AdministrativeUnitReportModel_User> modelUsersList = new ArrayList<>(modelUsers.values());
		return modelUsersList;
	}

	private void init() {
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all()).setRows(1000);
		searchService = modelLayerFactory.newTaxonomiesSearchService();
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory, locale);
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
	}

	public boolean isWithUsers() {
		return withUsers;
	}

	public FoldersLocator getFoldersLocator() {
		return modelLayerFactory.getFoldersLocator();
	}

	@Override
	public List<ReportWithCaptionVO> getSupportedReports() {
		return asList(new ReportWithCaptionVO("Reports.AdministrativeUnits", $("Reports.AdministrativeUnits")));
	}

	@Override
	public NewReportWriterFactory getReport(String report) {
		RMModuleExtensions rmModuleExtensions = appCollectionExtentions.forModule(ConstellioRMModule.ID);
		return rmModuleExtensions.getReportBuilderFactories().transferContainerRecordBuilderFactory.getValue();
	}

	@Override
	public AdministrativeUnitReportParameters getReportParameters(String report) {
		return new AdministrativeUnitReportParameters(withUsers);
	}
}