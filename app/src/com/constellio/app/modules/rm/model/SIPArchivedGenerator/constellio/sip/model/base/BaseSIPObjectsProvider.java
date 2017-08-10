package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.base;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.data.SIPObjectsProvider;
import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPObject;

import java.util.List;

public abstract class BaseSIPObjectsProvider implements SIPObjectsProvider {

	private List<SIPObject> metsObjects;
	
	public BaseSIPObjectsProvider(List<SIPObject> metsObjects) {
		this.metsObjects = metsObjects;
	}

	@Override
	public List<SIPObject> list() {
		return metsObjects;
	}

}
