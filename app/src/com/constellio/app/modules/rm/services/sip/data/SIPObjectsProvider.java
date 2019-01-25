package com.constellio.app.modules.rm.services.sip.data;

import com.constellio.app.modules.rm.services.sip.model.SIPObject;
import com.constellio.app.services.factories.AppLayerFactory;

import java.util.List;
import java.util.Map;

public interface SIPObjectsProvider {

	int getStartIndex();

	List<SIPObject> list();

	List<String> getMetadataIds(SIPObject sipObject);

	List<String> getMetadataValues(SIPObject sipObject, String metadataId);

	Map<String, byte[]> getExtraFiles(SIPObject sipObject);

	String getCollection();

	AppLayerFactory getAppLayerCollection();

}
