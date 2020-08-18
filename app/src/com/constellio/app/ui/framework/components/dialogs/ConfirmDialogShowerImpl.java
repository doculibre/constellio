package com.constellio.app.ui.framework.components.dialogs;

import com.vaadin.ui.UI;
import org.jetbrains.annotations.NotNull;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.function.Supplier;


/*
 * Ideas
 * 		showConfirmDialog can return an handle of the dialog with the ability to listen to the close event or even
 * 		close the window from another thread*
 * */
public class ConfirmDialogShowerImpl implements ConfirmDialogShower {
	private final Supplier<UI> getUI;

	public ConfirmDialogShowerImpl(@NotNull final Supplier<UI> getUI) {
		this.getUI = getUI;
	}

	@Override
	public void showConfirmDialog(ConfirmDialogProperties properties) {
		ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
				properties.getTitle(),
				properties.getMessage(),
				properties.getOkCaption(),
				properties.getCancelCaption(),
				properties.getNotOkCaption());

		confirmDialog.show(getUI.get(), dialog -> {

			ConfirmDialogResults confirmDialogResult = ConfirmDialogResults.CANCEL;

			if (resultIsOk(dialog)) {
				confirmDialogResult = ConfirmDialogResults.OK;
			} else if (resultIsNotOk(dialog)) {
				confirmDialogResult = ConfirmDialogResults.NOT_OK;
			} else {
				confirmDialogResult = ConfirmDialogResults.CANCEL;
			}

			properties.getOnCloseListener().accept(confirmDialogResult);

		}, properties.isModal());
	}

	private static boolean resultIsOk(ConfirmDialog dialog) {
		return dialog.isConfirmed();
	}

	private static boolean resultIsNotOk(ConfirmDialog dialog) {
		return !dialog.isConfirmed() && !dialog.isCanceled();
	}
}
