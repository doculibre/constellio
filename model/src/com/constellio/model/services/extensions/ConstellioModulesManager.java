package com.constellio.model.services.extensions;

import java.util.List;
import java.util.Set;

import com.constellio.model.entities.modules.Module;
import com.constellio.model.services.collections.CollectionsListManager;

public interface ConstellioModulesManager {
	Module getInstalledModule(String module);

	boolean isModuleEnabled(String collection, Module module);

	List<? extends Module> getInstalledModules();

	Set<String> enableValidModuleAndGetInvalidOnes(String collection, Module module);

	List<String> getPermissionGroups(String collection);

	List<String> getPermissionsInGroup(String collection, String group);

	boolean isInstalled(Module module);

	Set<String> installValidModuleAndGetInvalidOnes(Module module, CollectionsListManager collectionsListManager);

	List<? extends Module> getEnabledModules(String collection);

	public List<? extends Module> getAllModules();

	public List<? extends Module> getBuiltinModules();

	<T> Class<T> getModuleClass(String name)
			throws ClassNotFoundException;

	public void initializePluginResources(String collection);

	public void enableComplementaryModules();
}
