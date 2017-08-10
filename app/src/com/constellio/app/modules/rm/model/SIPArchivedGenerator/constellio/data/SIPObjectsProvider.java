package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.data;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.ead.EADArchdesc;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPObject;

import java.util.List;
import java.util.Map;

public interface SIPObjectsProvider {
	
	int getStartIndex();
	
	List<SIPObject> list();
	
	EADArchdesc getEADArchdesc(SIPObject sipObject);
	
	List<String> getMetadataIds(SIPObject sipObject);
	
	List<String> getMetadataValues(SIPObject sipObject, String metadataId);
	
	Map<String, byte[]> getExtraFiles(SIPObject sipObject);

}
