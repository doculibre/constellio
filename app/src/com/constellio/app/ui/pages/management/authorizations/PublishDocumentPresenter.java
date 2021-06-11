package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

public class PublishDocumentPresenter extends BasePresenter<PublishDocumentView> {
	private String recordId;

	public PublishDocumentPresenter(PublishDocumentView view) {
		super(view);
	}

	public PublishDocumentPresenter forRequestParams(String parameters) {
		recordId = parameters;
		return this;
	}

	public RecordVO getRecordVO() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void backButtonClicked() {
		view.navigate().to(CoreViews.class).previousView();
	}


	private AuthorizationAddRequest toAuthorization(AuthorizationVO authorizationVO) {
		Authorization details;

		ArrayList<String> roles = new ArrayList<>();
		roles.addAll(authorizationVO.getAccessRoles());

		for (String roleCode : authorizationVO.getUserRoles()) {
			roles.add(roleCode);
		}

		List<String> principals = new ArrayList<>();
		principals.addAll(authorizationVO.getUsers());
		principals.addAll(authorizationVO.getGroups());

		return AuthorizationAddRequest.authorizationInCollection(collection).giving(roles)
				.forPrincipalsIds(principals).on(authorizationVO.getRecord()).sharedBy(authorizationVO.getSharedBy())
				.startingOn(authorizationVO.getStartDate()).endingOn(authorizationVO.getEndDate());
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return asList(params);
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		String schemaType = new SchemaUtils().getSchemaTypeCode(restrictedRecord.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(user.getCollection(), schemaType);
		if (taxonomy != null && taxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(user.getCollection()))) {
			return user.has(CorePermissions.MANAGE_SECURITY).globally();
		} else {
			return user.hasReadAccess().on(restrictedRecord);
		}
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	public String getConstellioUrl() {
		return appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.CONSTELLIO_URL);
	}

	public RMSchemasRecordsServices getRmService() {
		return new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public Document publishDocument(String recordId, Date publishStartDate, Date publishEndDate)
			throws RecordServicesException {
		Document document = getRmService().wrapDocument(recordServices().getDocumentById(recordId));
		document.setPublished(true);

		if (publishStartDate == null) {
			publishStartDate = new Date();
		}
		document.setPublishingStartDate((new LocalDateTime(publishStartDate)).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).toLocalDate());
		if (publishEndDate != null) {
			document.setPublishingEndDate((new LocalDateTime(publishEndDate)).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).toLocalDate());
		}
		recordServices().update(document);

		return document;
	}

	public boolean validatePublishingDates(Date publishStartDate, Date publishEndDate) {

		Date today = resetTimeOfDay(new Date());
		if (publishStartDate != null && today.after(publishStartDate)) {
			return false;
		}
		if (publishEndDate != null && today.after(publishEndDate)) {
			return false;
		}
		if (publishStartDate != null && publishEndDate != null) {
			if (publishStartDate.after(publishEndDate)) {
				return false;
			}
		}
		return true;
	}

	public Date resetTimeOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);

		return calendar.getTime();
	}

	protected boolean isDateFieldValuesRequired() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager())
				.isDateFieldValuesRequiredInPublishDocument();
	}
}
