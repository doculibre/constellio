package com.constellio.model.entities.batchprocess;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.utils.Parametrized;

public interface BatchProcessAction extends Parametrized {

	Transaction execute(List<Record> batch, User user, MetadataSchemaTypes schemaTypes, RecordProvider recordProvider, ModelLayerFactory modelLayerFactory);

}
