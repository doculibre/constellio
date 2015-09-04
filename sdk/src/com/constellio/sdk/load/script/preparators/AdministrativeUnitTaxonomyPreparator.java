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
package com.constellio.sdk.load.script.preparators;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.security.Authorization;
import com.constellio.sdk.load.script.PrincipalTaxonomyPreparator;
import com.constellio.sdk.load.script.utils.LinkableIdsList;

public class AdministrativeUnitTaxonomyPreparator extends BaseTaxonomyPreparator implements PrincipalTaxonomyPreparator {

	LinkableIdsList ids = new LinkableIdsList();

	@Override
	protected RecordWrapper newConceptWithCodeAndParent(RMSchemasRecordsServices rm, String code, RecordWrapper parent) {
		String title = "Administrative unit '" + code + "'";
		return ids.attach(rm.newAdministrativeUnit().setCode(code).setTitle(title).setParent((AdministrativeUnit) parent));
	}

	@Override
	public void init(RMSchemasRecordsServices rm, Transaction transaction) {

	}

	@Override
	public void attach(RMSchemasRecordsServices rm, Record record) {
		if (record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
			rm.wrapFolder(record).setAdministrativeUnitEntered(ids.next());
		}
	}

	@Override
	public List<Authorization> setupAuthorizations(RMSchemasRecordsServices rm, RecordWrapper unit, LinkableIdsList users,
			LinkableIdsList groups) {
		List<Authorization> authorizations = new ArrayList<>();
		authorizations.add(rm.newAuthorization().forPrincipalsIds(groups.next()).on(unit).givingReadWriteAccess());
		return authorizations;
	}

}
