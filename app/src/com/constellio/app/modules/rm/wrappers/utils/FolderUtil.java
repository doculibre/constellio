package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import org.apache.commons.lang3.StringUtils;

public class FolderUtil {
    public static Folder createCopyFrom(Folder folder) {
        String collection = folder.getCollection();
        Folder copy = createNewFolder(collection);

        for (Metadata metadata : folder.getSchema().getMetadatas()) {
            if (!metadata.isSystemReserved() && metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
                copy.set(metadata.getLocalCode(), folder.get(metadata));
            }
        }

        return copy;
    }

    public static Folder createNewFolder() {
        return createNewFolder(null);
    }

    public static Folder createNewFolder(String collection) {
        ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
        SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
        AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

        if(StringUtils.isBlank(collection)) {
            collection = sessionContext.getCurrentCollection();
        }

        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        SchemaPresenterUtils presenterUtils = new SchemaPresenterUtils(Folder.DEFAULT_SCHEMA, constellioFactories, sessionContext);

        Record record = presenterUtils.newRecord();
        return rm.wrapFolder(record);
    }
}
