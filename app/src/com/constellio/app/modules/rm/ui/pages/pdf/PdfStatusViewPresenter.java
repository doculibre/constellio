package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorAsyncTask;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.DocumentToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;

import java.io.InputStream;
import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfStatusViewPresenter extends BasePresenter<PdfStatusView> {
    private static final String PDF_GENERATION = "PdfGeneration";

    private final String pdfFileName;
    private final List<String> documentIds;
    private final boolean withMetadata;

    private PdfStatusMessageProvider dataProvider;
    private Timer timer;

    public PdfStatusViewPresenter(final PdfStatusView view, String pdfFileName, List<String> documentIds, boolean withMetadata) {
        super(view);

        this.pdfFileName = pdfFileName;
        this.documentIds = documentIds;
        this.withMetadata = withMetadata;

        this.dataProvider = new PdfStatusMessageProvider();

        startBatchProcess();
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }

    public PdfStatusDataProvider<?> getDataProvider() {
        return dataProvider;
    }

    protected void startBatchProcess() {
        ConstellioFactories constellioFactories = view.getConstellioFactories();
        final ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        final BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();

        final String username = view.getSessionContext().getCurrentUser().getUsername();

        final String consolidedId = username + String.valueOf(System.currentTimeMillis());

        PdfGeneratorAsyncTask asyncTask = new PdfGeneratorAsyncTask(documentIds, consolidedId,
                pdfFileName, pdfFileName, username, withMetadata);

        AsyncTaskCreationRequest request = new AsyncTaskCreationRequest(asyncTask, view.getCollection(), PDF_GENERATION);
        request.setUsername(username);

        AsyncTaskBatchProcess batchProcess = batchProcessesManager.addAsyncTask(request);
        final String batchProcessId = batchProcess.getId();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ContentManager contentManager = modelLayerFactory.getContentManager();
                RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
            	AsyncTaskBatchProcess batchProcess = (AsyncTaskBatchProcess) batchProcessesManager.get(batchProcessId);

                if (batchProcess.getStartDateTime() != null) {
                    List<String> messages = new ArrayList<>();

                    int errors = batchProcess.getErrors();
                    if (errors > 0) {
                        // Display errors
                        messages.add($("PdfStatusViewPresenter.generationErrors", errors));
                    }

                    int processedCount = 0;
                    for (String documentId : documentIds) {
                        Document document = rm.getDocument(documentId);
                        if (document.getContent() != null) {
                            ContentVersion contentVersion = document.getContent().getCurrentVersion();
                            String hash = contentVersion.getHash();
                            Boolean markedForPreviewConversion = document.getWrappedRecord().get(Schemas.MARKED_FOR_PREVIEW_CONVERSION);
                            if (contentManager.hasContentPreview(hash)) {
                                // Preview ready for document
                                processedCount++;
                                messages.add($("PdfStatusViewPresenter.generationCompleted", contentVersion.getFilename()));
                            } else if (!Boolean.FALSE.equals(markedForPreviewConversion)) {
                                // Conversion pending
                                messages.add($("PdfStatusViewPresenter.generationPending", contentVersion.getFilename()));
                            } else {
                                // The MARKED_FOR_PREVIEW_CONVERSION flag has been set to false, which means that the conversion failed
                                processedCount++;
                                messages.add($("PdfStatusViewPresenter.generationFailed", contentVersion.getFilename()));
                            }
                        } else {
                            processedCount++;
                            messages.add($("PdfStatusViewPresenter.generationEmptyDocument", document.getTitle()));
                        }
                    }
                    view.notifyGlobalProgressMessage($("PdfStatusViewPresenter.generationProgress", processedCount, documentIds.size()));
                    dataProvider.setMessages(messages);

                    if (batchProcess.getStatus() == BatchProcessStatus.FINISHED) {
                        view.firePdfGenerationCompleted(consolidedId);
                        timer.cancel();
                    }
                }
            }
        }, 100, 500);
    }

    public Resource getPdfDocumentResource(final String id) {
        return new StreamResource(new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return getPdfDocumentVO(id).getContent().getInputStreamProvider().getInputStream(pdfFileName);
            }
        }, pdfFileName);
    }

    public DocumentVO getPdfDocumentVO(String id) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        DocumentToVOBuilder documentToVOBuilder = new DocumentToVOBuilder(appLayerFactory.getModelLayerFactory());
        return documentToVOBuilder.build(rm.get(id), RecordVO.VIEW_MODE.TABLE, ConstellioUI.getCurrentSessionContext());
    }
}
