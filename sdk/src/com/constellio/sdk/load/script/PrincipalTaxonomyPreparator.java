package com.constellio.sdk.load.script;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.sdk.load.script.utils.LinkableIdsList;

import java.util.List;

public interface PrincipalTaxonomyPreparator extends TaxonomyPreparator {

	List<AuthorizationAddRequest> setupAuthorizations(RMSchemasRecordsServices rm, RecordWrapper record,
													  LinkableIdsList users,
													  LinkableIdsList groups);

}
