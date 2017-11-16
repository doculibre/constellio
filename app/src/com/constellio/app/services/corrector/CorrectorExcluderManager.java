package com.constellio.app.services.corrector;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;

import java.util.List;

public class CorrectorExcluderManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<List<CorrectorExclusion>> {

    private static String EXCLUSION_CONFIG = "/exclusion.xml";
    private OneXMLConfigPerCollectionManager<List<CorrectorExclusion>> oneXMLConfigPerCollectionManager;
    private ConfigManager configManager;
    private CollectionsListManager collectionsListManager;
    private ModelLayerFactory modelLayerFactory;
    private ConstellioCacheManager cacheManager;


    public CorrectorExcluderManager(ModelLayerFactory modelLayerFactory) {
        this.configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
        this.modelLayerFactory = modelLayerFactory;
        this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
        this.cacheManager = modelLayerFactory.getDataLayerFactory().getSettingsCacheManager();
    }

    @Override
    public void initialize() {
        ConstellioCache cache = cacheManager.getCache(CorrectorExcluderManager.class.getName());
        this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
                EXCLUSION_CONFIG, xmlConfigReader(), this, cache);
    }

    @Override
    public void close() {

    }

    private XMLConfigReader<List<CorrectorExclusion>> xmlConfigReader() {
        return new XMLConfigReader<List<CorrectorExclusion>>() {
            @Override
            public List<CorrectorExclusion> read(String collection, Document document) {
                return newExclusionReader(document).getAllCorrection();
            }

            public List<CorrectorExclusion> getAllExclusion(String collection) {
                return oneXMLConfigPerCollectionManager.get(collection);
            }

            public void addExclusion(final CorrectorExclusion exclusion) {
                DocumentAlteration alteration = new DocumentAlteration() {
                    @Override
                    public void alter(Document document) {
                        CorrectorExcluderWriter writer = newExclusionWriter(document);
                        writer.addExclusion(exclusion);
                    }
                };
                oneXMLConfigPerCollectionManager.updateXML(exclusion.getCollection(), alteration);
            }

            public void deleteException(final CorrectorExclusion correctorExclusion)
                    throws RolesManagerRuntimeException {
                DocumentAlteration alteration = new DocumentAlteration() {
                    @Override
                    public void alter(Document document) {
                        CorrectorExcluderWriter writer = newExclusionWriter(document);
                        writer.deleteExclusion(correctorExclusion);
                    }
                };
                oneXMLConfigPerCollectionManager.updateXML(correctorExclusion.getCollection(), alteration);
            }

            public void updateException(final CorrectorExclusion correctorExclusion, final CorrectorExclusion oldCorrectionExclusion)
                    throws RolesManagerRuntimeException {
                DocumentAlteration alteration = new DocumentAlteration() {
                    @Override
                    public void alter(Document document) {
                        CorrectorExcluderWriter writer = newExclusionWriter(document);
                        writer.updateExclusion(correctorExclusion, oldCorrectionExclusion);
                    }
                };
                oneXMLConfigPerCollectionManager.updateXML(correctorExclusion.getCollection(), alteration);
            }

            public void createCollectionExclusion(String collection) {
                DocumentAlteration createConfigAlteration = new DocumentAlteration() {
                    @Override
                    public void alter(Document document) {
                        CorrectorExcluderWriter writer = newExclusionWriter(document);
                        writer.createEmptyExceltion();
                    }
                };
                oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
            }

            private CorrectorExcluderWriter newExclusionWriter(Document document) {
                return new CorrectorExcluderWriter(document);
            }

            private CorrectorExcluderReader newExclusionReader(Document document) {
                return new CorrectorExcluderReader(document);
            }
        };
    }

    @Override
    public void onValueModified(String collection, List<CorrectorExclusion> newValue) {

    }
}
