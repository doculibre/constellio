package com.constellio.app.modules.rm.services.sip.data;

import com.constellio.app.modules.rm.services.sip.model.SIPObject;

import java.util.List;
import java.util.Map;

public interface SIPObjectsProvider {

	int getStartIndex();

	List<SIPObject> list();

	Map<String, byte[]> getExtraFiles(SIPObject sipObject);

}
