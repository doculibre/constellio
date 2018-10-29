package com.constellio.model.entities.security;

import com.constellio.data.utils.Provider;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordProvider;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class SecurityModelUtils {

	public static List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity,
			RecordProvider recordProvider,
			SecurityModel securityModel,
			Provider<String, SecurityModelAuthorization> authProvider) {

		List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();

		for (Metadata metadata : metadatasProvidingSecurity.getAvailableMetadatasWithAValue()) {
			List<String> ids;
			if (metadata.isMultivalue()) {
				ids = metadatasProvidingSecurity.getValue(metadata);
			} else {
				ids = singletonList(metadatasProvidingSecurity.<String>getValue(metadata));
			}

			for (String aReferenceId : ids) {
				Record record = recordProvider.getRecord(aReferenceId);

				returnedAuths.addAll(securityModel.getAuthorizationsOnTarget(aReferenceId));

//				for (String authId : record.<String>getValues(Schemas.AUTHORIZATIONS)) {
				//					returnedAuths.add(authProvider.get(authId));
				//				}
			}
		}

		return returnedAuths;
	}


}
