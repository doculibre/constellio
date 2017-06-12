package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.themes.ValoTheme;

public class AddToOrRemoveFromSelectionButton extends BaseButton {
	
	private String recordId;
	
	private String schemaTypeCode;

	public AddToOrRemoveFromSelectionButton(RecordVO recordVO) {
		this(recordVO.getId(), recordVO.getSchema().getTypeCode());
	}

	public AddToOrRemoveFromSelectionButton(String recordId, String schemaTypeCode) {
		super($("addToOrRemoveFromSelection"));
		this.recordId = recordId;
		this.schemaTypeCode = schemaTypeCode;
		addStyleName(ValoTheme.BUTTON_LINK);
	}

	@Override
	protected void buttonClick(ClickEvent event) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		if (sessionContext.getSelectedRecordIds().contains(recordId)) {
			sessionContext.removeSelectedRecordId(recordId, schemaTypeCode);
		} else {
			sessionContext.addSelectedRecordId(recordId, schemaTypeCode);
		}
	}

}
