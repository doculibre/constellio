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

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

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
		return presenterService().getRecordVO(recordId, VIEW_MODE.DISPLAY);
	}

	public void authorizationCreationRequested(AuthorizationVO authorizationVO) {
		Authorization authorization = toAuthorization(authorizationVO);
		authorizationsServices().add(authorization, getCurrentUser());
		view.showMessage($("ShareContentView.shared"));
		view.returnFromPage();
	}

	public void backButtonClicked(String schemaCode) {
		if (schemaCode.startsWith(Folder.SCHEMA_TYPE)) {
			view.navigateTo().displayFolder(recordId);
		} else if (schemaCode.startsWith(Document.SCHEMA_TYPE)) {
			view.navigateTo().displayDocument(recordId);
		}
	}

	public List<String> getAllowedAccesses() {
		List<String> results = new ArrayList<>(3);

		User user = getCurrentUser();
		Record record = presenterService().getRecord(recordId);
		if (authorizationsServices().canRead(user, record)) {
			results.add(Role.READ);
		}
		if (authorizationsServices().canWrite(user, record)) {
			results.add(Role.WRITE);
		}
		if (authorizationsServices().canDelete(user, record)) {
			results.add(Role.DELETE);
		}

		return results;
	}

	public List<String> getAllowedRoles() {
		return getCurrentUser().getUserRoles();
	}

	public boolean seeRolesField() {
		return new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager(), collection)
				.seeUserRolesInAuthorizations();
	}

	private Authorization toAuthorization(AuthorizationVO authorizationVO) {
		String code = authorizationVO.getAuthId();
		AuthorizationDetails details;
		if (StringUtils.isBlank(code)) {
			code = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator().next();

			ArrayList<String> roles = new ArrayList<>();
			roles.addAll(authorizationVO.getAccessRoles());

			for (String roleCode : authorizationVO.getUserRoles()) {
				roles.add(roleCode);
			}

			details = AuthorizationDetails.create(
					code, roles, authorizationVO.getStartDate(), authorizationVO.getEndDate(), view.getCollection());
		} else {
			details = modelLayerFactory.getAuthorizationDetailsManager().get(view.getCollection(), code);
		}

		List<String> principals = new ArrayList<>();
		principals.addAll(authorizationVO.getUsers());
		principals.addAll(authorizationVO.getGroups());

		return new Authorization(details, principals, authorizationVO.getRecords());
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
