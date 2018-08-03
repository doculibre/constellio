package com.constellio.data.dao.services.bigVault.solr;

import org.apache.solr.client.solrj.request.UpdateRequest;

import java.io.IOException;
import java.io.Writer;

public class BigVaultUpdateRequest extends UpdateRequest {

	public BigVaultUpdateRequest() {

	}

	@Override
	public UpdateRequest writeXML(Writer writer)
			throws IOException {
		// Solr requires the <update> tag when documents are added and deleted in the same request
		writer.write("<update>");
		super.writeXML(writer);
		writer.write("</update>");
		return this;
	}

}
