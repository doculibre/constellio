package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.data.ConversationMessageDataProvider;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed.DataRefreshedListener;
import com.constellio.app.ui.framework.data.ConversationMessageDataProviderEvents.DataRefreshed.DataRefreshedObservable;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.List;

public class ConversationMessageListPresenter implements DataRefreshedListener {
	private final DataRefreshedObservable dataRefreshedObservable;

	private ConversationMessageDataProvider dataProvider;

	private Message oldestMessage;
	private Message newestMessage;

	private final String parentMessageId;
	private Message parentMessage;

	private final String conversationId;
	private Conversation conversation;

	private RMSchemasRecordsServices rm;

	public ConversationMessageListPresenter(String collection, SessionContext sessionContext,
											ConversationFacetsHandler conversationFacetsHandler, String conversationId,
											String parentMessageId) {
		this.conversationId = conversationId;
		this.parentMessageId = parentMessageId;
		this.dataRefreshedObservable = new DataRefreshedObservable();

		ConstellioFactories constellioFactories = ConstellioFactories.getInstanceIfAlreadyStarted();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();

		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		dataProvider = new ConversationMessageDataProvider(collection, conversationId, parentMessageId, sessionContext, appLayerFactory, modelLayerFactory, conversationFacetsHandler);
		dataProvider.addDataRefreshedListener(this);
	}

	public void loadMessagesAroundTargetedMessage(Message message) {
		loadMessagesAroundTargetedMessage(message.getId());
	}

	public void loadMessagesAroundTargetedMessage(String messageId) {
		dataProvider.loadMessagesAroundThisMessage(messageId);
	}

	public boolean isOlderMessagesAvailable() {
		return dataProvider.isOlderMessagesThanThisMessageAvailable(getOldestMessage());
	}

	public void loadOlderMessagesThanThisMessage(Message targetedMessage) {
		dataProvider.loadOlderMessagesThanThisMessage(targetedMessage);
	}

	public void loadOldestMessages() {
		//Since newer messages are loaded in order, the first "newer" are really the oldest
		loadNewerMessagesThanThisMessage(null);
	}

	public void loadNewerMessagesThanThisMessage(Message message) {
		dataProvider.loadNewerMessagesThanThisMessage(message);
	}

	public boolean isNewerMessagesAvailable() {
		return dataProvider.isNewerMessagesThanThisMessageAvailable(getNewestMessage());
	}

	public void loadNewestMessages() {
		//Since older messages are loaded in reversed order, the first "older" are really the newest
		loadOlderMessagesThanThisMessage(null);
	}

	private void updateNewestMessageFromMessages(List<Message> messages) {
		setNewestMessage(!messages.isEmpty() ? messages.get(messages.size() - 1) : null);
	}

	public void setNewestMessage(Message newestMessage) {
		this.newestMessage = newestMessage;
	}

	public Message getNewestMessage() {
		return newestMessage;
	}

	private void updateOldestMessageFromMessages(List<Message> messages) {
		setOldestMessage(!messages.isEmpty() ? messages.get(0) : null);
	}

	public void setOldestMessage(Message oldestMessage) {
		this.oldestMessage = oldestMessage;
	}

	public Message getOldestMessage() {
		return oldestMessage;
	}

	public Message getParentMessage() {
		if (parentMessage == null) {
			parentMessage = rm.getMessage(parentMessageId);
		}

		return parentMessage;
	}

	public Conversation getConversation() {
		if (conversation == null) {
			conversation = rm.getConversation(conversationId);
		}

		return conversation;
	}

	public ConversationMessageDataProvider getDataProvider() {
		return dataProvider;
	}

	public void addDataRefreshedListener(DataRefreshedListener dataRefreshedListener) {
		dataRefreshedObservable.addListener(dataRefreshedListener);
	}

	public void removeDataRefreshedListener(DataRefreshedListener dataRefreshedListener) {
		dataRefreshedObservable.addListener(dataRefreshedListener);
	}

	@Override
	public void eventFired(DataRefreshed args) {
		List<Message> messages = args.getMessages();

		if (args.isNewContext() || !args.isAddedAfterTargetedMessage()) {
			updateOldestMessageFromMessages(messages);
		}

		if (args.isNewContext() || args.isAddedAfterTargetedMessage()) {
			updateNewestMessageFromMessages(messages);
		}

		dataRefreshedObservable.fire(args);
	}
}
