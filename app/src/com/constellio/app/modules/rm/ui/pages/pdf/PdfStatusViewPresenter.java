package com.constellio.app.modules.rm.ui.pages.pdf;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FilenameUtils;

import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorAsyncTask;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.entities.TemporaryRecordVO;
import com.constellio.app.ui.framework.builders.TemporaryRecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.batch.controller.BatchProcessState;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;

public class PdfStatusViewPresenter extends BasePresenter<PdfStatusView> {
	
    private static final String PDF_GENERATION = "PdfGeneration";
    
    private String batchProcessId;

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
        final BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();

        final String username = view.getSessionContext().getCurrentUser().getUsername();

        final String consolidatedId = username + String.valueOf(System.currentTimeMillis());

        PdfGeneratorAsyncTask asyncTask = new PdfGeneratorAsyncTask(documentIds, consolidatedId,
                pdfFileName, pdfFileName, username, withMetadata);

        AsyncTaskCreationRequest request = new AsyncTaskCreationRequest(asyncTask, view.getCollection(), PDF_GENERATION);
        request.setUsername(username);

        AsyncTaskBatchProcess batchProcess = batchProcessesManager.addAsyncTask(request);
        batchProcessId = batchProcess.getId();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isStarted()) {
                    List<Record> documentRecords = recordServices().getRecordsById(collection, documentIds);
                    
                    TaskProgressInfo taskProgressInfo = new TaskProgressInfo(documentRecords);
                    List<String> messages = taskProgressInfo.getMessages();
                    int processedCount = taskProgressInfo.getProcessedCount();
                    boolean errorOccurred = taskProgressInfo.hasErrorOccurred();

                    String globalProgressMessage = $("PdfStatusView.generationProgress", processedCount, documentIds.size());
                    view.notifyGlobalProgressMessage(globalProgressMessage);
                    dataProvider.setMessages(messages);

                    if (isFinished()) {
                        view.firePdfGenerationCompleted(consolidatedId, errorOccurred);
                        timer.cancel();
                    }
                }
            }
        }, 100, 500);
    }
    
    private BatchProcess getBatchProcess() {
    	BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
    	BatchProcess batchProcess = batchProcessesManager.get(batchProcessId);
    	return batchProcess;
    }
    
    private boolean isStarted() {
    	return getBatchProcess().getStartDateTime() != null;
    }
    
    private boolean isFinished() {
    	BatchProcessStatus status = getBatchProcess().getStatus();
    	return status == BatchProcessStatus.FINISHED;
    }

    public String getPdfFileName() {
		return pdfFileName;
	}

    public TemporaryRecordVO getPdfDocumentVO(String id) {
    	TemporaryRecordVO result;
        try {
            RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            result = new TemporaryRecordToVOBuilder().build(rm.get(id), VIEW_MODE.DISPLAY, view.getSessionContext());
        } catch (NoSuchRecordWithId e) {
        	result = null;
        }
        return result;
    }
    
    private class TaskProgressInfo {
    	
    	private List<String> messages = new ArrayList<>();
    	
    	private List<String> globalErrorMessages = new ArrayList<>();
    	
    	private List<String> documentIdsWithError = new ArrayList<>();
    	
    	private TaskProgressInfo(List<Record> documentRecords) {
    		BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
    		ContentManager contentManager = modelLayerFactory.getContentManager();
    		
    		BatchProcess batchProcess = getBatchProcess();
    		BatchProcessState batchProcessState = batchProcessesManager.getBatchProcessState(batchProcessId);
    		ValidationErrors validationErrors;
    		if (batchProcessState != null) {
        		validationErrors = batchProcessState.getValidationErrors();
    		} else {
    			validationErrors = new ValidationErrors();
    		}
    		
    		int errors = batchProcess.getErrors();
            if (errors > 0) {
                // Display errors
            	messages.add($("PdfStatusView.generationErrors", errors));
            }
            
            globalErrorMessages = getGlobalErrorMessages(validationErrors);
        	messages.addAll(globalErrorMessages);
            
            RMSchemasRecordsServices rm = new RMSchemasRecordsServices(view.getCollection(), appLayerFactory);
            for (Record documentRecord : documentRecords) {
				String documentId = documentRecord.getId();
				Document document = rm.wrapDocument(documentRecord);
				String documentErrorMessage = getErrorMessage(document, validationErrors);
				String documentInfoMessage = getInfoMessage(document, validationErrors);
				if (documentErrorMessage != null) {
					documentIdsWithError.add(documentId);
					messages.add(documentErrorMessage);
				} else if (documentInfoMessage != null) {	
					messages.add(documentInfoMessage);
				} else {
					// If we reach this point, the batch process manager said nothing about the document, so we'll try to deduce the state
	                if (document.getContent() != null) {
	                    ContentVersion contentVersion = document.getContent().getCurrentVersion();
	                    String extension = FilenameUtils.getExtension(contentVersion.getFilename());
	                    String hash = contentVersion.getHash();
	                    Boolean markedForPreviewConversion = document.getWrappedRecord().get(Schemas.MARKED_FOR_PREVIEW_CONVERSION);
	                    if (contentManager.hasContentPreview(hash)) {
	                        // Preview ready for document
	                        messages.add($("PdfStatusView.generationCompleted", document.getTitle()));
	                    } else if (!Boolean.FALSE.equals(markedForPreviewConversion)) {
	                        // Conversion pending
	                        messages.add($("PdfStatusView.generationPending", document.getTitle()));
	                    } else {
	                        // The MARKED_FOR_PREVIEW_CONVERSION flag has been set to false, which means that the conversion failed
	                        messages.add($("PdfStatusView.generationFailed", document.getTitle()));
	                        documentIdsWithError.add(documentId);
	                    }
	                } else {
	                    messages.add($("PdfStatusView.generationEmptyDocument", document.getTitle()));
	                }
				}
			}
    	}
    	
    	private List<String> getGlobalErrorMessages(ValidationErrors validationErrors) {
    		List<String> globalErrorMessages = new ArrayList<>();
    		for (ValidationError validationError : validationErrors.getValidationErrors()) {
				String id = (String) validationError.getParameter("id");
				if (PdfGeneratorAsyncTask.GLOBAL_ERROR_KEY.equals(id)) {
					String globalErrorMessageKey = (String) validationError.getParameter("messageKey");
					String errorMessage = $(globalErrorMessageKey);
					globalErrorMessages.add(errorMessage);
					break;
				}
			}
    		return globalErrorMessages;
    	}
    	
    	private String getErrorMessage(Document document, ValidationErrors validationErrors) {
    		String errorMessage = null;
    		String documentId = document.getId();
    		for (ValidationError validationError : validationErrors.getValidationErrors()) {
				String id = (String) validationError.getParameter("id");
				if (documentId.equals(id)) {
					String documentMessageKey = (String) validationError.getParameter("messageKey");
					errorMessage = $(documentMessageKey, document.getTitle());
					break;
				}
			}
    		return errorMessage;
    	}
    	
    	private String getInfoMessage(Document document, ValidationErrors validationErrors) {
    		String infoMessage = null;
    		String documentId = document.getId();
    		for (ValidationError validationError : validationErrors.getValidationWarnings()) {
				String id = (String) validationError.getParameter("id");
				if (documentId.equals(id)) {
					String documentMessageKey = (String) validationError.getParameter("messageKey");
					infoMessage = $(documentMessageKey, document.getTitle());
					break;
				}
			}
    		return infoMessage;
    	}

		public List<String> getMessages() {
			return messages;
		}

		public List<String> getDocumentIdsWithError() {
			return documentIdsWithError;
		}
		
		public boolean hasErrorOccurred() {
			return !globalErrorMessages.isEmpty() || !documentIdsWithError.isEmpty();
		}
		
		public int getProcessedCount() {
			return documentIds.size() - documentIdsWithError.size();
		}
    	
    }
    
}
