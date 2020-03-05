package com.constellio.app.entities.navigation;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenu;
import com.constellio.app.ui.framework.data.RecordLazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Component;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class PageItem implements CodedItem, Serializable {
	public enum Type {RECENT_ITEM_TABLE, RECORD_TABLE, SHARED_ITEMS_TABLES, RECORD_TREE, CUSTOM_ITEM}

	private final String code;
	private final Type type;

	protected PageItem(String code, Type type) {
		this.code = code;
		this.type = type;
	}

	@Override
	public String getCode() {
		return code;
	}

	public Type getType() {
		return type;
	}

	public static abstract class RecentItemTable extends PageItem {

		private String schemaType;

		protected RecentItemTable(String code, String schemaType) {
			super(code, Type.RECENT_ITEM_TABLE);
			this.schemaType = schemaType;
		}

		public String getSchemaType() {
			return schemaType;
		}

		public abstract List<RecentItem> getItems(AppLayerFactory appLayerFactory, SessionContext sessionContext);

		public static class RecentItem implements Serializable {
			public static final String CAPTION = "title";
			public static final String LAST_ACCESS = "lastAccess";

			private final RecordVO record;
			private final String title;

			public RecentItem(RecordVO record, String caption) {
				this.record = record;
				this.title = caption;
			}

			public RecordVO getRecord() {
				return record;
			}

			public String getTitle() {
				return title;
			}

			public String getId() {
				return record.getId();
			}

			public LocalDateTime getLastAccess() {
				return record.get(CommonMetadataBuilder.MODIFIED_ON);
			}
		}
	}

	public static abstract class RecordTable extends PageItem {
		public RecordTable(String code) {
			super(code, Type.RECORD_TABLE);
		}

		public abstract RecordVODataProvider getDataProvider(
				AppLayerFactory appLayerFactory, SessionContext sessionContext);
	}

	public static abstract class SharedItemsTables extends PageItem {

		public SharedItemsTables(String code) {
			super(code, Type.SHARED_ITEMS_TABLES);
		}

		public abstract Map<String, RecordVODataProvider> getDataProvider(
				AppLayerFactory appLayerFactory, SessionContext sessionContext);
	}

	public static abstract class RecordTree extends PageItem {

		private int defaultDataProvider = -1;

		private List<String> expandedRecordIds = Collections.emptyList();

		public RecordTree(String code) {
			super(code, Type.RECORD_TREE);
		}

		public int getDefaultDataProvider() {
			return defaultDataProvider;
		}

		public void setDefaultDataProvider(int defaultDataProvider) {
			this.defaultDataProvider = defaultDataProvider;
		}

		public List<String> getExpandedRecordIds() {
			return expandedRecordIds;
		}

		public void setExpandedRecordIds(List<String> expandedRecordIds) {
			this.expandedRecordIds = expandedRecordIds;
		}

		public abstract List<RecordLazyTreeDataProvider> getDataProviders(
				AppLayerFactory appLayerFactory, SessionContext sessionContext);

		public abstract BaseContextMenu getContextMenu();

	}

	public static abstract class CustomItem extends PageItem {
		public CustomItem(String code) {
			super(code, Type.CUSTOM_ITEM);
		}

		public abstract Component buildCustomComponent(ConstellioFactories factories, SessionContext context,
													   ItemClickEvent.ItemClickListener itemClickListener);

		public ComponentState getStateFor(User user, AppLayerFactory modelLayerFactory) {
			return ComponentState.visibleIf(true);
		}
	}
}
