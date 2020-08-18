package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.excel.BaseExcelReportPresenter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AdministrativeUnitExcelReportPresenter extends BaseExcelReportPresenter implements NewReportPresenter {
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private AuthorizationsServices authorizationsServices;
	private List<String> administrativeUnitToIncludes;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerCollectionExtensions appCollectionExtentions;
	protected transient AppLayerSystemExtensions appSystemExtentions;
	private TaxonomiesSearchServices taxonomiesSearchServices;
	private TaxonomiesSearchOptions searchOptions;
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AdministrativeUnitExcelReportPresenter.class);
	private MetadataSchemaTypes types;
	private User currentUser = null;

	public AdministrativeUnitExcelReportPresenter(String collection, AppLayerFactory appLayerFactory,
												  Locale locale, List<String> administrativeUnitToIncludes,
												  User currentUser) {
		super(appLayerFactory, locale, collection);
		this.currentUser = currentUser;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
		this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
		this.administrativeUnitToIncludes = administrativeUnitToIncludes;
	}

	private void init() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory, locale);
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
	}

	public AdministrativeUnitExcelReportModel build() {
		init();

		AdministrativeUnitExcelReportModel model = new AdministrativeUnitExcelReportModel();


		MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(
				AdministrativeUnit.SCHEMA_TYPE);

		List<Metadata> metadataListed = new ArrayList<>();


		metadataListed.add(metadataSchema.getMetadata(AdministrativeUnit.CODE));
		metadataListed.add(metadataSchema.getMetadata(AdministrativeUnit.TITLE));

		for (Metadata metadata : metadataListed) {
			model.addTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
		}

		model.addTitle(i18n.$("Reports.administrativeUnitExcelUserWithPermission"));
		model.addTitle(i18n.$("Reports.administrativeUnitExcelGroupWithPermission"));


		if(administrativeUnitToIncludes != null && !administrativeUnitToIncludes.isEmpty()) {
			for (String administrativeUnitId : administrativeUnitToIncludes) {
				AdministrativeUnit administrativeUnit1 = rmSchemasRecordsServices.getAdministrativeUnit(administrativeUnitId);
				List<Object> recordLine = administrativeUnitToCellContentList(metadataListed, administrativeUnit1);
				model.addLine($(AdministrativeUnitExcelReportModel.SINGLE_SHEET_CAPTION_KEY),recordLine);
			}

		} else {

			List<AdministrativeUnit> rootAmdinistrativeUnit = new ArrayList<>();
			List<TaxonomySearchRecord> taxonomySearchRecords = taxonomiesSearchServices
					.getLinkableRootConcept(currentUser, collection,
							RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

			if (taxonomySearchRecords != null) {
				for (TaxonomySearchRecord taxonomyRecord : taxonomySearchRecords) {

					if (taxonomyRecord != null) {
						Record record = taxonomyRecord.getRecord();
						AdministrativeUnit recordAdministrativeUnit = new AdministrativeUnit(record, types, locale);

							if (recordAdministrativeUnit != null) {
								rootAmdinistrativeUnit.add(recordAdministrativeUnit);
							}
						}
				}
			}

			for(AdministrativeUnit rootAdministrativeUnit : rootAmdinistrativeUnit) {
				List<AdministrativeUnit> administrativeUnitsList = new ArrayList<>();
				getCategoriesForRecord(rootAdministrativeUnit.getWrappedRecord(), administrativeUnitsList);
				model.addLine(rootAdministrativeUnit.getCode(), administrativeUnitToCellContentList(metadataListed, rootAdministrativeUnit));

				for(AdministrativeUnit currentAdministrativeUnit : administrativeUnitsList) {
					model.addLine(rootAdministrativeUnit.getCode(), administrativeUnitToCellContentList(metadataListed, currentAdministrativeUnit));
				}
			}
		}

		return model;
	}

	private List<Object> administrativeUnitToCellContentList(List<Metadata> metadataListed,
															 AdministrativeUnit administrativeUnit1) {
		List<Object> recordLine = getRecordLine(administrativeUnit1.getWrappedRecord(), metadataListed);
		getExtraLineData(recordLine, administrativeUnit1);
		return recordLine;
	}


	private void updateAcces(String user, Map<String, List<String>> accessList, List<String> newAcessList) {
		List<String> currentAcces = accessList.get(user);
		if (currentAcces == null) {
			accessList.put(user, newAcessList);
		} else {
			for (String currentNewAccess : newAcessList) {
				if (!currentAcces.contains(currentNewAccess)) {
					currentAcces.add(currentNewAccess);
				}
			}
		}
	}

	private void getCategoriesForRecord(Record record, List<AdministrativeUnit> administrativeUnits) {

		searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all())
				.setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);

		List<TaxonomySearchRecord> children = taxonomiesSearchServices.getLinkableChildConcept(currentUser, record,
				RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

		if (children != null) {
			for (TaxonomySearchRecord child : children) {
				if (child != null) {
					try {
						Record childRecord = child.getRecord();
						if (childRecord != null) {
							AdministrativeUnit administrativeUnit = new AdministrativeUnit(childRecord, types, locale);

							if (administrativeUnit != null) {
								administrativeUnits.add(administrativeUnit);
								getCategoriesForRecord(childRecord, administrativeUnits);
							}
						}
					} catch (Exception e) {
						// throw new RuntimeException(e);
						LOGGER.info("This is not a category. It's a " + child.getRecord().getSchemaCode());
					}
				}
			}
		}
	}

	private void getExtraLineData(List<Object> recordLine, AdministrativeUnit administrativeUnit) {
		List<Authorization> authorizationList = authorizationsServices.getRecordAuthorizations(administrativeUnit.getWrappedRecord());
		List<String> groupList = new ArrayList<>();
		List<String> userList = new ArrayList<>();
		Map<String, List<String>> groupHashMap = new HashMap<>();
		Map<String, List<String>> userAccessHashMap = new HashMap<>();

		for (Authorization authorization : authorizationList) {
			List<String> accessList = ReportUtil.getAccess(authorization,modelLayerFactory);
			for (String principal : authorization.getPrincipals()) {
				Record record = rmSchemasRecordsServices.get(principal);
				if (record.getSchemaCode().split("_")[0].equals(User.SCHEMA_TYPE)) {

					updateAcces(record.getTitle(), userAccessHashMap, accessList);

					if (!userList.contains(record.getTitle())) {
						userList.add(record.getTitle());
					}
				} else {
					updateAcces(record.getTitle(), groupHashMap, accessList);
					if (!groupList.contains(record.getTitle())) {
						groupList.add(record.getTitle());
					}
				}
			}
		}

		List<User> usersWithGlobalAccessInCollection = authorizationsServices.getUsersWithGlobalAccessInCollection(collection);
		for(User currentUser: usersWithGlobalAccessInCollection) {
			List<String> accessList = new ArrayList<>();
			if(currentUser.hasCollectionReadAccess()) {
				accessList.add(Role.READ);
			}
			if(currentUser.hasCollectionWriteAccess()) {
				accessList.add(Role.WRITE);
			}
			if(currentUser.hasCollectionDeleteAccess()) {
				accessList.add(Role.DELETE);
			}

			if(!accessList.isEmpty()) {
				updateAcces(currentUser.getTitle(), userAccessHashMap, accessList);
			}

			if (!userList.contains(currentUser.getTitle())) {
				userList.add(currentUser.getTitle());
			}
		}

		recordLine.add(getUserCellContent(userList, userAccessHashMap));
		recordLine.add(getUserCellContent(groupList, groupHashMap));
	}

	private String getUserCellContent(List<String> itemList, Map<String, List<String>> userAccessHashMap) {
		StringBuilder stringBuilder = new StringBuilder();

		for(String item : itemList) {
			if(stringBuilder.length() != 0) {
				stringBuilder.append(", ");
			}

			String access = ReportUtil.accessAbreviation(userAccessHashMap.get(item));

			stringBuilder.append(item);

			if(access != null && access.length() >= 0) {
				stringBuilder.append(" (" + access + ")");
			}
		}

		return stringBuilder.toString();
	}

	@Override
	public List<ReportWithCaptionVO> getSupportedReports() {
		return null;
	}

	@Override
	public NewReportWriterFactory getReport(String report) {
		return null;
	}

	@Override
	public Object getReportParameters(String report) {
		return null;
	}

	public Locale getLocale() {
		return locale;
	}
}
