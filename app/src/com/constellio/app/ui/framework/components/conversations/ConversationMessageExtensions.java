package com.constellio.app.ui.framework.components.conversations;

import com.constellio.app.api.extensions.MetadataDisplayCustomValueExtention;
import com.constellio.app.api.extensions.SchemaDisplayExtension;
import com.constellio.app.api.extensions.SearchPageExtension;
import com.constellio.app.api.extensions.params.MetadataDisplayCustomValueExtentionParams;
import com.constellio.app.api.extensions.params.SchemaDisplayParams;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.extensions.records.RecordAppExtension;
import com.constellio.app.extensions.records.params.GetIconPathParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.ViewWindow;
import com.constellio.app.ui.framework.components.user.UserDisplay;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.conversations.DisplayConversationViewImpl;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

import java.util.Collections;
import java.util.List;

public class ConversationMessageExtensions {
	public static class ConversationMessageSearchPageExtension extends SearchPageExtension {
		final private String collection;
		final private AppLayerFactory appLayerFactory;

		public ConversationMessageSearchPageExtension(String collection, AppLayerFactory appLayerFactory) {
			this.collection = collection;
			this.appLayerFactory = appLayerFactory;
		}

		@Override
		public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {

			if (param.getSchemaType() != null && param.getSchemaType().equals(Message.SCHEMA_TYPE)) {
				final SearchResultVO searchResultVO = param.getSearchResultVO();
				final RecordVO recordVO = searchResultVO.getRecordVO();
				final String schemaCode = recordVO.getSchemaCode();

				ConstellioEIMConfigs configs = new ConstellioEIMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
				return new ConversationMessageSearchResultDisplay(param.getSearchResultVO(), param.getComponentFactory(), appLayerFactory, param.getQuery(), configs.isNoLinksInSearchResults());
			}

			return super.getCustomResultDisplayFor(param);
		}
	}

	public static class ConversationMessageRecordAppExtension extends RecordAppExtension {
		final private String collection;
		final private AppLayerFactory appLayerFactory;

		public ConversationMessageRecordAppExtension(String collection, AppLayerFactory appLayerFactory) {
			this.collection = collection;
			this.appLayerFactory = appLayerFactory;
		}

		@Override
		public Resource getThumbnailResourceForRecordVO(GetIconPathParams params) {
			Resource result;
			RecordVO recordVO = params.getRecordVO();

			if (isRecordAMessage(recordVO)) {
				String messaheAuthorId = recordVO.get(Message.MESSAGE_AUTHOR);
				UserDisplay userDisplay = new UserDisplay(messaheAuthorId);

				result = userDisplay.getIcon();
			} else {
				result = null;
			}

			return result;
		}

		@Override
		public List<String> getThumbnailStylesForRecordVO(GetIconPathParams params) {
			List<String> styles;
			RecordVO recordVO = params.getRecordVO();

			if (isRecordAMessage(recordVO)) {
				styles = Collections.singletonList("search-message-result-thumbnail");
			} else {
				styles = null;
			}

			return styles;
		}
	}

	public static class ConversationMessageSchemaDisplayExtension extends SchemaDisplayExtension {
		private final AppLayerFactory appLayerFactory;

		public ConversationMessageSchemaDisplayExtension(
				AppLayerFactory appLayerFactory) {
			this.appLayerFactory = appLayerFactory;
		}

		@Override
		public Component getDisplay(SchemaDisplayParams schemaDisplayParams) {
			Component display;
			RecordVO recordVO = schemaDisplayParams.getRecordVO();

			if (isRecordAMessage(recordVO)) {
				display = buildSchemaDisplay(schemaDisplayParams.getRecordVO());
			} else {
				display = null;
			}

			return display;
		}

		@Override
		public ViewWindow getWindowDisplay(SchemaDisplayParams schemaDisplayParams) {
			ViewWindow viewWindow;
			RecordVO recordVO = schemaDisplayParams.getRecordVO();

			if (isRecordAMessage(recordVO)) {
				try {
					viewWindow = new ConversationMessageViewWindow(new DisplayConversationViewImpl() {
						@Override
						protected Component buildMainComponent(ViewChangeEvent event) {
							return buildSchemaDisplay(recordVO);
						}
					});
				} catch (UserDoesNotHaveAccessException e) {
					viewWindow = null;
				}
			} else {
				viewWindow = null;
			}

			return viewWindow;
		}

		private Component buildSchemaDisplay(RecordVO recordVO) {
			Record record = recordVO.getRecord();

			SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();

			String conversationId = recordVO.get(Message.CONVERSATION);
			String messageId = recordVO.getId();
			String parentId = recordVO.get(Message.MESSAGE_PARENT);
			ConversationFacetsHandler conversationFacetsHandler = new ConversationFacetsHandler(record.getCollection(), sessionContext, appLayerFactory.getModelLayerFactory());

			return new ConversationViewImpl(record.getCollection(), sessionContext, conversationFacetsHandler, conversationId, messageId) {
				@Override
				public String getParentMessageId() {
					return parentId;
				}
			};
		}


		private class ConversationMessageViewWindow extends ViewWindow {
			public ConversationMessageViewWindow(BaseViewImpl view) throws UserDoesNotHaveAccessException {
				super(view);
			}
		}
	}

	public static class ConversationMessageMetadataDisplayCustomValueExtention extends MetadataDisplayCustomValueExtention {
		@Override
		public Object getCustomDisplayValue(
				MetadataDisplayCustomValueExtentionParams metadataDisplayCustomValueExtentionParams) {

			Object result = null;
			RecordVO recordVO = metadataDisplayCustomValueExtentionParams.getRecordVO();
			MetadataVO metadataVO = metadataDisplayCustomValueExtentionParams.getMetadataVO();

			if (isRecordAMessage(recordVO)) {

				if (Message.MESSAGE_BODY_TYPE.equals(metadataVO.getLocalCode())) {
					result = new Label((String) recordVO.get(metadataVO));
				}
			}

			return result;
		}
	}

	private static boolean isRecordAMessage(RecordVO recordVO) {
		String schemaCode = recordVO.getSchemaCode();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);

		return Message.SCHEMA_TYPE.equals(schemaTypeCode);
	}
}
