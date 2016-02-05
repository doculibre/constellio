package com.constellio.app.ui.framework.components.display;

import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.ComponentListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnComponentEvent;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.RecordVOToCaptionConverter;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

public class ReferenceDisplay extends Button {
	public static final String STYLE_NAME = "reference-display";
	private RecordVO recordVO;
	private String recordId;
	private RecordContextMenu contextMenu;

	public ReferenceDisplay(RecordVO recordVO) {
		this.recordVO = recordVO;
		String caption = new RecordVOToCaptionConverter().convertToPresentation(recordVO, String.class, getLocale());
		Resource icon = FileIconUtils.getIcon(recordVO);
		if (icon != null) {
			setIcon(icon);
		}
		setCaption(caption);
		init(recordVO);
	}

	public ReferenceDisplay(String recordId) {
		this.recordId = recordId;
		String caption = new RecordIdToCaptionConverter().convertToPresentation(recordId, String.class, getLocale());
		if (recordId != null) {
			Resource icon = FileIconUtils.getIconForRecordId(recordId);
			if (icon != null) {
				setIcon(icon);
			}
		}
		setCaption(caption);
		init(recordId);
	}

	private void init(RecordVO recordVO) {
		setSizeFull();
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);
		setEnabled(false);
		prepareLink();

		String niceTitle = recordVO.getNiceTitle();
		if (niceTitle != null) {
			addExtension(new NiceTitle(this, niceTitle));
		}
	}

	private void init(String recordId) {
		setSizeFull();
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);
		setEnabled(false);
		prepareLink();
		//		addContextMenu();

		if (recordId != null) {
			ConstellioUI ui = ConstellioUI.getCurrent();
			String collection = ui.getSessionContext().getCurrentCollection();
			ModelLayerFactory modelLayerFactory = ui.getConstellioFactories().getModelLayerFactory();
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			RecordServices recordServices = modelLayerFactory.newRecordServices();

			String niceTitle = getNiceTitle(recordServices.getDocumentById(recordId), types);
			addExtension(new NiceTitle(this, niceTitle));
		}

	}

	protected String getNiceTitle(Record record, MetadataSchemaTypes types) {
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		String description = null;
		if (schema.hasMetadataWithCode("description")) {
			Metadata descriptionMetadata = schema.getMetadata("description");
			description = record.get(descriptionMetadata);
		}
		return description;
	}

	protected void prepareLink() {
		final ConstellioUI ui = ConstellioUI.getCurrent();
		String collection = ui.getSessionContext().getCurrentCollection();
		AppLayerFactory appLayerFactory = ui.getConstellioFactories().getAppLayerFactory();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<RecordNavigationExtension> recordNavigationExtensions = extensions.recordNavigationExtensions.getExtensions();

		NavigationParams navigationParams = null;
		if (recordVO != null) {
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(recordVO.getSchema().getCode());
			navigationParams = new NavigationParams(ui.navigateTo(), recordVO, schemaTypeCode, Page.getCurrent(),
					this);
		} else if (recordId != null) {
			Record record = recordServices.getDocumentById(recordId);
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
			navigationParams = new NavigationParams(ui.navigateTo(), recordId, schemaTypeCode, Page.getCurrent(),
					this);
		}
		if (navigationParams != null) {
			for (final RecordNavigationExtension recordNavigationExtension : recordNavigationExtensions) {
				recordNavigationExtension.prepareLinkToView(navigationParams);
			}
		}
	}

	protected void addContextMenu() {
		List<RecordContextMenuHandler> recordContextMenuHandlers = ConstellioUI.getCurrent().getRecordContextMenuHandlers();
		for (final RecordContextMenuHandler recordContextMenuHandler : recordContextMenuHandlers) {
			if (recordId != null && recordContextMenuHandler.isContextMenuForRecordId(recordId)) {
				contextMenu = recordContextMenuHandler.getForRecordId(recordId);
				break;
			} else if (recordVO != null && recordContextMenuHandler.isContextMenu(recordVO)) {
				contextMenu = recordContextMenuHandler.get(recordVO);
				break;
			}
		}
		if (contextMenu != null) {
			contextMenu.setAsContextMenuOf(this);
			contextMenu.addContextMenuComponentListener(new ComponentListener() {
				@Override
				public void onContextMenuOpenFromComponent(ContextMenuOpenedOnComponentEvent event) {
					if (recordId != null) {
						contextMenu.openFor(recordId);
					} else if (recordVO != null) {
						contextMenu.openFor(recordVO);
					}
				}
			});
		}
	}
}
