package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.*;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by Constellio on 2017-03-23.
 */
public class RMRequestTaskApprovedExtension extends RecordExtension {

    String collection;
    AppLayerFactory appLayerFactory;
    TasksSchemasRecordsServices tasksSchemas;
    RMSchemasRecordsServices rmSchemas;
    BorrowingServices borrowingServices;
    UserServices userServices;

    public RMRequestTaskApprovedExtension(String collection, AppLayerFactory appLayerFactory) {

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
            List<String> acceptedSchemas = new ArrayList<>(asList(BorrowRequest.FULL_SCHEMA_NAME, ReturnRequest.FULL_SCHEMA_NAME,
                    ReactivationRequest.FULL_SCHEMA_NAME, ExtensionRequest.FULL_SCHEMA_NAME));
            String schemaCode = task.getSchemaCode();
            if(event.hasModifiedMetadata(Task.STATUS) && tasksSchemas.getTaskStatus(task.getStatus()).isFinished() && acceptedSchemas.contains(schemaCode)) {
                String typeCode = task.getType() == null ? "":tasksSchemas.getTaskType(task.getType()).getCode();
                Boolean isAccepted = task.get(BorrowRequest.ACCEPTED);
                if(typeCode.equals(BorrowRequest.SCHEMA_NAME)) {
                    completeBorrowRequest(task, isAccepted);
                } else if(typeCode.equals(ReturnRequest.SCHEMA_NAME)) {
                    completeReturnRequest(task, isAccepted);
                } else if(typeCode.equals(ReactivationRequest.SCHEMA_NAME)) {
                    completeReactivationRequest(task, isAccepted);
                } else if(typeCode.equals(ExtensionRequest.SCHEMA_NAME)) {
                    completeBorrowExtensionRequest(task, isAccepted);
                }
            }
        }
    }

    public void completeBorrowRequest(RMTask task, Boolean isAccepted) {
        try {
            User applicant = rmSchemas.getUser((String) task.get(RequestTask.APPLICANT));
            User respondant = rmSchemas.getUser((String) task.get(RequestTask.RESPONDANT));
            Double borrowDuration = task.get(BorrowRequest.BORROW_DURATION);
            int numberOfDays = borrowDuration == null? 0:borrowDuration.intValue();
            borrowingServices.borrowRecordsFromTask(task.getId(), LocalDate.now(), LocalDate.now().plusDays(numberOfDays),
                    respondant, applicant, BorrowingType.BORROW);
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void completeReturnRequest(RMTask task, Boolean isAccepted) {
        try {
            User applicant = rmSchemas.getUser((String) task.get(RequestTask.APPLICANT));
            User respondant = rmSchemas.getUser((String) task.get(RequestTask.RESPONDANT));
            borrowingServices.returnRecordsFromTask(task.getId(), LocalDate.now(), respondant, applicant);
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }

    public void completeReactivationRequest(RMTask task, Boolean isAccepted) {

    }

    public void completeBorrowExtensionRequest(RMTask task, Boolean isAccepted) {
        try {
            User applicant = rmSchemas.getUser((String) task.get(RequestTask.APPLICANT));
            User respondant = rmSchemas.getUser((String) task.get(RequestTask.RESPONDANT));
            borrowingServices.extendRecordsBorrowingPeriodFromTask(task.getId(), (LocalDate) task.get(ExtensionRequest.EXTENSION_VALUE), respondant, applicant);
        } catch (RecordServicesException e) {
            e.printStackTrace();
        }
    }
}
