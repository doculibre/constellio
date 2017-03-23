package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.records.RecordServicesException;

import java.util.List;

/**
 * Created by Constellio on 2017-03-23.
 */
public class RMBorrowTaskApprovedExtension extends RecordExtension {

    String collection;
    AppLayerFactory appLayerFactory;
    TasksSchemasRecordsServices tasksSchemas;
    RMSchemasRecordsServices rmSchemas;

    public RMBorrowTaskApprovedExtension(String collection, AppLayerFactory appLayerFactory) {

        this.collection = collection;
        this.appLayerFactory = appLayerFactory;
        this.tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
        this.rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
    }

    @Override
    public void recordModified(RecordModificationEvent event) {
        if(event.isSchemaType(Task.SCHEMA_TYPE)) {
            RMTask task = rmSchemas.wrapRMTask(event.getRecord());
            if(event.hasModifiedMetadata(Task.STATUS) && tasksSchemas.getTaskStatus(task.getStatus()).isFinished() &&
                    "oui".equals(task.getDecision().toLowerCase())) {
                String typeCode = task.getType() == null ? "":tasksSchemas.getTaskType(task.getType()).getCode();
                if(typeCode.equals("borrowRequest")) {
                    completeBorrowRequest(task);
                } else if(typeCode.equals("returnRequest")) {
                    completeReturnRequest(task);
                } else if(typeCode.equals("reactivationRequest")) {
                    completeReactivationRequest(task);
                } else if(typeCode.equals("borrowExtensionRequest")) {
                    completeBorrowExtensionRequest(task);
                }
            }
        }
    }

    private void completeBorrowRequest(RMTask task) {
        List<String> linkedFolders = task.getLinkedFolders();
        Transaction transaction = new Transaction();
        if(linkedFolders != null) {
            List<Folder> folders = rmSchemas.getFolders(linkedFolders);
            for(Folder folder: folders) {
                folder.setBorrowed(true);
                transaction.add(folder);
            }
        }

        try {
            appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    private void completeReturnRequest(RMTask task) {
        List<String> linkedFolders = task.getLinkedFolders();
        Transaction transaction = new Transaction();
        if(linkedFolders != null) {
            List<Folder> folders = rmSchemas.getFolders(linkedFolders);
            for(Folder folder: folders) {
                folder.setBorrowed(null);
                transaction.add(folder);
            }
        }

        try {
            appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    private void completeReactivationRequest(RMTask task) {

    }

    private void completeBorrowExtensionRequest(RMTask task) {

    }
}
