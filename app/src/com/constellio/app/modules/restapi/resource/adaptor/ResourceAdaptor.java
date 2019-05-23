package com.constellio.app.modules.restapi.resource.adaptor;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;

import java.util.Set;

public abstract class ResourceAdaptor<T> {

	abstract public T adapt(T resource, Record record, MetadataSchema schema, boolean modified,
							Set<String> filters);

}
