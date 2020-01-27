package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddToOrRemoveFromSelectionButton extends BaseButton {

	private String recordId;

	private String schemaTypeCode;

	//public static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");
	public static final Resource SELECTION_ICON_RESOURCE = FontAwesome.SHOPPING_BASKET;

	public AddToOrRemoveFromSelectionButton(RecordVO recordVO, boolean isAlreadySelected) {
		this(recordVO.getId(), recordVO.getSchema().getTypeCode(), isAlreadySelected);
	}

	public AddToOrRemoveFromSelectionButton(String recordId, String schemaTypeCode, boolean isAlreadySelected) {
		super($("addToOrRemoveFromSelection.add"));
		if (isAlreadySelected) {
			this.setCaption($("addToOrRemoveFromSelection.remove"));
		} else {
			this.setCaption($("addToOrRemoveFromSelection.add"));
		}
		setIcon(SELECTION_ICON_RESOURCE);
		this.recordId = recordId;
		this.schemaTypeCode = schemaTypeCode;
		addStyleName(ValoTheme.BUTTON_LINK);
	}

	@Override
	protected void buttonClick(ClickEvent event) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();

		if (sessionContext.getSelectedRecordIds().contains(recordId)) {
			this.setCaption($("addToOrRemoveFromSelection.add"));
			sessionContext.removeSelectedRecordId(recordId, schemaTypeCode);
		} else {
			this.setCaption($("addToOrRemoveFromSelection.remove"));
			sessionContext.addSelectedRecordId(recordId, schemaTypeCode);
		}
	}

}
