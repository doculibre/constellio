package com.constellio.data.dao.services.bigVault.solr.listeners;

import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

public interface BigVaultServerAddEditListener extends BigVaultServerListener{
	void afterAdd(BigVaultServerTransaction transaction, TransactionResponseDTO responseDTO);
	void beforeAdd(BigVaultServerTransaction transaction);
}
