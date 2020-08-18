package com.constellio.model.entities.security;

import com.constellio.data.utils.Pair;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.schemas.Metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.singletonList;

public class SecurityModelUtils {

	public static List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity,
			SecurityModel securityModel) {

		List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();

		Iterator<Pair<Metadata, Object>> iterator = metadatasProvidingSecurity.iterateWithValues();

		while (iterator.hasNext()) {
			List<String> ids;
			Pair<Metadata, Object> pair = iterator.next();
			Metadata metadata = pair.getKey();
			if (metadata.isMultivalue()) {
				ids = (List) pair.getValue();
			} else {
				ids = singletonList((String) pair.getValue());
			}

			for (String aReferenceId : ids) {
				returnedAuths.addAll(securityModel.getAuthorizationsOnTarget(aReferenceId));
			}
		}

		return returnedAuths;
	}

	public static boolean hasNegativeAccessOnSecurisedRecord(List<SecurityModelAuthorization> auths) {
		for (SecurityModelAuthorization auth : auths) {
			if (auth.isSecurableRecord() && auth.getDetails().isNegative()) {
				return true;
			}
		}
		return false;
	}

}
