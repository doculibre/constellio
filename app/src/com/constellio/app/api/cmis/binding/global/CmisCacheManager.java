package com.constellio.app.api.cmis.binding.global;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ObjectNotFound;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepositoryInfoManager;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionTypeDefinitionsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.collections.CollectionsListManagerListener;

/**
 * This class centralize cmis service and repository caches
 */
public class CmisCacheManager implements StatefulService, CollectionsListManagerListener {

	private ThreadLocal<CallContextAwareCmisService> callContextAwareCmisServiceThreadLocal = new ThreadLocal<CallContextAwareCmisService>();

	private final Map<String, ConstellioCollectionRepository> repositories = new HashMap<String, ConstellioCollectionRepository>();

	private AppLayerFactory appLayerFactory;

	public CmisCacheManager(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void initialize() {
		addCollectionsToRepository();

		appLayerFactory.getModelLayerFactory().getCollectionsListManager().registerCollectionsListener(this);
	}

	public ConstellioCollectionRepository getRepository(String collection) {
		return repositories.get(collection);
	}

	public Collection<ConstellioCollectionRepository> getRepositories() {
		return repositories.values();
	}

	public ConstellioCollectionRepository getCollectionRepository(String repositoryId) {
		ConstellioCollectionRepository result = repositories.get(repositoryId);
		if (result == null) {
			throw new CmisExceptions_ObjectNotFound("collection repository", repositoryId);
		}

		return result;
	}

	@Override
	public void onCollectionCreated(String collection) {
		addCollectionsToRepository(collection);
	}

	@Override
	public void onCollectionDeleted(String collection) {
		repositories.remove(collection);
	}

	private void addCollectionsToRepository() {
		CollectionsListManager collectionsListManager = appLayerFactory.getModelLayerFactory().getCollectionsListManager();
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			addCollectionsToRepository(collection);
		}
	}

	private void addCollectionsToRepository(String collection) {
		File zeFileSystemRootDir = new File("/home/developer/git/constellio-dev/zeRepository");
		ConstellioCollectionTypeDefinitionsManager typeManager = new ConstellioCollectionTypeDefinitionsManager(
				appLayerFactory.getModelLayerFactory(), collection);
		ConstellioCollectionRepositoryInfoManager repositoryInfoManager = new ConstellioCollectionRepositoryInfoManager(
				collection, appLayerFactory.newMigrationServices().getCurrentVersion(collection));
		ConstellioCollectionRepository zeCollectionRepository = new ConstellioCollectionRepository(collection,
				zeFileSystemRootDir, typeManager,
				repositoryInfoManager);
		repositories.put(collection, zeCollectionRepository);
	}

	public ThreadLocal<CallContextAwareCmisService> getCallContextAwareCmisServiceThreadLocal() {
		return callContextAwareCmisServiceThreadLocal;
	}

	@Override
	public void close() {

	}

}