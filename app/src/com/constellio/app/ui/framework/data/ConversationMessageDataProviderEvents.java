package com.constellio.app.ui.framework.data;

import com.constellio.app.events.EventArgs;
import com.constellio.app.events.EventListener;
import com.constellio.app.events.EventObservable;
import com.constellio.model.entities.records.wrappers.Message;

import java.util.List;

public class ConversationMessageDataProviderEvents {
	public static class DataRefreshed extends EventArgs<ConversationMessageDataProvider> {
		private final List<Message> messages;

		public DataRefreshed(ConversationMessageDataProvider sender, List<Message> messages) {
			super(sender);
			this.messages = messages;
		}

		public List<Message> getMessages() {
			return messages;
		}

		public Message getTargetedMessage() {
			return null;
		}

		public boolean isNewContext() {
			return false;
		}

		public boolean isAddedAfterTargetedMessage() {
			return true;
		}

		public interface DataRefreshedListener extends EventListener<DataRefreshed> {
		}

		public static class DataRefreshedObservable extends EventObservable<DataRefreshed> {
		}
	}
}
