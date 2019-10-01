package com.constellio.app.ui.framework.buttons;

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
import org.apache.commons.lang3.StringUtils;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class DeleteWithJustificationButton extends WindowButton {
	public static final String CONFIRM_DELETION = "confirm-deletion";
	public static final String CANCEL_DELETION = "cancel-deletion";
	public static final String DELETION_REASON = "deletion-reason";

	public DeleteWithJustificationButton(String caption, boolean iconOnly) {
		this(caption, iconOnly, WindowConfiguration.modalDialog("35%", "350px"));
	}

	public DeleteWithJustificationButton(String caption, boolean iconOnly, WindowConfiguration windowConfiguration) {
		super((iconOnly ? DeleteButton.ICON_RESOURCE : null),
				DeleteButton.computeCaption(caption, iconOnly), iconOnly, windowConfiguration);
	}

	public DeleteWithJustificationButton(boolean iconOnly) {
		this($(DeleteButton.CAPTION), iconOnly);
	}

	public DeleteWithJustificationButton() {
		this(DeleteButton.CAPTION, true);
	}

	@Override
	protected Component buildWindowContent() {
		VerticalLayout messageLayout = new VerticalLayout();

		messageLayout.addComponent(getMessageComponent());

		final TextArea reason = new BaseTextArea();
		reason.setId(DELETION_REASON);
		reason.setWidth("90%");

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

		if(getRecordCaption() != null) {
			messageLayout.addComponent(getRecordCaption());
		}
		VerticalLayout layout = new VerticalLayout(messageLayout, reason, buttons);
		layout.setComponentAlignment(buttons, Alignment.MIDDLE_CENTER);
		layout.setSpacing(true);
//		layout.setSizeFull();
		return layout;
	}

	protected abstract void deletionConfirmed(String reason);

	protected void deletionCancelled() {
		// Do nothing by default
	}

	public Component getMessageComponent() {
		HorizontalLayout messageLayout = new HorizontalLayout();
		Label message = new Label(getConfirmDialogMessage());
		Label required = new Label(" *");
		required.addStyleName("red-star");

		messageLayout.addComponent(message);
		messageLayout.addComponent(required);

		return messageLayout;
	}

	public Component getRecordCaption() {
		return null;
	}

	protected String getConfirmDialogMessage() {
		if (getRecordCaption() != null) {
			return $("DeleteWithJustificationButton.pleaseConfirmWithRecordCaption", getRecordCaption());
		}
		return $("DeleteWithJustificationButton.pleaseConfirm");
	}
}
