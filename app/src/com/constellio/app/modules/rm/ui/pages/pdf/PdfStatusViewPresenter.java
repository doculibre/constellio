package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.pdfgenerator.FileStatus;
import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorAsyncTask;
import com.constellio.app.modules.rm.pdfgenerator.PdfGeneratorProgressInfo;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.batchprocess.*;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executors;

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
        final BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();

        final String userName = ConstellioUI.getCurrent().getSessionContext().getCurrentUser().getUsername();

        PdfGeneratorAsyncTask asyncTask = new PdfGeneratorAsyncTask(documentIds, null,
                pdfFileName, pdfFileName, userName, withMetadata);

        /*progressInfo = asyncTask.getProgressInfo();*/

        AsyncTaskCreationRequest request = new AsyncTaskCreationRequest(asyncTask, collection, PDF_GENERATION);
        request.setUsername(userName);

        batchProcessesManager.addAsyncTask(request);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(progressInfo == null) {
                    progressInfo = findProgressInfo();
                }

                if (progressInfo.isStarted()) {
                    if(progressInfo.isEnded()) {
                        view.firePdfGenerationCompleted(null);
                        timer.cancel();
                    } else {
                        view.notifyGlobalProgressMessage(progressInfo.getGlobalMessage());
                        dataProvider.setMessages(progressInfo.getMessages());
                    }
                } else {
                    timer.cancel();
                }
            }
        }, 2000, 2000);

        /*Executors.defaultThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AsyncTaskExecutionParams params = new AsyncTaskExecutionParams() {
                        @Override
                        public String getCollection() {
                            return view.getCollection();
                        }
                    };
                    asyncTask.execute(params);
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    @Nullable
    private PdfGeneratorProgressInfo findProgressInfo() {
        ConstellioFactories constellioFactories = view.getConstellioFactories();
        ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        BatchProcessesManager batchProcessesManager = modelLayerFactory.getBatchProcessesManager();

        PdfGeneratorProgressInfo progressInfo = findProgressInfo(batchProcessesManager.getAllNonFinishedBatchProcesses());
        if(progressInfo == null) {
            progressInfo = findProgressInfo(batchProcessesManager.getFinishedBatchProcesses());
        }
        if(progressInfo == null) {
            progressInfo = findProgressInfo(batchProcessesManager.getPendingBatchProcesses());
        }
        if(progressInfo == null) {
            progressInfo = findProgressInfo(batchProcessesManager.getStandbyBatchProcesses());
        }

        return progressInfo;
    }

    @Nullable
    private PdfGeneratorProgressInfo findProgressInfo(List<BatchProcess> allNonFinishedBatchProcesses) {
        String userName = ConstellioUI.getCurrent().getSessionContext().getCurrentUser().getUsername();

        PdfGeneratorProgressInfo pi = null;
        for (BatchProcess batchProcess : allNonFinishedBatchProcesses) {
            if (userName.equals(batchProcess.getUsername()) && batchProcess instanceof AsyncTaskBatchProcess) {
                AsyncTask asyncTask = ((AsyncTaskBatchProcess) batchProcess).getTask();
                if (asyncTask instanceof PdfGeneratorAsyncTask) {
                    PdfGeneratorAsyncTask task = (PdfGeneratorAsyncTask) asyncTask;
                    if (pdfFileName.equals(task.getConsolidatedName())) {
                        pi = task.getProgressInfo();
                    }
                }
            }
        }
        return pi;
    }
}
