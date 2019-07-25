package com.constellio.app.ui.framework.buttons;

import com.vaadin.server.Resource;
import com.vaadin.ui.UI;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class ConfirmDialogButton extends IconButton {

	public static enum DialogMode {
		TEXT, INFO, WARNING, ERROR, STOP
	}

	private DialogMode dialogMode = DialogMode.TEXT;

	public ConfirmDialogButton(String caption) {
		this(null, caption, false);
	}

	public ConfirmDialogButton(Resource iconResource, String caption) {
		super(iconResource, caption);
	}

	public ConfirmDialogButton(Resource iconResource, String caption, boolean iconOnly) {
		super(iconResource, caption, iconOnly);
	}

	public DialogMode getDialogMode() {
		return dialogMode;
	}

	public void setDialogMode(DialogMode dialogMode) {
		this.dialogMode = dialogMode;
	}

	@Override
	protected final void buttonClick(ClickEvent event) {
		showDialog(dialogMode,
				getConfirmDialogTitle(),
				getConfirmDialogMessage(),
				getConfirmDialogOKCaption(),
				getConfirmDialogNotOkCaption(),
				getConfirmDialogCancelCaption(),
				new ConfirmDialog.Listener() {
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							confirmButtonClick(dialog);
						} else {
							dialogClosedWitoutConfirm(dialog);
						}
					}
				});
	}

	public static void showDialog(DialogMode dialogMode, String title, String message, String okCaption,
								  String notOkCaption,
								  String cancelCaption, ConfirmDialog.Listener closeListener) {
		String iconName;
		if (dialogMode != null) {
			switch (dialogMode) {
				case INFO:
					iconName = "info";
					break;
				case WARNING:
					iconName = "warn";
					break;
				case ERROR:
					iconName = "error";
					break;
				case STOP:
					iconName = "stop";
					break;
				case TEXT:
				default:
					iconName = null;
					break;
			}
		} else {
			iconName = null;
		}

		if (iconName != null) {
			StringBuilder html = new StringBuilder();
			html.append("<span style=\"height: 60px;\" class=\"confirm-dialog-" + iconName + "\">");
			html.append("<span class=\"confirm-dialog-message\">");
			html.append(message);
			html.append("</span>");
			html.append("</span>");
			message = html.toString();
		}

		if (notOkCaption != null) {
			ConfirmDialog.show(
					UI.getCurrent(),
					title,
					message,
					okCaption,
					cancelCaption,
					notOkCaption,
					closeListener);
		} else {
			ConfirmDialog.show(
					UI.getCurrent(),
					title,
					message,
					okCaption,
					cancelCaption,
					closeListener);
		}
	}

	protected String getConfirmDialogTitle() {
		return $("ConfirmDialog.title");
	}

	protected String getConfirmDialogOKCaption() {
		return $("ConfirmDialog.yes");
	}

	protected String getConfirmDialogCancelCaption() {
		return $("ConfirmDialog.no");
	}

	protected String getConfirmDialogNotOkCaption() {
		return null;
	}

	protected void dialogClosedWitoutConfirm(ConfirmDialog dialog) {
	}

	protected abstract String getConfirmDialogMessage();

	protected abstract void confirmButtonClick(ConfirmDialog dialog);

}
