package com.constellio.sdk.tests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.extensions.BigVaultServerExtension;

public class CommitCounter extends BigVaultServerExtension {

	private List<String> commits = new ArrayList<>();

	public CommitCounter(DataLayerFactory dataLayerFactory) {
		dataLayerFactory.getExtensions().getSystemWideExtensions().bigVaultServerExtension.add(this);
	}

	public synchronized List<String> newCommitsCall() {
		List<String> newCommitsCalls = new ArrayList<>(commits);
		commits.clear();
		return newCommitsCalls;
	}

	public synchronized void reset() {
		commits.clear();
	}

	@Override
	public void afterCommit(BigVaultServerTransaction transaction, long qtime) {

		String stackTrace = ExceptionUtils.getStackTrace(new Throwable());

		commits.add(stackTrace);
	}
}
