package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model;

import java.util.List;

public interface SIPObject {
	
	String getId();
	
	String getType();
	
	String getTitle();
	
	List<String> getMetadataIds();
	
	String getMetadataLabel(String metadataId);
	
	String getMetadataValue(String metadataId);

	List<String> getMetadataValues(String metadataId);
	
	String getZipPath();

}
