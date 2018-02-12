package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.AuthorizationsServices;

import java.io.Serializable;
import java.util.List;

public class RecordCommentsDisplayPresenter implements Serializable {
    private RecordCommentsDisplay editor;

    private String recordId;

    private String metadataCode;

    private SchemaPresenterUtils presenterUtils;

    public RecordCommentsDisplayPresenter(RecordCommentsDisplay editor) {
        this.editor = editor;
    }

    public void forRecordVO(RecordVO recordVO, String metadataCode) {
        this.recordId = recordVO.getId();
        this.metadataCode = metadataCode;
        init();
    }

    public void forRecordId(String recordId, String metadataCode) {
        this.recordId = recordId;
        this.metadataCode = metadataCode;
        init();
    }

    private void init() {
        SessionContext sessionContext = editor.getSessionContext();
        ConstellioFactories constellioFactories = editor.getConstellioFactories();
        ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
        RecordServices recordServices = modelLayerFactory.newRecordServices();

        AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();

        Record record = recordServices.getDocumentById(recordId);
        String schemaCode = record.getSchemaCode();

        presenterUtils = new SchemaPresenterUtils(schemaCode, constellioFactories, sessionContext);

        Metadata metadata = presenterUtils.getMetadata(metadataCode);
        String caption = metadata.getLabel(Language.withCode(presenterUtils.getCurrentLocale().getLanguage()));
        List<Comment> comments = record.get(metadata);
        editor.setComments(comments);
        editor.setCaption(caption);

        User currentUser = presenterUtils.getCurrentUser();
        if (!authorizationsServices.canWrite(currentUser, record)) {
            editor.setVisible(false);
        }
    }

    public void commentsChanged(List<Comment> newComments) {
        if (newComments != null) {
            Metadata metadata = presenterUtils.getMetadata(metadataCode);

            Record record = presenterUtils.getRecord(recordId);
            List<Comment> existingComments = record.get(metadata);
            if (!newComments.equals(existingComments)) {
                User user = presenterUtils.getCurrentUser();
                ConstellioFactories constellioFactories = editor.getConstellioFactories();
                ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
                AuthorizationsServices authorizationsServices = modelLayerFactory.newAuthorizationsServices();

                record.set(metadata, newComments);
                if (authorizationsServices.canWrite(user, record)) {
                    presenterUtils.addOrUpdate(record);
                }
            }
        }
    }
}
