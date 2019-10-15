package com.constellio.app.ui.framework.window;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseLabel;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.jsclipboard.JSClipboard;
import com.vaadin.jsclipboard.JSClipboardButton;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConsultLinkWindow extends BaseWindow {

	public ConsultLinkWindow(List<ConsultLinkParams> linkToDisplayList) {
		addStyleName("consultation-link-window");
		setModal(true);
		setWidth("700px");
		setCaption($("consultationLink"));

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addStyleName("consultation-link-window-layout");
		mainLayout.setSpacing(true);
		mainLayout.addStyleName(WindowButton.WINDOW_CONTENT_STYLE_NAME);

		StringBuilder toCopyToClipboard = new StringBuilder();

		for (ConsultLinkParams linkToDisplay : linkToDisplayList) {
			Label linkLabel = new BaseLabel(linkToDisplay.title + ":" + "<br>" + linkToDisplay.link, ContentMode.HTML);
			linkLabel.addStyleName("consultation-link-window-link");
			mainLayout.addComponent(linkLabel);

			if (toCopyToClipboard.length() != 0) {
				toCopyToClipboard.append("\n");
			}
			toCopyToClipboard.append(linkToDisplay.title + "\n");
			toCopyToClipboard.append(linkToDisplay.link + "\n");
		}

		int clipboardStrLenght = toCopyToClipboard.length();
		if (clipboardStrLenght > 0) {
			toCopyToClipboard.delete(clipboardStrLenght - 1, clipboardStrLenght);
		}

		JSClipboardButton copyToClipboardButton = new JSClipboardButton(mainLayout, $("consultationWindow.copyToClipboard"));
		copyToClipboardButton.addStyleName("clipboard-button");
		copyToClipboardButton.setClipboardText(toCopyToClipboard.toString());
		copyToClipboardButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		copyToClipboardButton.addSuccessListener(new JSClipboard.SuccessListener() {
			@Override
			public void onSuccess() {
				Notification.show($("consultationWindow.copyToClipboard.success"));
			}
		});
		copyToClipboardButton.addErrorListener(new JSClipboard.ErrorListener() {
			@Override
			public void onError() {
				Notification.show($("consultationWindow.copyToClipboard.error"), Notification.Type.ERROR_MESSAGE);
			}
		});
		mainLayout.addComponent(copyToClipboardButton);
		mainLayout.setComponentAlignment(copyToClipboardButton, Alignment.TOP_CENTER);

		this.setContent(mainLayout);
	}


	@Getter
	@AllArgsConstructor
	public static class ConsultLinkParams {
		String link;
		String title;
	}


}
