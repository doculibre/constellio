package com.constellio.app.modules.es.extensions;

import com.constellio.app.extensions.records.RecordNavigationExtension;
import com.constellio.app.extensions.records.params.NavigationParams;
import com.constellio.app.modules.es.connectors.ConnectorServicesFactory;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.RegisteredConnector;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.SearchResultDisplay;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Locale;

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
		boolean openWithAgent;
		Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(navigationParams.getRecordVO().getId());
		if (ConstellioAgentUtils.isAgentSupported()) {
			String typeCode = record.getTypeCode();
			if (ConnectorSmbDocument.SCHEMA_TYPE.equals(typeCode)) {
				openWithAgent = true;
			} else {
				openWithAgent = false;
			}
		} else {
			openWithAgent = false;
		}

		if (openWithAgent) {
			try {
				openWithAgent(navigationParams);
			} catch (Exception e) {
				openWithoutAgent(navigationParams);
			}
		} else {
			openWithoutAgent(navigationParams);
		}
	}

	private void openWithoutAgent(NavigationParams navigationParams) {
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

	private void openWithAgent(NavigationParams navigationParams) {
		Record record = appLayerFactory.getModelLayerFactory().newRecordServices().getDocumentById(navigationParams.getRecordVO().getId());
		Page page = navigationParams.getPage();
		if (ConstellioAgentUtils.isAgentSupported()) {
			String smbMetadataCode;
			String typeCode = record.getTypeCode();
			if (ConnectorSmbDocument.SCHEMA_TYPE.equals(typeCode)) {
				smbMetadataCode = ConnectorSmbDocument.URL;
			} else {
				smbMetadataCode = null;
			}
			if (smbMetadataCode != null) {
				SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
				RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
				if (rmConfigs.isAgentEnabled()) {
					RecordVO recordVO = navigationParams.getRecordVO();
					MetadataVO smbPathMetadata = recordVO.getMetadata(typeCode + "_default_" + smbMetadataCode);
					String agentSmbPath = ConstellioAgentUtils.getAgentSmbURL(recordVO, smbPathMetadata);
					page.open(agentSmbPath, null);
				} else {
					Metadata smbUrlMetadata = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
							.getMetadata(typeCode + "_default_" + smbMetadataCode);
					String smbPath = record.get(smbUrlMetadata);
					String path = smbPath;
					if (StringUtils.startsWith(path, "smb://")) {
						path = "file://" + StringUtils.removeStart(path, "smb://");
					}
					page.open(path, null);
				}
			}
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
	public boolean prepareLinkToView(final NavigationParams navigationParams, boolean isRecordInTrash,
									 Locale currentLocale) {
		boolean activeLink;
		if (isViewForSchemaTypeCode(navigationParams.getSchemaTypeCode())) {
			ClickListener clickListener = null;

			RecordVO recordVO = navigationParams.getRecordVO();
			String schemaTypeCode = navigationParams.getSchemaTypeCode();
			String schemaCode = recordVO.getSchema().getCode();
			final String url = recordVO.get(schemaCode + "_url");
			String title = recordVO.get(schemaCode + "_title");
			String id = recordVO.getId();
			String collection = recordVO.getSchema().getCollection();
			Component component = navigationParams.getComponent();
			if (component instanceof ReferenceDisplay) {
				ReferenceDisplay referenceDisplay = (ReferenceDisplay) component;

				//if (ConnectorSmbDocument.SCHEMA_TYPE.equals(schemaTypeCode)) {
				//	final String filename = StringUtils
				//			.substringAfterLast(url, "/");
				//	clickListener = prepareFileDownloader(url, title, id, collection, referenceDisplay, filename);
				//} else {
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
								referenceDisplay.setCaption(title == null ? url : title);
								break;
							}
						}
					}
				}
				//}
				referenceDisplay.addStyleName(SearchResultDisplay.TITLE_STYLE);
				referenceDisplay.setEnabled(true);
				if (clickListener == null) {
					//to fix null titles
					if (StringUtils.isBlank(title)) {
						title = url;
					}
					clickListener = prepareFileDownloader(url, title, id, collection, referenceDisplay, title);
				}
				referenceDisplay.addClickListener(clickListener);
				activeLink = true;
			} else if (component instanceof Table) {
				// TODO Implement for table 
				activeLink = false;
			} else {
				activeLink = false;
			}
		} else {
			activeLink = false;
		}
		return activeLink;
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

	@Override
	public String getViewHrefTag(NavigationParams navigationParams) {
		return null;
	}

}
