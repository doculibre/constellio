package com.constellio.app.ui.pages.conversations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.pages.extrabehavior.SecurityWithNoUrlParamSupport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.components.conversations.ConversationFacetsHandler;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Conversation;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustMeetRequirements;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

public class DisplayConversationPresenter implements SecurityWithNoUrlParamSupport {

	private final RMSchemasRecordsServices rm;
	private final ConversationFacetsHandler conversationFacetsHandler;

	private boolean paramsValid;

	private String conversationId;
	private Conversation conversation;

	private String targetedMessageId;
	private Message targetedMessage;

	public DisplayConversationPresenter(String collection, SessionContext sessionContext,
										Map<String, String> paramsMap) {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstanceIfAlreadyStarted().getAppLayerFactory();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		validateParams(paramsMap);

		conversationFacetsHandler = new ConversationFacetsHandler(collection, sessionContext, appLayerFactory.getModelLayerFactory());
	}

	public boolean validateParams(Map<String, String> paramsMap) {
		paramsValid = true;
		conversation = null;
		targetedMessage = null;

		TreeMap<String, String> caseInsensitiveParamsMap = new TreeMap<>();
		paramsMap.forEach((key, value) -> {
			caseInsensitiveParamsMap.put(key.toUpperCase(), value);
		});

		conversationId = caseInsensitiveParamsMap.get(DisplayConversationView.CONVERSATION_ID_PARAM_KEY);
		if (conversationId != null) {
			try {
				Optional<Conversation> optionalConversation = rm.searchConversations(where(Schemas.IDENTIFIER).is(conversationId)).stream().findAny();
				optionalConversation.ifPresent(value -> conversation = value);
			} catch (WrappedRecordMustMeetRequirements exception) {
				paramsValid = false;
			}
		}
		paramsValid &= conversation != null;

		targetedMessageId = caseInsensitiveParamsMap.get(DisplayConversationView.TARGETED_MESSAGE_ID_PARAM_KEY);
		if (targetedMessageId != null) {
			try {
				Optional<Message> optionalTargetedMessage = rm.searchMessages(where(Schemas.IDENTIFIER).is(targetedMessageId)).stream().findFirst();
				if (optionalTargetedMessage.isPresent()) {
					targetedMessage = optionalTargetedMessage.get();
				} else {
					paramsValid = false;
				}
			} catch (WrappedRecordMustMeetRequirements exception) {
				paramsValid = false;
			}
		}

		return isParamsValid();
	}

	public String getConversationId() {
		return conversationId;
	}

	public Conversation getConversation() {
		return conversation;
	}


	public String getTargetedMessageId() {
		return targetedMessageId;
	}

	public Message getTargetedMessage() {
		return targetedMessage;
	}

	public boolean isParamsValid() {
		return paramsValid;
	}

	public ConversationFacetsHandler getConversationFacetHandler() {
		return conversationFacetsHandler;
	}

	@Override
	public boolean hasPageAccess(User user) {
		return true;
	}
}
