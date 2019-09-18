package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveCommentField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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

	@Override
	protected Component initContent() {
		Component finalComponent = super.initContent();

		enableModification(isAddButtonVisible());

		return finalComponent;
	}

	private void init() {
		setCaption($("comments"));

		addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				List<Comment> comments = (List<Comment>) event.getProperty().getValue();
				presenter.commentsChanged(comments, isUserHasToHaveWriteAuthorization());
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

	@Override
	protected CommentField newAddEditField() {
		CommentField components = super.newAddEditField();

		components.setEnabled(isAddButtonVisible());
		return components;
	}

	@Override
	protected Component newCaptionComponent(Comment itemId, String caption) {
		Component component = super.newCaptionComponent(itemId, caption);
		component.setEnabled(isAddButtonVisible());
		return component;
	}

	@Override
	protected boolean isEditButtonVisible(Comment item) {
		return presenter.isAddEditButtonEnabled();
	}

	@Override
	protected boolean isDeleteButtonVisible(Comment item) {
		return presenter.isAddEditButtonEnabled();
	}

	@Override
	public void enableModification(boolean modification) {
		getAddButton().setEnabled(modification);
	}

	public boolean isAddButtonVisible() {
		return presenter.isAddEditButtonEnabled();
	}

	public boolean isUserHasToHaveWriteAuthorization() {
		return true;
	}
}

