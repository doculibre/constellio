package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.intelligid;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.io.File;
import java.io.InputStream;

public class EntityRetriever {
    private String collection;
    private AppLayerFactory appLayerFactory;
    private ModelLayerFactory modelLayerFactory;

    public EntityRetriever(String collection, AppLayerFactory appLayerFactory) {
        this.collection = collection;
        this.appLayerFactory = appLayerFactory;
        this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
    }

    public InputStream getContentFromHash(String hash) {
       return modelLayerFactory.getContentManager().getContentInputStream(hash, "plugin031.entityRetreiver");
    }

    public Folder getFoldersFromString(String id){
        return new RMSchemasRecordsServices(collection, appLayerFactory).getFolder(id);
    }

    public File newTempFile(){
        return modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFile("sipfile");
    }
}
