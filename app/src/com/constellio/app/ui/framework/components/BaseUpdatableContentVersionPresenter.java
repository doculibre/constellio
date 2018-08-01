package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.builders.UserDocumentToVOBuilder;
import com.constellio.app.ui.framework.components.content.UpdatableContentVersionPresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;

public class BaseUpdatableContentVersionPresenter implements UpdatableContentVersionPresenter {
    AppLayerFactory appLayerFactory = null;
    SessionContext sessionContext = null;

    public BaseUpdatableContentVersionPresenter() {
    }

    public BaseUpdatableContentVersionPresenter(AppLayerFactory appLayerFactory) {
        this.appLayerFactory = appLayerFactory;
    }

    @Override
    public ContentVersionVO getUpdatedContentVersionVO(RecordVO recordVO, ContentVersionVO previousConventVersionVO) {
        ensureAppLayerFactory();
        ensureSessionContext();
        Record updatedRecord = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(recordVO.getId());
        UserDocumentToVOBuilder voBuilder = new UserDocumentToVOBuilder();
        UserDocumentVO updatedUserDocument = voBuilder.build(updatedRecord, RecordVO.VIEW_MODE.FORM, sessionContext);
        return updatedUserDocument.getContent();
    }

    public void ensureAppLayerFactory() {
        if(appLayerFactory == null) {
            appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
        }
    }

    public void ensureSessionContext() {
        if(sessionContext == null) {
            sessionContext = ConstellioUI.getCurrentSessionContext();
        }
    }
}
