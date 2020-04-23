package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordCommentsDisplayImpl extends CustomField<List<Comment>> implements RecordCommentsDisplay {

	private RecordVO recordVO;

	private String recordId;

	private String metadataCode;

	public static final String USER_PROPERTY = "user";

	public static final String DATE_PROPERTY = "commentDate";

	private JodaDateTimeToStringConverter dateTimeConverter = new JodaDateTimeToStringConverter();

	private VerticalLayout mainLayout;

	private BaseButton addCommentButton;

	private VerticalLayout commentsLayout;

	private Boolean delayedReadOnly;

	private RecordCommentsDisplayPresenter presenter;

	public RecordCommentsDisplayImpl(RecordVO recordVO, String metadataCode) {
		this.recordVO = recordVO;
		this.metadataCode = metadataCode;
		init();
	}

	public RecordCommentsDisplayImpl(String recordId, String metadataCode) {
		this.recordId = recordId;
		this.metadataCode = metadataCode;
		init();
	}

	private void init() {
		presenter = new RecordCommentsDisplayPresenter(this);
		if (recordVO != null) {
			presenter.forRecordVO(recordVO, metadataCode);
		} else {
			presenter.forRecordId(recordId, metadataCode);
		}
	}

	@Override
	protected Component initContent() {
		addStyleName("record-comments");
		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.addStyleName("record-comments-layout");

		commentsLayout = newCommentsLayout();
		mainLayout.addComponent(commentsLayout);
		return mainLayout;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		if (addCommentButton != null) {
			addCommentButton.setVisible(!readOnly);
		} else {
			delayedReadOnly = readOnly;
		}
	}

	protected void reloadComments() {
		mainLayout.replaceComponent(commentsLayout, commentsLayout = newCommentsLayout());
	}

	protected List<Comment> ensureComments() {
		List<Comment> comments = getValue();
		if (comments == null) {
			comments = new ArrayList<>();
			setValue(comments);
		}
		return comments;
	}

	protected VerticalLayout newCommentsLayout() {
		CommentsLayout commentsLayout = new CommentsLayout(ensureComments(), recordVO.getMetadata(metadataCode).getLabel()) {
			@Override
			protected Locale getLocal() {
				return getLocale();
			}

			@Override
			protected void commentDeleted(Comment comment) {
				presenter.commentDeleted(comment);
			}

			@Override
			protected void commentModified(Comment comment, String value) {
				presenter.commentModified(comment, value);
			}

			@Override
			protected void commentAdded(Comment comment) {
				presenter.commentAdded(comment);
			}

			@Override
			protected boolean deleteButtonVisible(Comment comment) {
				return !isReadOnly() && presenter.commentCreatedByCurrentUser(comment);
			}

			@Override
			protected boolean editButtonVisible(Comment comment) {
				return !isReadOnly() && presenter.commentCreatedByCurrentUser(comment);
			}

			@Override
			protected boolean addCommentButtonVisible() {
				return presenter.addButtonVisible();
			}
		};
		return commentsLayout;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends List<Comment>> getType() {
		return (Class) List.class;
	}

	@Override
	public void setComments(List<Comment> comments) {
		super.setValue(comments);
		if (mainLayout != null) {
			reloadComments();
		}
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
