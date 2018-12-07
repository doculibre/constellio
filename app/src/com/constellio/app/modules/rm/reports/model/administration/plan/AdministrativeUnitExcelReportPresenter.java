package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
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
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.RolesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class AdministrativeUnitExcelReportPresenter extends BaseExcelReportPresenter implements NewReportPresenter {
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private AuthorizationsServices authorizationsServices;
	private List<String> administrativeUnitToIncludes = null;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerCollectionExtensions appCollectionExtentions;
	protected transient AppLayerSystemExtensions appSystemExtentions;


	public AdministrativeUnitExcelReportPresenter(String collection, AppLayerFactory appLayerFactory,
												  Locale locale, List<String> administrativeUnitToIncludes) {
		super(appLayerFactory, locale, collection);
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
		this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
		this.administrativeUnitToIncludes = administrativeUnitToIncludes;
	}

	private void init() {
		rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory, locale);
		authorizationsServices = modelLayerFactory.newAuthorizationsServices();
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


		if(administrativeUnitToIncludes != null || !administrativeUnitToIncludes.isEmpty()) {
			for (String administrativeUnitId : administrativeUnitToIncludes) {
				AdministrativeUnit administrativeUnit1 = rmSchemasRecordsServices.getAdministrativeUnit(administrativeUnitId);
				List<Object> recordLine = getRecordLine(administrativeUnit1.getWrappedRecord(), metadataListed);
				getExtraLineData(recordLine, administrativeUnit1);
				model.addLine(recordLine);
			}

		} else {

		}

		return model;
	}

	private List<String> getAccess(Authorization authorization) {
		List<String> access = new ArrayList<>();
		for (String roleCode : authorization.getRoles()) {
			RolesManager rolesManager = modelLayerFactory.getRolesManager();
			Role role = rolesManager.getRole(authorization.getCollection(), roleCode);
			if (role.isContentPermissionRole()) {
				access.add(roleCode);
			}
		}

		return access;
	}

	private String stringListToString(List<String> stringList, String separator) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String item : stringList) {

			if (stringBuilder.length() != 0) {
				stringBuilder.append(separator + " ");
			}

			stringBuilder.append(item);
		}

		return stringBuilder.toString();
	}

	private String accessAbreviation(List<String> roles) {
		List<String> shortened = new ArrayList<>(3);


		if (roles.contains(Role.READ)) {
			shortened.add($("AuthorizationsView.short.READ"));
		}
		if (roles.contains(Role.WRITE)) {
			shortened.add($("AuthorizationsView.short.WRITE"));
		}
		if (roles.contains(Role.DELETE)) {
			shortened.add($("AuthorizationsView.short.DELETE"));
		}

		if(shortened == null || roles.isEmpty()) {
			return "";
		}

		return stringListToString(shortened, "/");
	}

	private void updateAcces(String user, Map<String, List<String>> accessList, List<String> newAcessList) {
		List<String> currentAcces = accessList.get(user);
		if(currentAcces == null) {
			accessList.put(user, newAcessList);
		} else {
			for(String currentNewAccess : newAcessList) {
				if(!currentAcces.contains(currentNewAccess)) {
					currentAcces.add(currentNewAccess);
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
			List<String> accessList = getAccess(authorization);
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

		recordLine.add(getUserCellContent(userList, userAccessHashMap));
		recordLine.add(getUserCellContent(groupList, groupHashMap));
	}

	private String getUserCellContent(List<String> itemList, Map<String, List<String>> userAccessHashMap) {
		StringBuilder stringBuilder = new StringBuilder();

		for(String item : itemList) {
			if(stringBuilder.length() != 0) {
				stringBuilder.append(", ");
			}

			String access = accessAbreviation(userAccessHashMap.get(item));

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
