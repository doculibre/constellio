package com.constellio.app.ui.pages.management.authorizations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

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
			view.navigate().to(RMViews.class).displayFolder(recordId);
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
		List<String> results = new ArrayList<>(3);

		User user = getCurrentUser();
		Record record = presenterService().getRecord(recordId);
		if (user.has(CorePermissions.MANAGE_SECURITY).globally()) {
			results.addAll(Arrays.asList(Role.READ, Role.WRITE, Role.DELETE));
		} else {
			if (authorizationsServices().canRead(user, record)) {
				results.add(Role.READ);
			}
			if (authorizationsServices().canWrite(user, record)) {
				results.add(Role.WRITE);
			}
			if (authorizationsServices().canDelete(user, record)) {
				results.add(Role.DELETE);
			}
		}

		return results;
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
		return false;
	}

	@Override
	public boolean seeAccessField() {
		return true;
	}
}
