package com.constellio.app.modules.es.extensions;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.RegisteredConnector;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class ESRecordNavigationExtension implements RecordNavigationExtension {

	private static final String DOWNLOAD_DOCUMENT = "ESRecordNavigationExtension-DownloadDocument";
	final String collection;
	final AppLayerFactory appLayerFactory;

	public ESRecordNavigationExtension(
			String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void navigateToEdit(NavigationParams navigationParams) {
	}

	@Override
	public void navigateToView(NavigationParams navigationParams) {
		RecordVO recordVO = navigationParams.getRecordVO();
		if (recordVO != null) {
			Page page = navigationParams.getPage();
			String schemaTypeCode = navigationParams.getSchemaTypeCode();

			ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
			ConnectorManager connectorManager = es.getConnectorManager();

			for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
				ConnectorUtilsServices<?> services = connector.getServices();
				for (String type : services.getConnectorDocumentTypes()) {
					if (schemaTypeCode.equals(type)) {
						String url = services.getRecordExternalUrl(recordVO);
						if (url != null) {
							page.open(url, null);
						}
						return;
					}
				}
			}

			throw new UnsupportedOperationException("No navigation for schema type code " + schemaTypeCode);
		}
	}

	@Override
	public boolean isViewForSchemaTypeCode(String documentSchemaTypeCode) {
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		ConnectorManager connectorManager = es.getConnectorManager();

		for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
			ConnectorUtilsServices<?> services = connector.getServices();
			for (String type : services.getConnectorDocumentTypes()) {
				if (documentSchemaTypeCode.equals(type)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void prepareLinkToView(final NavigationParams navigationParams) {
		if (isViewForSchemaTypeCode(navigationParams.getSchemaTypeCode())) {
			ClickListener clickListener = null;

			RecordVO recordVO = navigationParams.getRecordVO();
			String schemaTypeCode = navigationParams.getSchemaTypeCode();
			String schemaCode = recordVO.getSchema().getCode();
			final String url = recordVO.get(schemaCode + "_url");
			String title = recordVO.get(schemaCode + "_title");
			String id = recordVO.getId();
			String collection = recordVO.getSchema().getCollection();
			ReferenceDisplay component = (ReferenceDisplay) navigationParams.getComponent();

			if (ConnectorSmbDocument.SCHEMA_TYPE.equals(schemaTypeCode)) {
				final String filename = StringUtils
						.substringAfterLast(url, "/");
				clickListener = prepareFileDownloader(url, title, id, collection, component, filename);
			} else {
				ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
				ConnectorManager connectorManager = es.getConnectorManager();
				for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
					ConnectorUtilsServices<?> services = connector.getServices();
					for (String type : services.getConnectorDocumentTypes()) {
						if (schemaTypeCode.equals(type)) {
							String connectorUrl = services.getRecordExternalUrl(recordVO);
							if (connectorUrl != null) {
								clickListener = new ClickListener() {
									@Override
									public void buttonClick(ClickEvent event) {
										navigateToView(navigationParams);
									}
								};
								component.setCaption(title == null ? url : title);
								break;
							}
						}
					}
				}
			}
			component.addStyleName(SearchResultDisplay.TITLE_STYLE);
			component.setEnabled(true);
			if (clickListener == null) {
				//to fix null titles
				if (StringUtils.isBlank(title)) {
					title = url;
				}
				clickListener = prepareFileDownloader(url, title, id, collection, component, title);
			}
			component.addClickListener(clickListener);
		}
	}

	private ClickListener prepareFileDownloader(String url, String title, String id, String collection,
			ReferenceDisplay component, String filename) {
		ClickListener clickListener;
		FileDownloader downloader = new FileDownloader(getResourceStream(collection, id, filename));
		downloader.extend(component);
		clickListener = new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				//Important
			}
		};
		component.setCaption(title == null ? url : title);
		return clickListener;
	}

	private static Resource getResourceStream(final String collection, final String id, String fileName) {
		return new StreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				try {
					AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
					ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);

					ConnectorDocument<?> document = es.getConnectorDocument(id);
					ConnectorUtilsServices services = ConnectorServicesFactory
							.forConnectorDocument(appLayerFactory, document);
					InputStream inputStream = services.newContentInputStream(document, DOWNLOAD_DOCUMENT);
					return inputStream;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}, fileName);
	}
}
