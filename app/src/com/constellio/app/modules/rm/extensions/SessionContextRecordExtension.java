package com.constellio.app.modules.rm.extensions;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.records.Record;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionEvent;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;

public class SessionContextRecordExtension extends RecordExtension {

	@Override
	public void recordLogicallyDeleted(RecordLogicalDeletionEvent event) {
		Record record = event.getRecord();
		try {
			ConstellioUI.getCurrentSessionContext().removeSelectedRecordId(record.getId(), record.getTypeCode());
		} catch (Throwable t) {
			// Ignore
		}
	}

	@Override
	public void recordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {
		Record record = event.getRecord();
		try {
			ConstellioUI.getCurrentSessionContext().removeSelectedRecordId(record.getId(), record.getTypeCode());
		} catch (Throwable t) {
			// Ignore
		}
	}
}
