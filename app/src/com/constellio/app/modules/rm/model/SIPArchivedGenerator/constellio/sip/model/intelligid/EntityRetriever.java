package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.intelligid;

import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.io.InputStream;

public class EntityRetriever {
    private String collection;

    private ModelLayerFactory modelLayerFactory;

    public EntityRetriever(String collection, ModelLayerFactory modelLayerFactory) {
        this.collection = collection;
        this.modelLayerFactory = modelLayerFactory;
    }

    public InputStream getContentFromHash(String hash) {
       return modelLayerFactory.getContentManager().getContentInputStream(hash, "plugin031.entityRetreiver");
    }

    public File newTempFile(){
        return modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFile("sipfile");
    }
}
