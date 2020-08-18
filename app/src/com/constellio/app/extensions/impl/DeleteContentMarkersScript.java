package com.constellio.app.extensions.impl;

import com.constellio.app.extensions.api.scripts.ScriptWithLogOutput;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;

public class DeleteContentMarkersScript extends ScriptWithLogOutput {


	private AppLayerFactory appLayerFactory;

	public DeleteContentMarkersScript(AppLayerFactory appLayerFactory) {

		super(appLayerFactory, "Voûte", "Supprimer les marqueurs de fichiers à vérifier pour suppression", true);

		this.appLayerFactory = appLayerFactory;
	}

	@Override
	protected void execute() {
		BigVaultServer bigVaultServer = modelLayerFactory.getDataLayerFactory().getRecordsVaultServer();
		try {
			bigVaultServer.addAll(new BigVaultServerTransaction(RecordsFlushing.NOW())
					.addDeletedQuery("type_s:marker AND (*:* -schema_s:*)"));
		} catch (BigVaultException e) {
			throw new RuntimeException(e);
		}
	}

}
