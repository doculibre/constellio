package com.constellio.app.modules.rm.services.sip.model;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.List;

public interface SIPObject {

	String getTitle();

	String getType();

	String getId();

	String getZipPath();

	List<Metadata> getMetadataList();

	Record getRecord();
}
