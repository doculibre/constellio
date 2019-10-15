package com.constellio.app.ui.framework.components.display;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenu;
import com.constellio.app.ui.framework.components.contextmenu.RecordContextMenuHandler;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.RecordVOToCaptionConverter;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedListener.ComponentListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnComponentEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReferenceDisplay extends Button {

	public static final String STYLE_NAME = "reference-display";
	private RecordVO recordVO;
	private String recordId;
	private RecordContextMenu contextMenu;
	private boolean openLinkInNewTab = false;
	private Map<String,String> extraParameters = new HashMap<>();

	public ReferenceDisplay(RecordVO recordVO) {
		this(recordVO, true);
	}

	public ReferenceDisplay(RecordVO recordVO, boolean link) {
		this(recordVO, link,  null);
	}

	public ReferenceDisplay(RecordVO recordVO, boolean link, Map<String, String> extraParameters) {
		this.recordVO = recordVO;
		String caption = new RecordVOToCaptionConverter().convertToPresentation(recordVO, String.class, getLocale());
		this.extraParameters = extraParameters;

		Resource icon = FileIconUtils.getIcon(recordVO);
		if (icon != null) {
			setIcon(icon);
		}
		setCaption(caption);
		init(recordVO, link);
	}

	public ReferenceDisplay(String recordId) {
		this(recordId, true);
	}

	public ReferenceDisplay(String recordId, boolean link) {
		this(recordId, link, new RecordIdToCaptionConverter());
	}

	public ReferenceDisplay(String recordId, boolean link, Converter<String, String> captionConverter) {
		this.recordId = recordId;
		String caption = captionConverter.convertToPresentation(recordId, String.class, getLocale());
		if (recordId != null) {
			Resource icon = FileIconUtils.getIconForRecordId(recordId);
			if (icon != null) {
				setIcon(icon);
			}
		}
		setCaption(caption);
		init(recordId, link);
	}

	public Map<String, String> getExtraParameters() {
		return extraParameters;
	}

	public void setExtraParameters(Map<String, String> extraParameters) {
		this.extraParameters = extraParameters;
	}

	@Override
	public Locale getLocale() {
		Locale locale = super.getLocale();
		if (locale == null) {
			locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		}
		return locale;
	}

	private void init(RecordVO recordVO, boolean link) {
		setSizeFull();
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);
		setEnabled(false);
		if (link) {
			prepareLink();
		}

		String niceTitle = recordVO.getNiceTitle();
		if (niceTitle != null) {
			addExtension(new NiceTitle(niceTitle));
		}
	}

	private void init(String recordId, boolean link) {
		setSizeFull();
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);
		setEnabled(false);
		if (link) {
			prepareLink();
		}
		//		addContextMenu();

		if (recordId != null) {
			ConstellioUI ui = ConstellioUI.getCurrent();
			String collection = ui.getSessionContext().getCurrentCollection();
			ModelLayerFactory modelLayerFactory = ui.getConstellioFactories().getModelLayerFactory();
			MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			RecordServices recordServices = modelLayerFactory.newRecordServices();

			try {
				String niceTitle = getNiceTitle(recordServices.getDocumentById(recordId), types);
				addExtension(new NiceTitle(niceTitle));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				e.printStackTrace();
			}
		}

	}

	protected String getNiceTitle(Record record, MetadataSchemaTypes types) {
		MetadataSchema schema = types.getSchemaOf(record);
		String description = null;
		if (schema.hasMetadataWithCode("description")) {
			Metadata descriptionMetadata = schema.getMetadata("description");
			description = record.get(descriptionMetadata, getLocale());
		}
		return description;
	}

	protected void prepareLink() {
		final ConstellioUI ui = ConstellioUI.getCurrent();
		SessionContext sessionContext = ui.getSessionContext();
		String collection = sessionContext.getCurrentCollection();
		AppLayerFactory appLayerFactory = ui.getConstellioFactories().getAppLayerFactory();
		RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<RecordNavigationExtension> recordNavigationExtensions = extensions.recordNavigationExtensions.getExtensions();
		boolean isRecordInTrash = false;

		NavigationParams navigationParams = null;
		if (recordVO != null) {
			String schemaTypeCode = SchemaUtils.getSchemaTypeCode(recordVO.getSchema().getCode());
			navigationParams = new NavigationParams(ui.navigate(), recordVO, schemaTypeCode, Page.getCurrent(),
					this);
			//isRecordInTrash = Boolean.TRUE.equals(recordServices.getDocumentById(recordVO.getId()).get(Schemas.LOGICALLY_DELETED_STATUS));
			isRecordInTrash = Boolean.TRUE.equals(recordVO.get(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode()));
		} else if (recordId != null) {
			try {
				Record record = recordServices.getDocumentById(recordId);
				String schemaTypeCode = SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
				navigationParams = new NavigationParams(ui.navigate(), recordId, schemaTypeCode, Page.getCurrent(),
						this);
				isRecordInTrash = Boolean.TRUE.equals(record.get(Schemas.LOGICALLY_DELETED_STATUS));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				e.printStackTrace();
			}
		}
		if (navigationParams != null) {
			boolean activeLink = false;
			for (final RecordNavigationExtension recordNavigationExtension : recordNavigationExtensions) {
				boolean activeLinkForExtension = recordNavigationExtension.prepareLinkToView(navigationParams, isRecordInTrash, sessionContext.getCurrentLocale());
				if (!activeLink && activeLinkForExtension) {
					activeLink = true;
				}
			}
			if (activeLink && isEnabled()) {
				addActiveLinkStyle();
			}
			
			// Mark as visited
			Component component = navigationParams.getComponent();
			if (component instanceof Button) {
				Button button = (Button) component;
				String id = recordVO != null ? recordVO.getId() : recordId;
				if (id != null && sessionContext.isVisited(id)) {
					button.addStyleName("visited-link");
				}
			}	
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (!enabled) {
			removeActiveLinkStyle();
		}
	}

	private void addActiveLinkStyle() {
		addStyleName(STYLE_NAME + "-link");
	}

	private void removeActiveLinkStyle() {
		removeStyleName(STYLE_NAME + "-link");
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

	public String getRecordId() {
		return recordId;
	}

	public ReferenceDisplay withOpenLinkInNewTab(boolean openLinkInNewTab) {
		this.openLinkInNewTab = openLinkInNewTab;
		return this;
	}

	public boolean isOpenLinkInNewTab() {
		return openLinkInNewTab;
	}

	public void disableClicks() {
		// TODO Auto-generated method stub

	}
}
