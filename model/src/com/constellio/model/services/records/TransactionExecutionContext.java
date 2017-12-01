package com.constellio.model.services.records;

import java.util.List;

import com.constellio.model.entities.calculators.dependencies.AllPrincipalsAuthsDependencyValue;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;

public class TransactionExecutionContext {

	List<SolrAuthorizationDetails> allAuthorizationDetails;

	AllPrincipalsAuthsDependencyValue allPrincipalsAuthsDependencyValue;

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
}
