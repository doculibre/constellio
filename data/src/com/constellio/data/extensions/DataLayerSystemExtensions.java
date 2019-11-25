package com.constellio.data.extensions;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.events.Event;
import com.constellio.data.events.EventBusManagerExtension;
import com.constellio.data.events.ReceivedEventParams;
import com.constellio.data.events.SentEventParams;
import com.constellio.data.extensions.contentDao.ContentDaoExtension;
import com.constellio.data.extensions.contentDao.ContentDaoInputStreamOpenedParams;
import com.constellio.data.extensions.contentDao.ContentDaoUploadParams;
import com.constellio.data.extensions.extensions.configManager.AddUpdateConfigParams;
import com.constellio.data.extensions.extensions.configManager.ConfigManagerExtension;
import com.constellio.data.extensions.extensions.configManager.DeleteConfigParams;
import com.constellio.data.extensions.extensions.configManager.ExtensionConverter;
import com.constellio.data.extensions.extensions.configManager.ReadConfigParams;
import com.constellio.data.extensions.extensions.configManager.SupportedExtensionExtension;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.data.frameworks.extensions.ExtensionUtils.getBooleanValue;

public class DataLayerSystemExtensions {

	private static final Logger VAULT_LOGGER = LoggerFactory.getLogger(DataLayerSystemExtensions.class);

	//------------ Extension points -----------
	public VaultBehaviorsList<BigVaultServerExtension> bigVaultServerExtension = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TransactionLogExtension> transactionLogExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<ConfigManagerExtension> configManagerExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<ContentDaoExtension> contentDaoExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<SupportedExtensionExtension> supportedExtensionExtensions = new VaultBehaviorsList<>();
	public VaultBehaviorsList<EventBusManagerExtension> eventBusManagerExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<BigVaultServerExtension> getBigVaultServerExtension() {
		return bigVaultServerExtension;
	}

	//----------------- Callers ---------------

	public void onVaultInputStreamOpened(ContentDaoInputStreamOpenedParams params) {
		contentDaoExtensions.forEach(e -> e.onInputStreamOpened(params));
	}

	public void onVaultUpload(ContentDaoUploadParams params) {
		contentDaoExtensions.forEach(e -> e.onUpload(params));
	}


	public void afterRealtimeGetById(final long qtime, final String id, boolean found) {
		AfterGetByIdParams params = new AfterGetByIdParams() {
			@Override
			public String getId() {
				return id;
			}

			@Override
			public long getQtime() {
				return qtime;
			}

			@Override
			public boolean found() {
				return found;
			}
		};
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterRealtimeGetById(params);
		}
	}

	public void afterQuery(final SolrParams params, final String name, final long qtime, final int resultsSize,
						   Map<String, Object> debugMap) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			try {
				extension.afterQuery(params, qtime);
				final boolean getById = name != null && name.startsWith("getById:");
				extension.afterQuery(new AfterQueryParams() {
					@Override
					public SolrParams getSolrParams() {
						return params;
					}

					@Override
					public long getQtime() {
						return qtime;
					}

					@Override
					public int getReturnedResultsCount() {
						return resultsSize;
					}

					@Override
					public String getQueryName() {
						return name;
					}

					@Override
					public boolean isGetByIdQuery() {
						return getById;
					}

					@Override
					public Map<String, Object> getDebugMap() {
						return debugMap;
					}
				});
				if (getById) {
					String id = name.substring(name.indexOf(":") + 1);
					extension.afterGetById(new AfterGetByIdParams() {

						@Override
						public long getQtime() {
							return qtime;
						}

						@Override
						public boolean found() {
							return resultsSize > 0;
						}

						@Override
						public String getId() {
							return id;
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterUpdate(transaction, qtime);
		}
	}

	public void afterCommmit(BigVaultServerTransaction transaction, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterCommit(transaction, qtime);
		}
	}

	public boolean isDocumentFieldLoggedInTransactionLog(final String field, final String schema,
														 final String collection,
														 boolean defaultValue) {
		return getBooleanValue(getTransactionLogExtensions(), defaultValue, new BooleanCaller<TransactionLogExtension>() {
			@Override
			public ExtensionBooleanResult call(TransactionLogExtension extension) {
				return extension.isDocumentFieldLoggedInTransactionLog(field, schema, collection);
			}
		});
	}

	public VaultBehaviorsList<TransactionLogExtension> getTransactionLogExtensions() {
		return transactionLogExtensions;
	}

	public void onReadConfig(final String configPath) {
		for (ConfigManagerExtension extension : configManagerExtensions) {
			extension.readConfigs(new ReadConfigParams() {
				@Override
				public String getConfigPath() {
					return configPath;
				}
			});
		}
	}

	public String[] getSupportedExtensionExtensions() {
		List<String> allExtension = new ArrayList<>();
		for (SupportedExtensionExtension extensionExtension : supportedExtensionExtensions.getExtensions()) {
			allExtension.addAll(extensionExtension.getAdditionalSupportedExtension());
		}
		return allExtension.toArray(new String[0]);
	}

	public ExtensionConverter getConverterForSupportedExtension(String extension) {
		for (SupportedExtensionExtension extensionExtension : supportedExtensionExtensions.getExtensions()) {
			if (extensionExtension.getAdditionalSupportedExtension().contains(extension)) {
				return extensionExtension.getConverter();
			}
		}
		return null;
	}

	public void onAddUpdateConfig(final String configPath) {
		for (ConfigManagerExtension extension : configManagerExtensions) {
			extension.addUpdateConfig(new AddUpdateConfigParams() {
				@Override
				public String getConfigPath() {
					return configPath;
				}
			});
		}
	}

	public void onDeleteConfig(final String configPath) {
		for (ConfigManagerExtension extension : configManagerExtensions) {
			extension.deleteConfig(new DeleteConfigParams() {
				@Override
				public String getConfigPath() {
					return configPath;
				}
			});
		}
	}

	public void onEventSent(final Event event) {
		for (EventBusManagerExtension extension : eventBusManagerExtensions) {
			extension.onEventSent(new SentEventParams(event));
		}
	}

	public void onEventReceived(final Event event, boolean remoteEvent) {
		for (EventBusManagerExtension extension : eventBusManagerExtensions) {
			extension.onEventReceived(new ReceivedEventParams(event, remoteEvent));
		}
	}

}
