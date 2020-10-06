package com.constellio.app.ui.framework.components.fields.comment;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseForm.FieldAndPropertyId;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.framework.components.user.UserDisplay;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class CommentsLayout extends VerticalLayout {

	private JodaDateTimeToStringConverter dateTimeConverter = new JodaDateTimeToStringConverter();

	List<Comment> comments;
	String caption;
	boolean isTableMode;

	public CommentsLayout(List<Comment> comments, String caption, boolean isTableMode) {
		this.comments = comments;
		this.caption = caption;
		this.isTableMode = isTableMode;
		init();
	}

	private void init() {
		if (isTableMode) {
			setCaption(caption);
			addStyleName("task-details-comments");
		}
		setWidth("100%");
		setSpacing(true);
		addStyleName("record-comments");

		if (addCommentButtonVisible()) {
			Component addCommentsComponent = newCommentComponent($("RecordCommentsDisplayImpl.addComment"), $("RecordCommentsDisplayImpl.addComment"), FontAwesome.PLUS, null);
			addComponent(addCommentsComponent);
			setComponentAlignment(addCommentsComponent, Alignment.TOP_RIGHT);
		}

		final Label noCommentLabel = new Label($("RecordCommentsDisplayImpl.noComment"));
		noCommentLabel.addStyleName("record-no-comment");
		if (comments.isEmpty()) {
			addComponent(noCommentLabel);
		}
		for (Comment comment : comments) {
			addComment(comment);
		}
	}

	public void addComment(Comment comment) {
		String userId = comment.getUserId();
		LocalDateTime commentDateTime = comment.getCreationDateTime();
		String commentDateTimeStr = dateTimeConverter.convertToPresentation(commentDateTime, String.class, getLocal());

		Component commentUserComponent = new UserDisplay(userId);
		commentUserComponent.addStyleName("record-comment-user");

		Label commentDateTimeLabel = new Label(commentDateTimeStr);
		commentDateTimeLabel.addStyleName("record-comment-date-time");

		LinkButton modificationInformation = new LinkButton("Modifié") {
			@Override
			protected void buttonClick(ClickEvent event) {

			}
		};
		boolean commentModified = comment.getModificationDateTime() != null;
		modificationInformation.setVisible(commentModified);
		if (commentModified) {
			modificationInformation.addExtension(new NiceTitle($("CommentsLayout.modifiedOn") + " " + dateTimeConverter.convertToPresentation(comment.getModificationDateTime(), String.class, getLocal())));
		}

		Component editCommentButton = newCommentComponent($("edit"), $("RecordCommentsDisplayImpl.editComment"), FontAwesome.EDIT, comment);
		editCommentButton.addStyleName("edit-record-comment-button");
		editCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
		editCommentButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
		editCommentButton.setVisible(editButtonVisible(comment));

		BaseButton deleteCommentButton = new DeleteButton(FontAwesome.TRASH_O, $("delete"), true) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				commentDeleted(comment);
			}
		};
		deleteCommentButton.addStyleName("delete-record-comment-button");
		deleteCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
		deleteCommentButton.setVisible(deleteButtonVisible(comment) && !isReadOnly());

		I18NHorizontalLayout userTimeLayout = new I18NHorizontalLayout(commentUserComponent, commentDateTimeLabel, modificationInformation, editCommentButton, deleteCommentButton);
		userTimeLayout.addStyleName("record-comment-user-date-time");
		userTimeLayout.setSpacing(true);
		userTimeLayout.setComponentAlignment(editCommentButton, Alignment.MIDDLE_CENTER);
		userTimeLayout.setComponentAlignment(deleteCommentButton, Alignment.MIDDLE_CENTER);

		String message = comment.getMessage();
		message = StringUtils.replace(message, "\n", "<br/>");
		Label messageLabel = new Label(message, ContentMode.HTML);
		messageLabel.addStyleName("record-comment-message");
		addComponents(userTimeLayout, messageLabel);
	}

	protected Component newCommentComponent(String caption, String windowCaption, Resource icon, Comment comment) {
		WindowButton addEditCommentButton = new WindowButton(caption, windowCaption, WindowConfiguration.modalDialog("400px", "260px")) {
			@Override
			protected Component buildWindowContent() {
				if (comment == null) {
					Comment comment = new Comment();
					return newCommentForm(comment, getWindow(), false);
				} else {
					return newCommentForm(comment, getWindow(), true);
				}
			}
		};
		addEditCommentButton.setIcon(icon);
		addEditCommentButton.addStyleName("task-details-add-comment-button");
		addEditCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
		addEditCommentButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		addEditCommentButton.setCaptionVisibleOnMobile(false);
		addEditCommentButton.addExtension(new NiceTitle(caption));
		return addEditCommentButton;
	}

	protected Component newCommentForm(Comment comment, Window window, boolean isEdit) {
		BaseTextArea commentField = new BaseTextArea();
		commentField.setWidth("100%");
		FieldAndPropertyId commentFieldAndPropertyId = new FieldAndPropertyId(commentField, "message");
		BaseForm<Comment> commentForm = new BaseForm<Comment>(comment, Arrays.asList(commentFieldAndPropertyId)) {
			@Override
			protected void saveButtonClick(Comment comment) {
				if (isEdit) {
					commentModified(comment, commentField.getValue());
				} else {
					commentAdded(comment);
				}
				window.close();
			}

			@Override
			protected void cancelButtonClick(Comment newComment) {
				window.close();
			}
		};
		commentForm.addStyleName("comment-form");
		return commentForm;
	}

	protected abstract Locale getLocal();

	protected abstract void commentDeleted(Comment comment);

	protected abstract void commentModified(Comment comment, String value);

	protected abstract void commentAdded(Comment comment);

	protected abstract boolean deleteButtonVisible(Comment comment);

	protected abstract boolean editButtonVisible(Comment comment);

	protected abstract boolean addCommentButtonVisible();

}
