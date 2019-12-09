package com.constellio.data.dao.services.replicationFactor;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.bigVault.solr.listeners.BigVaultServerAddEditListener;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class TransactionLogReplicationFactorManager implements StatefulService, BigVaultServerAddEditListener {

	protected DataLayerSystemExtensions extensions;

	private DataLayerFactory dataLayerFactory;
	private ReplicationFactorTransactionWriteService replicationFactorTransactionWriteService;
	private ReplicationFactorTransactionReadService replicationFactorTransactionReadService;

	public TransactionLogReplicationFactorManager(DataLayerFactory dataLayerFactory,
												  DataLayerSystemExtensions extensions) {
		this.dataLayerFactory = dataLayerFactory;
		this.extensions = extensions;
	}

	@Override
	public void afterAdd(BigVaultServerTransaction transaction, TransactionResponseDTO responseDTO) {
		if (responseDTO != null && responseDTO.getRf() != -1 && responseDTO.getRf() < getMinimalReplicationFactor()) {
			replicationFactorTransactionWriteService.add(transaction, responseDTO);
		}
	}

	@Override
	public void beforeAdd(BigVaultServerTransaction transaction) {
		// nothing to do
	}

	@Override
	public String getListenerUniqueId() {
		return "replicationFactorListener";
	}

	@Override
	public void initialize() {
		ReplicationFactorLogService replicationFactorLogService =
				new ReplicationFactorLogService(dataLayerFactory.getContentsDao());

		replicationFactorTransactionWriteService =
				new ReplicationFactorTransactionWriteService(replicationFactorLogService);

		dataLayerFactory.getRecordsVaultServer().registerListener(this);

		replicationFactorTransactionReadService =
				new ReplicationFactorTransactionReadService(dataLayerFactory, replicationFactorLogService, extensions);
		replicationFactorTransactionReadService.start();
	}

	@Override
	public void close() {
		if (replicationFactorTransactionWriteService != null) {
			replicationFactorTransactionWriteService.stop();
		}

		if (replicationFactorTransactionReadService != null) {
			replicationFactorTransactionReadService.stop();
		}
	}

	private int getMinimalReplicationFactor() {
		return dataLayerFactory.getDataLayerConfiguration().getSolrMinimalReplicationFactor();
	}
}
