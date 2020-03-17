package com.constellio.app.modules.rm.ui.pages.trigger;

import com.constellio.app.modules.rm.ui.field.TableAddRemoveTriggerActionField;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;

public abstract class TriggerRecordForm extends RecordForm {
	public TriggerRecordForm(RecordVO record, ConstellioFactories constellioFactories) {
		super(record, new TriggerFieldFactory(constellioFactories, record), constellioFactories);
	}

	public TableAddRemoveTriggerActionField getTriggerActionField() {
		return (TableAddRemoveTriggerActionField) getField(Trigger.ACTIONS);
	}
}
