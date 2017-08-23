package com.constellio.app.ui.framework.buttons.SIPButton;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.ConstellioSIP;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.data.intelligid.IntelliGIDSIPObjectsProvider;
import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
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

    public SIPBuildAsyncTask(String sipFileName, List<String> bagInfoLines, List<String> includeDocumentIds, List<String> includeFolderIds, Boolean limitSize, String username){
        this.bagInfoLines = bagInfoLines;
        this.includeDocumentIds = includeDocumentIds;
        this.includeFolderIds = includeFolderIds;
        this.sipFileName = sipFileName;
        this.limitSize = limitSize;
        this.username = username;
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
            IntelliGIDSIPObjectsProvider metsObjectsProvider = new IntelliGIDSIPObjectsProvider(collection, appLayerFactory, filter);
            if (!metsObjectsProvider.list().isEmpty()) {
                ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, bagInfoLines, limitSize);
                constellioSIP.build(outFile);
                SIParchive sipArchive = rm.newSIParchive();

                //Create SIParchive record
                ContentManager contentManager = modelLayerFactory.getContentManager();
                User currentUser = modelLayerFactory.newUserServices().getUserInCollection(this.username, collection);
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
        return new Object[] {sipFileName, bagInfoLines, includeDocumentIds, includeFolderIds, limitSize, username};
    }
}