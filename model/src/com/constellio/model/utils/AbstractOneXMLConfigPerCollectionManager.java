package com.constellio.model.utils;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.services.collections.CollectionsListManager;

public abstract class AbstractOneXMLConfigPerCollectionManager<T> implements StatefulService, OneXMLConfigPerCollectionManagerListener<T> {
    protected OneXMLConfigPerCollectionManager<T> oneXMLConfigPerCollectionManager;
    protected ConfigManager configManager;
    protected CollectionsListManager collectionsListManager;
    protected ConstellioCacheManager cacheManager;

    public AbstractOneXMLConfigPerCollectionManager(ConfigManager configManager, CollectionsListManager collectionsListManager, ConstellioCacheManager cacheManager) {
        this.configManager = configManager;
        this.collectionsListManager = collectionsListManager;
        this.cacheManager = cacheManager;
    }

    @Override
    public final void initialize() {
        oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<T>(configManager, collectionsListManager,
                getCollectionFolderRelativeConfigPath(), xmlConfigReader(), this, createConfigAlteration(), getConstellioCache());
    }

    public void createCollection(String collection) {
        oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration());
    }

    public void updateCollection(String collection, DocumentAlteration alteration) {
        oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
    }

    public T getCollection(String collection) {
        return oneXMLConfigPerCollectionManager.get(collection);
    }

    protected abstract String getCollectionFolderRelativeConfigPath();
    protected abstract ConstellioCache getConstellioCache();
    protected abstract XMLConfigReader<T> xmlConfigReader();
    protected abstract DocumentAlteration createConfigAlteration();
}
