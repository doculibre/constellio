package com.constellio.app.services.schemas.bulkImport;

import com.constellio.data.io.streamFactories.StreamFactory;

import java.io.InputStream;

public interface RecordsImportationBinaryContentProvider {

	StreamFactory<InputStream> get(String key);

}
