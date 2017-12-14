package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.sip.ConstellioSIP;
import com.constellio.app.modules.rm.services.sip.data.intelligid.ConstellioSIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import org.apache.commons.collections.ListUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class SIPBuildAsyncTask implements AsyncTask {

    private String sipFileName;
    private List<String> bagInfoLines;
    private List<String> includeDocumentIds;
    private List<String> includeFolderIds;
    private boolean limitSize;
    private String username;
    private boolean deleteFiles;
    private String currentVersion;
    private ProgressInfo progressInfo;
    private UUID uuid;

    public SIPBuildAsyncTask(String sipFileName, List<String> bagInfoLines, List<String> includeDocumentIds, List<String> includeFolderIds, Boolean limitSize, String username, Boolean deleteFiles, String currentVersion) {
        this.bagInfoLines = bagInfoLines;
        this.includeDocumentIds = includeDocumentIds;
        this.includeFolderIds = includeFolderIds;
        this.sipFileName = sipFileName;
        this.limitSize = limitSize;
        this.username = username;
        this.deleteFiles = deleteFiles;
        this.currentVersion = currentVersion;
        this.uuid = UUID.randomUUID();
        this.progressInfo = new ProgressInfo();
        validateParams();
    }

    @Override
    public void execute(AsyncTaskExecutionParams params) throws ImpossibleRuntimeException {
        ValidationErrors errors = new ValidationErrors();
        AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
        String collection = params.getCollection();
        ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
        File outFolder = null;
        File outFile = null;
        try {
            List<String> ids = ListUtils.union(this.includeDocumentIds, this.includeFolderIds);
            User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.username, collection);
            if (ids.isEmpty()) {
                errors.add(SIPGenerationValidationException.class, "Lists cannot be null");
            } else {
                if (deleteFiles) {
                    RecordServices recordServices = modelLayerFactory.newRecordServices();
                    for (String documentIds : ids) {
                        try {
                            Record record = recordServices.getDocumentById(documentIds);
                            recordServices.logicallyDelete(record, currentUser);
                            recordServices.physicallyDelete(record, currentUser, new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
                        } catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
                            //No need to delete it.
                        }
                    }
                }
            }

            RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            outFolder = modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFolder("SIPArchives");
            outFile = new File(outFolder, this.sipFileName);
            SIPFilter filter = new SIPFilter(collection, appLayerFactory).withIncludeDocumentIds(this.includeDocumentIds).withIncludeFolderIds(this.includeFolderIds);
            ConstellioSIPObjectsProvider metsObjectsProvider = new ConstellioSIPObjectsProvider(collection, appLayerFactory, filter, progressInfo);

            if (!metsObjectsProvider.list().isEmpty()) {
                ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, bagInfoLines, limitSize, currentVersion, progressInfo);
                constellioSIP.build(outFile);

                //Create SIParchive record
                ContentManager contentManager = modelLayerFactory.getContentManager();
                SIParchive sipArchive = rm.newSIParchive();
                ContentVersionDataSummary summary = contentManager.upload(outFile);
                sipArchive.setContent(contentManager.createMajor(currentUser, sipFileName, summary));
                sipArchive.setUser(currentUser);
                sipArchive.setCreatedBy(currentUser.getId());
                sipArchive.setCreationDate(new LocalDateTime());
                Transaction transaction = new Transaction();
                transaction.add(sipArchive);
                modelLayerFactory.newRecordServices().execute(transaction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
            ioServices.deleteQuietly(outFile);
            ioServices.deleteQuietly(outFolder);
        }
    }

    public String getUUID() {
        return uuid.toString();
    }

    @Override
    public Object[] getInstanceParameters() {
        return new Object[]{sipFileName, bagInfoLines, includeDocumentIds, includeFolderIds, limitSize, username, deleteFiles, currentVersion};
    }

    public ProgressInfo getProgressInfo() {
        return progressInfo;
    }

    private void validateParams() throws ImpossibleRuntimeException {
        if(this.sipFileName == null || this.sipFileName.isEmpty()) {
            throw new ImpossibleRuntimeException("sip file name null");
        }

        if(this.username == null || this.username.isEmpty()) {
            throw new ImpossibleRuntimeException("username null");
        }

        if(this.currentVersion == null || this.currentVersion.isEmpty()) {
            throw new ImpossibleRuntimeException("version null");
        }
    }
}