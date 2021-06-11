package com.constellio.app.modules.rm.ui.components.pdf;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

import java.io.Serializable;

public interface DocumentPdfFormatSelectorWindow extends Serializable {

	void showMessage(String message);

	void showErrorMessage(String message);

	void open();

	void createPdf();

	void close();

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	boolean isPdfA();

	void setPdfA(boolean isPDFa);
}
