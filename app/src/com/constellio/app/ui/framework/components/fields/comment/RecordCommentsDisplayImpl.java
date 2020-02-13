package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseForm.FieldAndPropertyId;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.user.UserDisplay;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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

	protected boolean commentAdded(Comment newComment) {
		return presenter.commentAdded(newComment);
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
		List<Comment> comments = ensureComments();

		final VerticalLayout commentsLayout = new VerticalLayout();
		commentsLayout.setWidth("100%");
		commentsLayout.setSpacing(true);
		commentsLayout.addStyleName("record-comments");

		if (presenter.addButtonVisible()) {
			Component addCommentsComponent = newAddCommentComponent(commentsLayout);
			commentsLayout.addComponent(addCommentsComponent);
			commentsLayout.setComponentAlignment(addCommentsComponent, Alignment.TOP_RIGHT);
		} else {
			commentsLayout.addComponent(new Label(" "));
		}

		final Label noCommentLabel = new Label($("TaskTable.details.noComment"));
		noCommentLabel.addStyleName("record-no-comment");
		if (comments.isEmpty()) {
			commentsLayout.addComponent(noCommentLabel);
		}

		for (Comment comment : comments) {
			addComment(comment, commentsLayout);
		}
		return commentsLayout;
	}

	protected Component newCommentForm(final Comment newComment, final Window window,
									   final VerticalLayout commentsLayout) {
		BaseTextArea commentField = new BaseTextArea();
		commentField.setWidth("100%");
		FieldAndPropertyId commentFieldAndPropertyId = new FieldAndPropertyId(commentField, "message");
		BaseForm<Comment> commentForm = new BaseForm<Comment>(newComment, Arrays.asList(commentFieldAndPropertyId)) {
			@Override
			protected void saveButtonClick(Comment newComment) throws ValidationException {
				if (commentAdded(newComment)) {
					window.close();
				}
			}

			@Override
			protected void cancelButtonClick(Comment newComment) {
				window.close();
			}
		};
		commentForm.addStyleName("add-record-comment-form");
		return commentForm;
	}

	protected Component newAddCommentComponent(final VerticalLayout commentsLayout) {
		addCommentButton = new WindowButton($("TaskTable.details.addComment"), $("TaskTable.details.addComment"), WindowConfiguration.modalDialog("400px", "280px")) {
			@Override
			protected Component buildWindowContent() {
				Comment newComment = new Comment();
				return newCommentForm(newComment, getWindow(), commentsLayout);
			}
		};
		addCommentButton.setIcon(FontAwesome.PLUS);
		addCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
		addCommentButton.addStyleName("record-add-comment-button");
		addCommentButton.setCaptionVisibleOnMobile(false);
		if (delayedReadOnly != null) {
			addCommentButton.setVisible(!delayedReadOnly);
		}
		return addCommentButton;
	}

	protected void addComment(final Comment comment, VerticalLayout commentsLayout) {
		String userId = comment.getUserId();
		LocalDateTime commentDateTime = comment.getDateTime();
		String commentDateTimeStr = dateTimeConverter.convertToPresentation(commentDateTime, String.class, getLocale());

		Component commentUserComponent = new UserDisplay(userId);
		commentUserComponent.addStyleName("record-comment-user");

		Label commentDateTimeLabel = new Label(commentDateTimeStr);
		commentDateTimeLabel.addStyleName("record-comment-date-time");

		BaseButton deleteCommentButton = new DeleteButton(FontAwesome.TRASH_O, $("delete"), true) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.commentDeleted(comment);
			}
		};
		deleteCommentButton.addStyleName("delete-record-comment-button");
		deleteCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
		deleteCommentButton.setVisible(!isReadOnly() && presenter.commentCreatedByCurrentUser(comment));

		I18NHorizontalLayout userTimeLayout = new I18NHorizontalLayout(commentUserComponent, commentDateTimeLabel, deleteCommentButton);
		userTimeLayout.addStyleName("record-comment-user-date-time");
		userTimeLayout.setSpacing(true);
		String message = comment.getMessage();
		message = StringUtils.replace(message, "\n", "<br/>");
		Label messageLabel = new Label(message, ContentMode.HTML);
		messageLabel.addStyleName("record-comment-message");

		commentsLayout.addComponents(userTimeLayout, messageLabel);
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
