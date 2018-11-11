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
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.AuthorizationsServices;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdministrativeUnitExcelReportPresenter extends BaseExcelReportPresenter implements NewReportPresenter {
	private RMSchemasRecordsServices rmSchemasRecordsServices;
	private AuthorizationsServices authorizationsServices;
	protected transient ModelLayerFactory modelLayerFactory;
	protected transient AppLayerCollectionExtensions appCollectionExtentions;
	protected transient AppLayerSystemExtensions appSystemExtentions;

	public AdministrativeUnitExcelReportPresenter(String collection, AppLayerFactory appLayerFactory,
												  Locale locale) {
		super(appLayerFactory, locale, collection);
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
		this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
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

		List<AdministrativeUnit> administrativeUnitList = rmSchemasRecordsServices.getAllAdministrativeUnits();


		for (AdministrativeUnit administrativeUnit : administrativeUnitList) {
			List<Object> recordLine = getRecordLine(administrativeUnit.getWrappedRecord(), metadataListed);
			getExtraLineData(recordLine, administrativeUnit);
			model.addLine(recordLine);
		}


		// TODO JOJO

		return model;
	}

	private void getExtraLineData(List<Object> recordLine, AdministrativeUnit administrativeUnit) {
		List<Authorization> authorizationList = authorizationsServices.getRecordAuthorizations(administrativeUnit.getWrappedRecord());
		List<String> groupList = new ArrayList<>();
		List<String> userList = new ArrayList<>();
		StringBuilder userStringBuilder = new StringBuilder();
		StringBuilder groupStringBuilder = new StringBuilder();

		for (Authorization authorization : authorizationList) {
			for (String principal : authorization.getPrincipals()) {
				Record record = rmSchemasRecordsServices.get(principal);
				if (record.getSchemaCode().split("_")[0].equals(User.SCHEMA_TYPE)) {
					if (!userList.contains(record.getTitle())) {
						userList.add(record.getTitle());
						if (userList.size() > 1) {
							userStringBuilder.append(", ");
						}

						userStringBuilder.append(record.getTitle());
					}
				} else {
					if (!groupList.contains(record.getTitle())) {
						groupList.add(record.getTitle());

						if (groupList.size() > 1) {
							groupStringBuilder.append(", ");
						}

						groupStringBuilder.append(record.getTitle());
					}
				}
			}
		}

		recordLine.add(userStringBuilder);
		recordLine.add(groupStringBuilder);
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
