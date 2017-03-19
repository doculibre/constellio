package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.server.Resource;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public abstract class ConfirmDialogButton extends IconButton {
	
	public static enum DialogMode {
		TEXT, INFO, WARNING, ERROR, STOP
	};

	private static ConfirmDialog.Factory factory = new DefaultConfirmDialogFactory();
	
	private DialogMode dialogMode = DialogMode.TEXT;

	static {
		ConfirmDialog.setFactory(new ConfirmDialog.Factory() {
			@Override
			public ConfirmDialog create(String windowCaption, String message, String okTitle, String cancelTitle,
					String notOKCaption) {
				ConfirmDialog confirmDialog = factory.create(windowCaption, message, okTitle, cancelTitle, notOKCaption);
				confirmDialog.setCaptionAsHtml(true);
				confirmDialog.addAttachListener(new AttachListener() {
					@Override
					public void attach(AttachEvent event) {
						BaseWindow.executeZIndexAdjustJavascript(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
					}
				});
				return confirmDialog;
			}
		});
	}

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
		String dialogMessage = getConfirmDialogMessage();
		String iconName;
		switch (dialogMode) {
		case INFO:
			iconName = "info";
			break;
		case  WARNING:
			iconName = "warn";
			break;
		case  ERROR:
			iconName = "error";
			break;
		case  STOP:
			iconName = "stop";
			break;
		case TEXT:
		default:	
			iconName = null;
			iconName = "info";
			break;
		};
		if (iconName != null) {
			StringBuilder html = new StringBuilder();
			html.append("<span class=\"confirm-dialog-info\">");
			html.append(dialogMessage);
			html.append("</span>");
			dialogMessage = html.toString();
		}
		ConfirmDialog.show(
				UI.getCurrent(),
				getConfirmDialogTitle(),
				dialogMessage,
				getConfirmDialogOKCaption(),
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

	protected String getConfirmDialogTitle() {
		return $("ConfirmDialog.title");
	}

	protected String getConfirmDialogOKCaption() {
		return $("ConfirmDialog.yes");
	}

	protected String getConfirmDialogCancelCaption() {
		return $("ConfirmDialog.no");
	}

	protected void dialogClosedWitoutConfirm(ConfirmDialog dialog) {
	}

	protected abstract String getConfirmDialogMessage();

	protected abstract void confirmButtonClick(ConfirmDialog dialog);

}
