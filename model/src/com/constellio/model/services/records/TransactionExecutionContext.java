package com.constellio.model.services.records;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.calculators.dependencies.AllPrincipalsAuthsDependencyValue;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.security.TransactionSecurityModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionExecutionContext {

	List<SolrAuthorizationDetails> allAuthorizationDetails;

	AllPrincipalsAuthsDependencyValue allPrincipalsAuthsDependencyValue;

	Map<String, KeyListMap<String, Record>> metadatasInvertedAggregatedValuesMap = new HashMap<>();

	Transaction transaction;

	TransactionSecurityModel transactionSecurityModel;

	public TransactionExecutionContext(Transaction transaction) {
		this.transaction = transaction;
	}

	public AllPrincipalsAuthsDependencyValue getAllPrincipalsAuthsDependencyValue() {
		return allPrincipalsAuthsDependencyValue;
	}

	public TransactionExecutionContext setAllPrincipalsAuthsDependencyValue(
			AllPrincipalsAuthsDependencyValue allPrincipalsAuthsDependencyValue) {
		this.allPrincipalsAuthsDependencyValue = allPrincipalsAuthsDependencyValue;
		return this;
	}

	public List<SolrAuthorizationDetails> getAllAuthorizationDetails() {
		return allAuthorizationDetails;
	}

	public TransactionExecutionContext setAllAuthorizationDetails(
			List<SolrAuthorizationDetails> allAuthorizationDetails) {
		this.allAuthorizationDetails = allAuthorizationDetails;
		return this;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public TransactionSecurityModel getTransactionSecurityModel() {
		return transactionSecurityModel;
	}

	public TransactionExecutionContext setTransactionSecurityModel(
			TransactionSecurityModel transactionSecurityModel) {
		this.transactionSecurityModel = transactionSecurityModel;
		return this;
	}
}
