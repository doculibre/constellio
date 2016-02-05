package com.constellio.app.ui.framework.components.fields.comment;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveCommentField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.data.Property;

@SuppressWarnings("unchecked")
public class RecordCommentsEditorImpl extends ListAddRemoveCommentField implements RecordCommentsEditor {
	
	private RecordVO recordVO;
	
	private String recordId;
	
	private String metadataCode;
	
	private RecordCommentsEditorPresenter presenter;
	
	public RecordCommentsEditorImpl(RecordVO recordVO, String metadataCode) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		init();
	}

	public RecordCommentsEditorImpl(String recordId, String metadataCode) {
		this.recordId = recordId;
		this.metadataCode = metadataCode;
		init();
	}
	
	private void init() {
		setCaption($("comments"));
		
		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				List<Comment> comments = (List<Comment>) event.getProperty().getValue();
				presenter.commentsChanged(comments);
			}
		});
		presenter = new RecordCommentsEditorPresenter(this);
		if (recordVO != null) {
			presenter.forRecordVO(recordVO, metadataCode);
		} else {
			presenter.forRecordId(recordId, metadataCode);
		}
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
