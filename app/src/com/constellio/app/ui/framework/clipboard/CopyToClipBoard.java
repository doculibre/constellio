package com.constellio.app.ui.framework.clipboard;

import com.constellio.app.ui.framework.window.ConsultLinkWindow.ConsultLinkParams;
import com.constellio.app.ui.util.JavascriptUtils;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class CopyToClipBoard {

	public static void copyConsultationLinkToClipBoard(List<ConsultLinkParams> linkToDisplayList) {
		StringBuilder toCopyToClipboard = new StringBuilder();

		boolean moreThenOneSelected = linkToDisplayList.size() > 1;
		for (ConsultLinkParams linkToDisplay : linkToDisplayList) {

			if (toCopyToClipboard.length() != 0) {
				toCopyToClipboard.append("\\n");
			}

			if (moreThenOneSelected) {
				toCopyToClipboard.append(linkToDisplay.getTitle() + "\\n");
			}
			toCopyToClipboard.append(linkToDisplay.getLink() + "\\n");
		}

		int clipboardStrLenght = toCopyToClipboard.length();
		if (clipboardStrLenght > 0) {
			toCopyToClipboard.delete(clipboardStrLenght - 2, clipboardStrLenght);
		}

		copyToClipBoard(toCopyToClipboard.toString());
	}

	public static void copyToClipBoard(String text) {
		copyToClipBoard(text, $("consultationWindow.copyToClipboard.success"));
	}

	public static void copyToClipBoard(String text, String message) {

		JavascriptUtils.loadScript("clipboard/clipboard.min.js");

		String toExecute = "text=\"" + text + "\";";
		JavaScript.eval(toExecute);

		JavascriptUtils.loadScript("clipboard/constellio-clipboard.js");

		if (StringUtils.isNotBlank(message)) {
			Notification.show(message);
		}
	}

}

