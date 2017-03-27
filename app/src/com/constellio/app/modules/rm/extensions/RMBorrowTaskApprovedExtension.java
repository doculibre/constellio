package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import org.joda.time.LocalDate;

/**
 * Created by Constellio on 2017-03-23.
 */
public class RMBorrowTaskApprovedExtension extends RecordExtension {

    String collection;
    AppLayerFactory appLayerFactory;
    TasksSchemasRecordsServices tasksSchemas;
    RMSchemasRecordsServices rmSchemas;
    BorrowingServices borrowingServices;
    UserServices userServices;

    public RMBorrowTaskApprovedExtension(String collection, AppLayerFactory appLayerFactory) {

        this.collection = collection;
        this.appLayerFactory = appLayerFactory;
        this.tasksSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
        this.rmSchemas = new RMSchemasRecordsServices(collection, appLayerFactory);
        this.borrowingServices = new BorrowingServices(collection, appLayerFactory.getModelLayerFactory());
        this.userServices = appLayerFactory.getModelLayerFactory().newUserServices();
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
        try {
            borrowingServices.borrowRecordsFromTask(task.getId(), LocalDate.now(), LocalDate.now(),
                    userServices.getUserInCollection(task.getAssigner(), collection), userServices.getUserInCollection(task.getAssigner(), collection),
                    BorrowingType.BORROW);
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    private void completeReturnRequest(RMTask task) {
        try {
            borrowingServices.returnRecordsFromTask(task.getId(), LocalDate.now(), userServices.getUserInCollection(task.getAssigner(), collection));
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    private void completeReactivationRequest(RMTask task) {

    }

    private void completeBorrowExtensionRequest(RMTask task) {

    }
}
