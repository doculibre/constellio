package com.constellio.app.modules.rm.ui.pages.pdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.constellio.app.modules.rm.pdfgenerator.FileStatus;
import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorAsyncTask;
import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
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

public class PdfStatusViewPresenter extends BasePresenter<PdfStatusView> {
    private static final String PDF_GENERATION = "PdfGeneration";

    private final String pdfFileName;
    private final List<String> documentIds;
    private final boolean withMetadata;

    private Map<String, FileStatus> messages = new HashMap<>();

    private PdfStatusMessageProvider dataProvider;
    private PdfGeneratorProgressInfo progressInfo;
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
        String collection = view.getCollection();

        ConstellioFactories constellioFactories = view.getConstellioFactories();
        ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		ContentManager contentManager = modelLayerFactory.getContentManager();
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

        final String username = view.getSessionContext().getCurrentUser().getUsername();

        PdfGeneratorAsyncTask asyncTask = new PdfGeneratorAsyncTask(documentIds, null,
                pdfFileName, pdfFileName, username, withMetadata);

        AsyncTaskCreationRequest request = new AsyncTaskCreationRequest(asyncTask, collection, PDF_GENERATION);
        request.setUsername(username);

        AsyncTaskBatchProcess batchProcess = batchProcessesManager.addAsyncTask(request);
        final String batchProcessId = batchProcess.getId();
        progressInfo = new PdfGeneratorProgressInfo();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
        	
        	private boolean notifiedStarted;
        	
            @Override
            public void run() {
            	AsyncTaskBatchProcess batchProcess = (AsyncTaskBatchProcess) batchProcessesManager.get(batchProcessId);
            	while (true) {
                	if (batchProcess.getStartDateTime() != null) {
                		// Process has started
                		if (!notifiedStarted) {
                			notifiedStarted = true;
                			progressInfo.notifyStartProcessing();
                		}

                    	if (batchProcess.getStatus() == BatchProcessStatus.FINISHED) {
                    		progressInfo.notifyEndProcessing();
                    		timer.cancel();
                    	} else if (batchProcess.getStatus() == BatchProcessStatus.CURRENT) {
                    		
                    	} else if (batchProcess.getStatus() == BatchProcessStatus.PENDING) {
                    		
                    	} else if (batchProcess.getStatus() == BatchProcessStatus.STANDBY) {
                    		
                    	}
                    	int errors = batchProcess.getErrors();
                    	if (errors > 0) {
                    		// Display errors
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
                            		progressInfo.notifyFileProcessingMessage(contentVersion.getFilename(), "Génération complétée");
                    			} else if (!Boolean.FALSE.equals(markedForPreviewConversion)) {
                    				// Conversion pending
                            		progressInfo.notifyFileProcessingMessage(contentVersion.getFilename(), "Génération en attente");
                        		} else {
                        			// The MARKED_FOR_PREVIEW_CONVERSION flag has been set to false, which means that the conversion failed
                    				processedCount++;
                            		progressInfo.notifyFileProcessingMessage(contentVersion.getFilename(), "Génération impossible");
                        		}
                    		} else {
                    			processedCount++;
                        		progressInfo.notifyFileProcessingMessage(document.getTitle(), "Aucun fichier électronique!");
                    		}
        				}
                    	progressInfo.notifyGlobalProcessingMessage("Ça roule! " + processedCount);
                        dataProvider.setMessages(progressInfo.getMessages());
                	} else {
                		try {
    						Thread.sleep(500);
    					} catch (InterruptedException e) {
    						e.printStackTrace();
    					}
                	}
            	}
            }
        }, 2000, 2000);
    }
    
}
