package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;

public class ListContentAccessAndRoleAuthorizationsPresenter extends ListContentAccessAuthorizationsPresenter {

	private boolean isCurrentlyViewingAccesses = true;

	public ListContentAccessAndRoleAuthorizationsPresenter(ListContentAccessAndRoleAuthorizationsView view) {
		super(view);
	}

	@Override
	public boolean seeRolesField() {
		return !isCurrentlyViewingAccesses;
	}

	@Override
	public boolean seeAccessField() {
		return isCurrentlyViewingAccesses;
	}

	public void viewAccesses() {
		isCurrentlyViewingAccesses = true;
	}

	public void viewRoles() {
		isCurrentlyViewingAccesses = false;
	}

	public boolean isCurrentlyViewingAccesses() {
		return isCurrentlyViewingAccesses;
	}
}
