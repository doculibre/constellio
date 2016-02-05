package com.constellio.data.frameworks.extensions;

public class ExtensionUtils {

	public static <T> Boolean getBooleanValue(VaultBehaviorsList<T> behaviors, Boolean defaultValue,
			BooleanCaller<T> caller) {

		Boolean value = defaultValue;
		boolean forced = false;
		for (T behavior : behaviors) {
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

	public static interface BooleanCaller<T> {

		ExtensionBooleanResult call(T behavior);

	}
}
