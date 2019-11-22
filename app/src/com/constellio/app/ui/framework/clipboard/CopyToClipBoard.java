package com.constellio.app.ui.framework.clipboard;

import com.constellio.app.ui.framework.window.ConsultLinkWindow.ConsultLinkParams;
import com.constellio.app.ui.util.JavascriptUtils;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CopyToClipBoard {

	public static void copyConsultationLinkToClipBoard(List<ConsultLinkParams> linkToDisplayList) {
		StringBuilder toCopyToClipboard = new StringBuilder();


		for (ConsultLinkParams linkToDisplay : linkToDisplayList) {

			if (toCopyToClipboard.length() != 0) {
				toCopyToClipboard.append("\\n");
			}
			toCopyToClipboard.append(linkToDisplay.getTitle() + "\\n");
			toCopyToClipboard.append(linkToDisplay.getLink() + "\\n");
		}

		int clipboardStrLenght = toCopyToClipboard.length();
		if (clipboardStrLenght > 0) {
			toCopyToClipboard.delete(clipboardStrLenght - 2, clipboardStrLenght);
		}

		copyToClipBoard(toCopyToClipboard.toString());
	}

	public static void copyToClipBoard(String text) {

		JavascriptUtils.loadScript("jquery/jquery-2.1.4.min.js");
		JavascriptUtils.loadScript("clipboard/clipboard.min.js");

		String toExecute = "text=\"" + text + "\";";
		JavaScript.eval(toExecute);

		JavascriptUtils.loadScript("clipboard/constellio-clipboard.js");

		Notification.show($("consultationWindow.copyToClipboard.success"));

	}

}

