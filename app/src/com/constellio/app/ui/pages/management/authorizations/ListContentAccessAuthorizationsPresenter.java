package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;

public class ListContentAccessAuthorizationsPresenter extends ListAuthorizationsPresenter {

	public ListContentAccessAuthorizationsPresenter(ListContentAccessAuthorizationsView view) {
		super(view);
	}

	@Override
	protected List<String> getRestrictedRecordIds(String params) {
		return Arrays.asList(recordId);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

	@Override
	protected boolean hasRestrictedRecordAccess(String params, User user, Record restrictedRecord) {
		TaxonomiesManager taxonomiesManager = modelLayerFactory.getTaxonomiesManager();

		String schemaType = SchemaUtils.getSchemaTypeCode(restrictedRecord.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(user.getCollection(), schemaType);
		if (taxonomy != null && taxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(user.getCollection()))) {
			return user.has(CorePermissions.MANAGE_SECURITY).globally();
		} else {
			return user.hasWriteAndDeleteAccess().on(restrictedRecord);
		}
	}

	@Override
	public void backButtonClicked(String schemaCode) {
		if (schemaCode.startsWith(Folder.SCHEMA_TYPE) || schemaCode.startsWith(Document.SCHEMA_TYPE)) {
			view.navigate().to(CoreViews.class).previousView();
		} else {
			Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(view.getCollection());
			view.navigate().to().taxonomyManagement(taxonomy.getCode(), recordId);
		}
	}

	@Override
	public boolean isDetacheable() {
		return !AdministrativeUnit.SCHEMA_TYPE.equals(getSchemaType());
	}

	public String getSchemaType() {
		return getRecordVO() == null || getRecordVO().getSchema() == null ? null : getRecordVO().getSchema().getTypeCode();
	}

	@Override
	public boolean isAttached() {
		Record record = recordServices().getDocumentById(recordId);
		return LangUtils.isFalseOrNull(record.get(Schemas.IS_DETACHED_AUTHORIZATIONS));
	}

	@Override
	public List<String> getAllowedAccesses() {
		List<String> results = new ArrayList<>(3);

		User user = getCurrentUser();
		Record record = presenterService().getRecord(recordId);
		if (user.has(CorePermissions.MANAGE_SECURITY).globally()) {
			results.addAll(Arrays.asList(Role.READ, Role.WRITE, Role.DELETE));
		} else {
			if (user.hasReadAccess().on(record)) {
				results.add(Role.READ);
			}
			if (user.hasWriteAccess().on(record)) {
				results.add(Role.WRITE);
			}
			if (user.hasDeleteAccess().on(record)) {
				results.add(Role.DELETE);
			}
		}

		return results;
	}

	@Override
	protected boolean isOwnAuthorization(Authorization authorization) {
		return authorization.getTarget().equals(recordId);
	}

	@Override
	protected void removeAuthorization(Authorization authorization) {
		authorizationsServices().execute(authorizationDeleteRequest(authorization).setExecutedBy(getCurrentUser()));
	}

	@Override
	public boolean seeRolesField() {
		return false;
	}

	@Override
	public boolean seeAccessField() {
		return true;
	}

	@Override
	public boolean seeSharedBy() {
		return false;
	}
}
