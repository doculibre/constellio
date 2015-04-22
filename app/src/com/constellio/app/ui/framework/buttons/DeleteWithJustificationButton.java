/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class DeleteWithJustificationButton extends WindowButton {
	public static final String CONFIRM_DELETION = "confirm-deletion";
	public static final String CANCEL_DELETION = "cancel-deletion";
	public static final String DELETION_REASON = "deletion-reason";

	public DeleteWithJustificationButton(String caption, boolean iconOnly) {
		super((iconOnly ? DeleteButton.ICON_RESOURCE : null),
				DeleteButton.computeCaption(caption, iconOnly), iconOnly, WindowConfiguration.modalDialog("35%", "30%"));
	}

	public DeleteWithJustificationButton(boolean iconOnly) {
		this(DeleteButton.CAPTION, iconOnly);
	}

	public DeleteWithJustificationButton() {
		this(DeleteButton.CAPTION, true);
	}

	@Override
	protected Component buildWindowContent() {
		Label message = new Label($("DeleteWithJustificationButton.pleaseConfirm"));
		final TextArea reason = new BaseTextArea();
		reason.setId(DELETION_REASON);
		reason.setWidth("90%");
		reason.setHeight("75%");

		final Button confirm = new Button($(DeleteButton.CAPTION));
		confirm.addStyleName(CONFIRM_DELETION);
		confirm.addStyleName(ValoTheme.BUTTON_PRIMARY);
		confirm.setEnabled(false);
		confirm.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
				deletionConfirmed(reason.getValue());
			}
		});

		Button cancel = new Button($("cancel"));
		cancel.addStyleName(CANCEL_DELETION);
		cancel.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				getWindow().close();
				deletionCancelled();
			}
		});

		reason.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				confirm.setEnabled(StringUtils.isNotBlank(event.getText()));
			}
		});

		HorizontalLayout buttons = new HorizontalLayout(confirm, cancel);
		buttons.setSpacing(true);

		VerticalLayout layout = new VerticalLayout(message, reason, buttons);
		layout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
		layout.setSpacing(true);
		return layout;
	}

	protected abstract void deletionConfirmed(String reason);

	protected void deletionCancelled() {
		// Do nothing by default
	}
}
