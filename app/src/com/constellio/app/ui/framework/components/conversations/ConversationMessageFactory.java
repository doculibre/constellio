package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMMessage;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Message;

public class ConversationMessageFactory {
	private final SessionContext sessionContext;
	private final ConversationFacetsHandler conversationFacetsHandler;

	public ConversationMessageFactory(SessionContext sessionContext,
									  ConversationFacetsHandler conversationFacetsHandler) {
		this.sessionContext = sessionContext;
		this.conversationFacetsHandler = conversationFacetsHandler;
	}

	ConversationMessage build(Record record, AppLayerFactory appLayerFactory) {
		return build(record, appLayerFactory, VIEW_MODE.DISPLAY);
	}

	ConversationMessage build(Record record, AppLayerFactory appLayerFactory, VIEW_MODE viewMode) {
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(record.getCollection(), appLayerFactory);

		return build(rmSchemasRecordsServices.wrapMessage(record), viewMode);
	}

	ConversationMessage build(Message message) {
		return build(RMMessage.wrapFromMessage(message), VIEW_MODE.DISPLAY);
	}

	ConversationMessage build(Message message, VIEW_MODE viewMode) {
		return build(RMMessage.wrapFromMessage(message), viewMode);
	}

	ConversationMessage build(RMMessage message) {
		return build(message, VIEW_MODE.DISPLAY);
	}

	ConversationMessage build(RMMessage message, VIEW_MODE viewMode) {
		ConversationMessage conversationMessage = new ConversationMessage(message, sessionContext, conversationFacetsHandler);

		if (VIEW_MODE.SEARCH.equals(viewMode)) {
			conversationMessage.setCompact(true);
		}

		return conversationMessage;
	}
}
