package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.app.ui.framework.components.conversations.ConversationMessageList.ListRefreshedArgs.ListRefreshedListener;
import com.constellio.app.ui.framework.data.ConversationMessageDataProvider;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed.DataRefreshedListener;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.Message;
import com.vaadin.ui.Component;

import java.util.List;

public interface ConversationMessageList extends Component, DataRefreshedListener {
	enum WhatToLoadWhenNoTargetedMessage {
		LOAD_OLDEST_MESSAGES,
		LOAD_NEWEST_MESSAGE
	}

	ConversationMessageDataProvider getDataProvider();

	SessionContext getSessionContext();

	void addListRefreshedListener(ListRefreshedListener listener);

	void removeListRefreshedListener(ListRefreshedListener listener);

	class ConversationMessageListArgs {
		private final String collection;
		private final String conversationId;
		private final SessionContext sessionContext;
		private final ConversationFacetsHandler conversationFacetsHandler;

		public ConversationMessageListArgs(String collection, SessionContext sessionContext,
										   ConversationFacetsHandler conversationFacetsHandler,
										   Conversation conversation) {
			this(collection, sessionContext, conversationFacetsHandler, conversation.getId());
		}

		public ConversationMessageListArgs(String collection, SessionContext sessionContext,
										   ConversationFacetsHandler conversationFacetsHandler, String conversationId) {
			this.collection = collection;
			this.sessionContext = sessionContext;
			this.conversationId = conversationId;
			this.conversationFacetsHandler = conversationFacetsHandler;
		}


		public String getCollection() {
			return collection;
		}

		public SessionContext getSessionContext() {
			return sessionContext;
		}

		public ConversationFacetsHandler getConversationFacetsHandler() {
			return conversationFacetsHandler;
		}

		public String getConversationId() {
			return conversationId;
		}

		public String getParentMessageId() {
			return null;
		}

		public String getTargetedMessageId() {
			return null;
		}

		public String getQuerySelectorForScrollablePanel() {
			return "#content-footer-wrapper";
		}

		public WhatToLoadWhenNoTargetedMessage getWhatToLoadWhenNoTargetedMessage() {
			return WhatToLoadWhenNoTargetedMessage.LOAD_NEWEST_MESSAGE;
		}

	}

	class ListRefreshedArgs extends EventArgs<ConversationMessageList> {
		private final DataRefreshed dataRefreshed;

		public ListRefreshedArgs(ConversationMessageList sender, DataRefreshed dataRefreshed) {
			super(sender);

			this.dataRefreshed = dataRefreshed;
		}

		public List<Message> getMessages() {
			return dataRefreshed.getMessages();
		}

		public Message getTargetedMessage() {
			return dataRefreshed.getTargetedMessage();
		}

		public boolean isNewContext() {
			return dataRefreshed.isNewContext();
		}

		public boolean isAddedAfterTargetedMessage() {
			return dataRefreshed.isAddedAfterTargetedMessage();
		}

		interface ListRefreshedListener extends EventListener<ListRefreshedArgs> {
		}

		public static class ListRefreshedObservable extends EventObservable<ListRefreshedArgs> {
		}
	}
}
