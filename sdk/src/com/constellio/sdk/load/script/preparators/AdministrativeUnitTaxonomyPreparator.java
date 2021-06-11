package com.constellio.sdk.load.script.preparators;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.sdk.load.script.PrincipalTaxonomyPreparator;
import com.constellio.sdk.load.script.utils.LinkableIdsList;

import java.util.ArrayList;
import java.util.List;

public class AdministrativeUnitTaxonomyPreparator extends BaseTaxonomyPreparator implements PrincipalTaxonomyPreparator {

	LinkableIdsList ids = new LinkableIdsList();

	@Override
	protected RecordWrapper newConceptWithCodeAndParent(RMSchemasRecordsServices rm, String code,
														RecordWrapper parent) {
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
	public List<AuthorizationAddRequest> setupAuthorizations(RMSchemasRecordsServices rm, RecordWrapper unit,
															 LinkableIdsList users,
															 LinkableIdsList groups) {
		List<AuthorizationAddRequest> authorizations = new ArrayList<>();
		authorizations.add(rm.newAuthorizationAddRequest().forPrincipalsIds(groups.next()).on(unit).givingReadWriteAccess());
		return authorizations;
	}

}
