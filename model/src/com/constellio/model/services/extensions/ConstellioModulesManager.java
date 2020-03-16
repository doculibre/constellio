package com.constellio.model.services.extensions;

import com.constellio.model.entities.modules.Module;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManagerException.ConstellioModulesManagerException_ModuleInstallationFailed;

import java.util.List;

public interface ConstellioModulesManager {
	Module getInstalledModule(String module);

	boolean isModuleEnabled(String collection, Module module);

	List<? extends Module> getInstalledModules();

	void enableValidModuleAndGetInvalidOnes(String collection, Module module)
			throws ConstellioModulesManagerException_ModuleInstallationFailed;

	List<String> getPermissionGroups(String collection);

	List<String> getPermissionsInGroup(String collection, String group);

	boolean isInstalled(Module module);

	boolean isInstalled(String moduleId);

	void installValidModuleAndGetInvalidOnes(Module module, CollectionsListManager collectionsListManager)
			throws ConstellioModulesManagerException_ModuleInstallationFailed;

	List<? extends Module> getEnabledModules(String collection);

	public List<? extends Module> getAllModules();

	public List<? extends Module> getBuiltinModules();

	<T> Class<T> getModuleClass(String name)
			throws ClassNotFoundException;

	public void initializePluginResources(String collection);

	public void enableComplementaryModules() throws ConstellioModulesManagerException_ModuleInstallationFailed;
}
