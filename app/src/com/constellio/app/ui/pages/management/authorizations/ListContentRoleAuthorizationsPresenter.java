package com.constellio.app.ui.pages.management.authorizations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class ListContentRoleAuthorizationsPresenter extends ListAuthorizationsPresenter {

	public ListContentRoleAuthorizationsPresenter(ListContentRoleAuthorizationsView view) {
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

		String schemaType = new SchemaUtils().getSchemaTypeCode(restrictedRecord.getSchemaCode());
		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(user.getCollection(), schemaType);
		if (taxonomy != null && taxonomy.hasSameCode(taxonomiesManager.getPrincipalTaxonomy(user.getCollection()))) {
			return user.has(CorePermissions.MANAGE_SECURITY).globally();
		} else {
			return user.hasReadAccess().on(restrictedRecord);
		}
	}

	@Override
	public void backButtonClicked(String schemaCode) {
		if (schemaCode.startsWith(Folder.SCHEMA_TYPE)) {
			view.navigateTo().displayFolder(recordId);
		} else if (schemaCode.startsWith(Document.SCHEMA_TYPE)) {
			view.navigateTo().displayDocument(recordId);
		} else {
			Taxonomy taxonomy = modelLayerFactory.getTaxonomiesManager().getPrincipalTaxonomy(view.getCollection());
			view.navigateTo().taxonomyManagement(taxonomy.getCode(), recordId);
		}
	}

	@Override
	public boolean isDetacheable() {
		return true;
	}

	@Override
	public boolean isAttached() {
		Record record = recordServices().getDocumentById(recordId);
		return LangUtils.isFalseOrNull(record.get(Schemas.IS_DETACHED_AUTHORIZATIONS));
	}

	@Override
	public List<String> getAllowedAccesses() {
		// Irrelevant in roles management context.
		return new ArrayList<>();
	}

	@Override
	protected boolean isOwnAuthorization(Authorization authorization) {
		return authorization.getGrantedOnRecords().contains(recordId);
	}

	@Override
	protected void removeAuthorization(Authorization authorization) {
		authorizationsServices().delete(authorization.getDetail(), getCurrentUser());
	}

	@Override
	public boolean seeRolesField() {
		return true;
	}

	@Override
	public boolean seeAccessField() {
		return false;
	}
}
