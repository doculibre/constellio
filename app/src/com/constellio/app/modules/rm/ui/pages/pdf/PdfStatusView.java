package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.ui.pages.base.BaseView;

import java.io.File;

public interface PdfStatusView extends BaseView {

	public void firePdfGenerationCompleted(File consolidatePdfFile, boolean errorOccurred);

	public void notifyGlobalProgressMessage(String message);

}
