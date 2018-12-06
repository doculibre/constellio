package com.constellio.model.entities.security;

import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.schemas.Metadata;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class SecurityModelUtils {

	public static List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity,
			SecurityModel securityModel) {

		List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();

		for (Metadata metadata : metadatasProvidingSecurity.getAvailableMetadatasWithAValue()) {
			List<String> ids;
			if (metadata.isMultivalue()) {
				ids = metadatasProvidingSecurity.getValue(metadata);
			} else {
				ids = singletonList(metadatasProvidingSecurity.<String>getValue(metadata));
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
