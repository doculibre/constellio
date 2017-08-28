package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.ConstellioSIP;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.data.intelligid.ConstellioSIPObjectsProvider;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
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

public class SIPBuildAsyncTask implements AsyncTask {

    private String sipFileName;
    private List<String> bagInfoLines;
    private List<String> includeDocumentIds;
    private List<String> includeFolderIds;
    private boolean limitSize;
    private String username;
    private boolean deleteFiles;
    private String currentVersion;

    public SIPBuildAsyncTask(String sipFileName, List<String> bagInfoLines, List<String> includeDocumentIds, List<String> includeFolderIds, Boolean limitSize, String username, Boolean deleteFiles, String currentVersion){
        this.bagInfoLines = bagInfoLines;
        this.includeDocumentIds = includeDocumentIds;
        this.includeFolderIds = includeFolderIds;
        this.sipFileName = sipFileName;
        this.limitSize = limitSize;
        this.username = username;
        this.deleteFiles = deleteFiles;
        this.currentVersion = currentVersion;
    }

    @Override
    public void execute(AsyncTaskExecutionParams params) {
        try{
            AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
            String collection = params.getCollection();
            ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
            RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            File outFolder = modelLayerFactory.getIOServicesFactory().newIOServices().newTemporaryFolder("SIPArchives");
            final File outFile = new File(outFolder, this.sipFileName);

            SIPFilter filter = new SIPFilter(collection, appLayerFactory).withIncludeDocumentIds(this.includeDocumentIds).withIncludeFolderIds(this.includeFolderIds);
            ConstellioSIPObjectsProvider metsObjectsProvider = new ConstellioSIPObjectsProvider(collection, appLayerFactory, filter);
            if (!metsObjectsProvider.list().isEmpty()) {
                ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, bagInfoLines, limitSize, currentVersion);
                constellioSIP.build(outFile);
                User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.username, collection);

                if(deleteFiles) {
                    RecordServices recordServices = modelLayerFactory.newRecordServices();
                    List<String> ids = ListUtils.union(this.includeDocumentIds, this.includeFolderIds);
                    for(String documentIds : ids) {
                        try{
                            Record record = recordServices.getDocumentById(documentIds);
                            recordServices.logicallyDelete(record, currentUser);
                            recordServices.physicallyDelete(record, currentUser, new RecordPhysicalDeleteOptions().setMostReferencesToNull(true));
                        }catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
                            //No need to delete it.
                        }
                    }
                }

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
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object[] getInstanceParameters() {
        return new Object[] {sipFileName, bagInfoLines, includeDocumentIds, includeFolderIds, limitSize, username, deleteFiles, currentVersion};
    }
}