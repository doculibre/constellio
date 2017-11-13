package com.constellio.app.ui.framework.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.extensions.records.params.BuildRecordVOParams;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.data.event.EventTypeUtils;
import com.constellio.app.ui.framework.data.event.UnsupportedEventTypeRuntimeException;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;

/**
 * Created by Constellio on 2016-12-13.
 */
public class EventToVOBuilder extends RecordToVOBuilder {
	
	private static Logger LOGGER = LoggerFactory.getLogger(EventToVOBuilder.class);

    @Override
    public RecordVO build(Record record, RecordVO.VIEW_MODE viewMode, MetadataSchemaVO schemaVO,
                          SessionContext sessionContext) {
        String id = record.getId();
        String schemaCode = record.getSchemaCode();
        String collection = record.getCollection();
        boolean saved = record.isSaved();

        ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
        ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        MetadataSchema schema = metadataSchemasManager.getSchemaTypes(collection).getSchema(schemaCode);

        if (schemaVO == null) {
            schemaVO = new MetadataSchemaToVOBuilder().build(schema, viewMode, sessionContext);
        }

        ContentVersionToVOBuilder contentVersionVOBuilder = new ContentVersionToVOBuilder(modelLayerFactory);

        List<MetadataValueVO> metadataValueVOs = new ArrayList<MetadataValueVO>();
        List<MetadataVO> metadatas = schemaVO.getMetadatas();
        for (MetadataVO metadataVO : metadatas) {
            String metadataCode = metadataVO.getCode();
            Metadata metadata = schema.getMetadata(metadataCode);

            Object recordVOValue = getValue(record, metadata);
            if((Event.DEFAULT_SCHEMA + "_" + Event.TYPE).equals(metadataCode)) {
            	String eventType = recordVOValue.toString();
            	try {
            		recordVOValue = EventTypeUtils.getEventTypeCaption(eventType);
        		} catch (UnsupportedEventTypeRuntimeException e) {
        			LOGGER.error("Error while retrieving event type caption", e);
        			recordVOValue = eventType;
        		}
            } else if (recordVOValue instanceof Content) {
                recordVOValue = contentVersionVOBuilder.build((Content) recordVOValue, sessionContext);
            } else if (recordVOValue instanceof List) {
                List<Object> listRecordVOValue = new ArrayList<Object>();
                List<Object> listRecordValue = (List<Object>) recordVOValue;
                for (Iterator<Object> it = listRecordValue.iterator(); it.hasNext(); ) {
                    Object listVOValueElement = it.next();
                    if (listVOValueElement instanceof Content) {
                        listVOValueElement = contentVersionVOBuilder.build((Content) listVOValueElement);
                    }
                    if (listVOValueElement != null) {
                        listRecordVOValue.add(listVOValueElement);
                    }
                }
                recordVOValue = listRecordVOValue;
            }
            MetadataValueVO metadataValueVO = new MetadataValueVO(metadataVO, recordVOValue);
            metadataValueVOs.add(metadataValueVO);
        }

        RecordVO recordVO = newRecordVO(id, metadataValueVOs, viewMode);
        recordVO.setSaved(saved);
		recordVO.setRecord(record);
        BuildRecordVOParams buildRecordVOParams = new BuildRecordVOParams(record, recordVO);
        constellioFactories.getAppLayerFactory().getExtensions()
                .forCollection(record.getCollection()).buildRecordVO(buildRecordVOParams);

        return recordVO;
    }
}
