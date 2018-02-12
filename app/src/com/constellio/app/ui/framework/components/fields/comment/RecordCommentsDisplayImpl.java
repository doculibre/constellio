package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveCommentField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.data.Property;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RecordCommentsDisplayImpl extends ListAddRemoveCommentField implements RecordCommentsDisplay {
    private RecordVO recordVO;

    private String recordId;

    private String metadataCode;

    private RecordCommentsDisplayPresenter presenter;

    public RecordCommentsDisplayImpl(RecordVO recordVO, String metadataCode) {
        this.recordVO = recordVO;
        this.metadataCode = metadataCode;
        init();
    }
    @Override
    protected boolean isAddEditFieldVisible() {
        return false;
    }

    private void init() {
        setCaption($("comments"));

        addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                List<Comment> comments = (List<Comment>) event.getProperty().getValue();
                presenter.commentsChanged(comments);
            }
        });
        presenter = new RecordCommentsDisplayPresenter(this);
        if (recordVO != null) {
            presenter.forRecordVO(recordVO, metadataCode);
        } else {
            presenter.forRecordId(recordId, metadataCode);
        }
        this.setSizeFull();
    }



    @Override
    public void setComments(List<Comment> comments) {
        super.setValue(comments);
    }

    @Override
    public SessionContext getSessionContext() {
        return ConstellioUI.getCurrentSessionContext();
    }

    @Override
    public ConstellioFactories getConstellioFactories() {
        return ConstellioFactories.getInstance();
    }
}
