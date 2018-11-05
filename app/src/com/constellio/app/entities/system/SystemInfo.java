package com.constellio.app.entities.system;

import com.constellio.app.api.admin.services.SystemAnalysisUtils;

public class SystemInfo {

	public static SystemInfo build() {
		SystemMemory systemMemory = SystemMemory.fetchSystemInfos();

		return new SystemInfo();
	}
}
