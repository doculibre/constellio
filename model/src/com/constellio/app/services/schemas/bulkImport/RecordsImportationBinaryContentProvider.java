package com.constellio.app.services.schemas.bulkImport;

import java.io.InputStream;

import com.constellio.data.io.streamFactories.StreamFactory;

public interface RecordsImportationBinaryContentProvider {

	StreamFactory<InputStream> get(String key);

}
