package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
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
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ShareContentPresenter extends BasePresenter<ShareContentView> {
	private transient AuthorizationsServices authorizationsServices;
	private String recordId;

	public ShareContentPresenter(ShareContentView view) {
		super(view);
	}

	public ShareContentPresenter forRequestParams(String parameters) {
		recordId = parameters;
		return this;
	}

	public RecordVO getRecordVO() {
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public void authorizationCreationRequested(AuthorizationVO authorizationVO) {
		AuthorizationAddRequest authorization = toAuthorization(authorizationVO);
		authorizationsServices().add(authorization, getCurrentUser());
		modelLayerFactory.newLoggingServices().shareDocument(presenterService().getRecord(recordId), getCurrentUser());
		view.showMessage($("ShareContentView.shared"));
		view.returnFromPage();
	}

	public void backButtonClicked() {
		view.navigate().to(CoreViews.class).previousView();
	}

	public List<String> getAllowedAccesses() {
		List<String> results = new ArrayList<>(3);

		User user = getCurrentUser();
		Record record = presenterService().getRecord(recordId);
		if (user.hasReadAccess().on(record)) {
			results.add(Role.READ);
		}
		if (user.hasWriteAccess().on(record)) {
			results.add(Role.WRITE);
		}
		if (user.hasDeleteAccess().on(record)) {
			results.add(Role.DELETE);
		}

		return results;
	}

	public List<String> getAllowedRoles() {
		return getCurrentUser().getUserRoles();
	}

	public boolean seeRolesField() {
		return new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager()).seeUserRolesInAuthorizations();
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
				.forPrincipalsIds(principals).on(authorizationVO.getRecord())
				.startingOn(authorizationVO.getStartDate()).endingOn(authorizationVO.getEndDate());
	}

	private AuthorizationsServices authorizationsServices() {
		if (authorizationsServices == null) {
			authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		}
		return authorizationsServices;
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
}
