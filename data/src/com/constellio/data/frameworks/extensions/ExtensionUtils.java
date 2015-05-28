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
package com.constellio.data.frameworks.extensions;

public class ExtensionUtils {

	public static <T> Boolean getBooleanValue(VaultBehaviorsList<T> behaviors, Boolean defaultValue,
			BehaviorCaller<T, ExtensionBooleanResult> caller) {

		Boolean value = defaultValue;
		boolean forced = false;
		for (T behavior : behaviors.getBehaviors()) {
			ExtensionBooleanResult behaviorValue = caller.call(behavior);

			if (value == null && behaviorValue == ExtensionBooleanResult.TRUE) {
				value = true;

			} else if (behaviorValue == ExtensionBooleanResult.FALSE) {
				value = false;

			} else if (behaviorValue == ExtensionBooleanResult.FORCE_TRUE) {
				forced = true;
			}
		}

		if (forced) {
			return true;
		} else {
			return value;
		}
	}

	public static interface BehaviorCaller<T, O> {

		O call(T behavior);

	}
}
