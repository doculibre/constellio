package com.constellio.app.extensions.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

public class InstallableModuleExtension {

	public void moduleStarted(ModuleStartedEvent event) {
	}

	@AllArgsConstructor
	@Getter
	public static class ModuleStartedEvent {
		private String moduleId;
		private Set<String> previouslyStartedModuleIds;
	}

}
