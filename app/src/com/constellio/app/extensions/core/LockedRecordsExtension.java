package com.constellio.app.extensions.core;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.services.factories.ModelLayerFactory;

/**
 * Created by constellios on 2017-03-30.
 */
public class LockedRecordsExtension extends RecordExtension {

    AppLayerFactory appLayerFactory;
    AppLayerCollectionExtensions collectionExtensions;

    public LockedRecordsExtension(String collection, AppLayerFactory appLayerFactory) {
        this.appLayerFactory = appLayerFactory;
        this.collectionExtensions = appLayerFactory.getExtensions().forCollection(collection);

    }

    @Override
    public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {

        //collectionExtensions.lockedRecords

        if (collectionExtensions.lockedRecords.contains(event.getSchemaTypeCode())) {
            String recordCode = event.getRecord().get(Schemas.CODE);
            if (collectionExtensions.lockedRecords.get(event.getSchemaTypeCode()).contains(recordCode)) {
                //ensure nothing import has been changed

                //Le code et le champ linkedSchema doivent être restés intactes
            }

        }

        super.recordInModificationBeforeSave(event);


    }
}
