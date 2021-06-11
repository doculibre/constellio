package com.constellio.app.modules.rm.ui.components.pdf;

import com.constellio.app.modules.rm.services.logging.DecommissioningLoggingService;
import com.constellio.app.modules.rm.util.RMNavigationUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentConversionManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class DocumentPdfFormatSelectorPresenter {


	private final DocumentPdfFormatSelectorWindow window;
	private final Document document;
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final DecommissioningLoggingService decommissioningLoggingService;

	public DocumentPdfFormatSelectorPresenter(DocumentPdfFormatSelectorWindow window, Document document) {
		this.window = window;
		this.document = document;
		this.modelLayerFactory = window.getConstellioFactories().getModelLayerFactory();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.decommissioningLoggingService = new DecommissioningLoggingService(modelLayerFactory);

		window.setPdfA(false);
	}

	public void generateButtonClicked(User user, Map<String, String> urlParams) {
		boolean pdfA = window.isPdfA();

		Content content = document.getContent();
		ContentConversionManager conversionManager = new ContentConversionManager(modelLayerFactory);
		if (content != null) {
			try {
				conversionManager = new ContentConversionManager(modelLayerFactory);
				conversionManager.convertContentToPDF(user, content, pdfA);
				recordServices.update(document.getWrappedRecord());

				decommissioningLoggingService.logPdfAGeneration(document, user);

				window.showMessage($(String.format("DocumentActionsComponent.create%sSuccess", pdfA ? "PDFA" : "PDF")));
				window.close();

				RMNavigationUtils.navigateToDisplayDocument(document.getId(), urlParams,
						window.getConstellioFactories().getAppLayerFactory(), document.getCollection());
			} catch (Exception e) {
				window.showErrorMessage($(String.format("DocumentActionsComponent.create%sFailure", pdfA ? "PDFA" : "PDF"))
										+ " : " + MessageUtils.toMessage(e));
			} finally {
				conversionManager.close();
			}
		}
	}

	public void cancelButtonClicked() {
		window.close();
	}
}
