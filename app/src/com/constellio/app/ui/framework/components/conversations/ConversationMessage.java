package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.modules.rm.wrappers.RMMessage;
import com.constellio.app.ui.framework.components.ExpandableDisplay;
import com.constellio.app.ui.framework.components.ExpandableDisplay.ExpansionControlsLocation;
import com.constellio.app.ui.framework.components.ExpandableTextDisplay;
import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.ConversationMessageListArgs;
import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.WhatToLoadWhenNoTargetedMessage;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.user.UserDisplay;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Message;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.constellio.app.ui.i18n.i18n.$;


public class ConversationMessage extends CustomComponent {
	private final String CSS_ROOT = "conversation-message";
	private final String CSS_MAIN_LAYOUT = CSS_ROOT + "-main-layout";
	private final String CSS_CONTENT_HEADER = CSS_ROOT + "-header";
	private final String CSS_ROOT_COMPACT = CSS_ROOT + "-compact";
	private final String CSS_HIGHLIGHTED = "highlighted";
	private final String CSS_REPLIES = CSS_ROOT + "-replies";
	private final String CSS_MESSAGE_BODY = CSS_ROOT + "-body";

	private final int BODY_MESSAGE_HEIGHT_DEFAULT = 100;
	private final int BODY_MESSAGE_HEIGHT_WHEN_COMPACT = 40;

	private final Message message;
	private final SessionContext sessionContext;
	private final ConversationFacetsHandler conversationFacetsHandler;

	private VerticalLayout mainLayout;

	private boolean highlighted;

	private boolean isCompact;

	public ConversationMessage(Message message, SessionContext sessionContext,
							   ConversationFacetsHandler conversationFacetsHandler) {
		this.sessionContext = sessionContext;
		this.message = message;
		this.conversationFacetsHandler = conversationFacetsHandler;

		setStyleName(CSS_ROOT);
		Responsive.makeResponsive(this);
	}

	@Override
	public void attach() {
		super.attach();
		setCompositionRoot(buildComponent());
	}

	@Override
	public void detach() {
		super.detach();
	}

	private Component buildComponent() {

		mainLayout = new VerticalLayout();

		mainLayout.addStyleName(CSS_MAIN_LAYOUT);
		if (isHighlighted()) {
			mainLayout.addStyleName(CSS_HIGHLIGHTED);
		}

		mainLayout.setSizeUndefined();

		mainLayout.addComponents(
				buildMessageHeader(),
				buildMessageBody());

		Component extrafields = buildExtraFields();
		if (extrafields != null) {
			mainLayout.addComponent(extrafields);
		}

		Component replies = buildReplies();
		if (replies != null) {
			mainLayout.addComponents(replies);
		}

		return mainLayout;
	}

	private Component buildMessageHeader() {
		Layout messageHeader = new I18NHorizontalLayout();

		messageHeader.addStyleName(CSS_CONTENT_HEADER);

		UserDisplay userDisplay = new UserDisplay(message.getMessageAuthor());
		userDisplay.setSizeUndefined();
		messageHeader.addComponents(userDisplay);

		JodaDateTimeToStringConverter dateTimeConverter = new JodaDateTimeToStringConverter();
		LocalDateTime commentDateTime = message.getCreatedOn();
		String commentDateTimeStr = dateTimeConverter.convertToPresentation(commentDateTime, String.class, getLocale());
		Label commentDateTimeLabel = new Label(commentDateTimeStr);
		userDisplay.setSizeUndefined();
		messageHeader.addComponents(commentDateTimeLabel);

		return messageHeader;
	}

	private Component buildMessageBody() {
		ExpandableTextDisplay messageBody = new ExpandableTextDisplay(message.getMessageBody());

		messageBody.addStyleName(CSS_MESSAGE_BODY);

		messageBody.setMaxHeightBeforeNeedsToExpand(isCompact ? BODY_MESSAGE_HEIGHT_WHEN_COMPACT : BODY_MESSAGE_HEIGHT_DEFAULT);
		messageBody.setExpansionControlsLocation(isCompact ? ExpansionControlsLocation.NotVisible : ExpandableTextDisplay.CONTROLS_LOCATION_DEFAULT);

		return messageBody;
	}

	private Component buildExtraFields() {
		RMMessage rmMessage = RMMessage.wrapFromMessage(message);

		VerticalLayout layout = new VerticalLayout();

		List<String> linkedDocuments = rmMessage.getLinkedDocuments();
		if (!linkedDocuments.isEmpty()) {
			String linkedDocumentCaption = "ConversationMessage.extraField.linkedDocument";
			if (linkedDocuments.size() > 1) {
				linkedDocumentCaption += ".multiple";
			}

			layout.addComponent(new ConversationMessageExtraFieldImpl(
					$(linkedDocumentCaption), () -> linkedDocuments, ReferenceDisplay::new)
			);
		}

		return layout.getComponentCount() > 0 ? layout : null;
	}

	private Component buildReplies() {

		ExpandableDisplay expandableReplies = null;

		if (message.getMessageReplyCount() > 0) {
			final VerticalLayout layout = new VerticalLayout();
			final AtomicBoolean loadedOnce = new AtomicBoolean(false);

			String multipleRepliesCaption = message.getMessageReplyCount() > 1 ? ".multiple" : "";

			expandableReplies = new ExpandableDisplay(0, ExpansionControlsLocation.BeforeMainContent) {
				@Override
				public Component buildMainComponent() {
					return layout;
				}

				@Override
				public String getShowMoreCaption() {
					return $("ConversationMessage.replies.replies.show" + multipleRepliesCaption, message.getMessageReplyCount());
				}

				@Override
				public String getShowLessCaption() {
					return $("ConversationMessage.replies.replies.hide" + multipleRepliesCaption, message.getMessageReplyCount());
				}
			};

			expandableReplies.addToggleToToggledViewListener(event -> {
				if (!loadedOnce.get()) {

					ConversationMessageListImpl replies = new ConversationMessageListImpl(new ConversationMessageListArgs(
							message.getCollection(), sessionContext, conversationFacetsHandler, message.getConversation()) {
						@Override
						public String getParentMessageId() {
							return message.getId();
						}

						@Override
						public WhatToLoadWhenNoTargetedMessage getWhatToLoadWhenNoTargetedMessage() {
							return WhatToLoadWhenNoTargetedMessage.LOAD_OLDEST_MESSAGES;
						}
					});

					layout.addComponent(replies);
					loadedOnce.set(true);
				}
			});
		}

		if (expandableReplies != null) {
			expandableReplies.addStyleName(CSS_REPLIES);
		}

		return expandableReplies;
	}

	public Message getMessage() {
		return message;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			this.highlighted = highlighted;

			if (mainLayout != null) {
				if (highlighted) {
					mainLayout.addStyleName(CSS_HIGHLIGHTED);
				} else {
					mainLayout.removeStyleName(CSS_HIGHLIGHTED);
				}
			}
		}
	}

	public boolean isCompact() {
		return isCompact;
	}

	public void setCompact(boolean compact) {
		isCompact = compact;

		setStyleName(CSS_ROOT_COMPACT, compact);
	}
}
