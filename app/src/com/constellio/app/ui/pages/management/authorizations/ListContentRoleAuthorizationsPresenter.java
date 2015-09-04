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
		List<String> results = new ArrayList<>();

		// What to do with this in roles context?

//		User user = getCurrentUser();
//		Record record = presenterService().getRecord(recordId);
//		if (authorizationsServices().canRead(user, record)) {
//			results.add(Role.READ);
//		}
//		if (authorizationsServices().canWrite(user, record)) {
//			results.add(Role.WRITE);
//		}
//		if (authorizationsServices().canDelete(user, record)) {
//			results.add(Role.DELETE);
//		}

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
		return true;
	}

	@Override
	public boolean seeAccessField() {
		return false;
	}
}
