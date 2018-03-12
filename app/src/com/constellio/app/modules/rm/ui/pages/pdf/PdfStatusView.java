package com.constellio.app.modules.rm.ui.pages.pdf;

import java.io.File;

import com.constellio.app.ui.pages.base.BaseView;

public interface PdfStatusView extends BaseView {
	
    public void firePdfGenerationCompleted(File consolidatePdfFile, boolean errorOccurred);
    
    public void notifyGlobalProgressMessage(String message);

}
