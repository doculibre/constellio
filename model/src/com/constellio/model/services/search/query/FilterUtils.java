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
package com.constellio.model.services.search.query;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.users.UserServices;

public class FilterUtils {

	public static String multiCollectionUserReadFilter(UserCredential user, UserServices userServices) {
		StringBuilder filter = new StringBuilder();
		if (user.getCollections().isEmpty()) {
			addTokenA38(filter);
		} else {
			filter.append("(");
			boolean firstCollection = true;
			for (String collection : user.getCollections()) {
				if (!firstCollection) {
					filter.append(" OR ");
				}
				firstCollection = false;
				User userInCollection = userServices.getUserInCollection(user.getUsername(), collection);
				filter.append("(");
				filter.append(userReadFilter(userInCollection));
				filter.append(")");

			}
			filter.append(")");
		}
		return filter.toString();
	}

	public static String userWriteFilter(User user) {
		StringBuilder stringBuilder = new StringBuilder();

		addTokenA38(stringBuilder);
		if (user.hasCollectionWriteAccess()) {
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(user.getCollection());
		}
		for (String token : user.getUserTokens()) {
			if (token.charAt(0) == 'w') {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(token);
			}
		}
		return stringBuilder.toString();
	}

	public static String userReadFilter(User user) {
		StringBuilder stringBuilder = new StringBuilder();

		addTokenA38(stringBuilder);

		if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(user.getCollection());
		}

		for (String token : user.getUserTokens()) {
			if (token.charAt(0) == 'r') {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(token);
			}
		}
		return stringBuilder.toString();
	}

	public static String userDeleteFilter(User user) {
		StringBuilder stringBuilder = new StringBuilder();

		addTokenA38(stringBuilder);

		if (user.hasCollectionDeleteAccess()) {
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(user.getCollection());
		}

		for (String token : user.getUserTokens()) {

			if (token.charAt(0) == 'd') {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(token);
			}
		}
		return stringBuilder.toString();
	}

	public static String statusFilter(StatusFilter status) {
		if (status == StatusFilter.ACTIVES) {
			return "deleted_s:__FALSE__ OR deleted_s:__NULL__";
		} else if (status == StatusFilter.DELETED) {
			return "deleted_s:__TRUE__";
		} else {
			return null;
		}
	}

	private static void addTokenA38(StringBuilder stringBuilder) {
		stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
		stringBuilder.append(":");
		stringBuilder.append("A38");
	}
}
