package com.constellio.app.ui.framework.components.contextmenu;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;

import com.vaadin.ui.UI;

public abstract class ConfirmDialogContextMenuItemClickListener implements BaseContextMenuItemClickListener {

	@Override
	public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
		ConfirmDialog.show(
				UI.getCurrent(),
				getConfirmDialogTitle(),
				getConfirmDialogMessage(),
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
