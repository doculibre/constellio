package com.constellio.model.entities.security;

import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class Authorization {

	SolrAuthorizationDetails detail;

	public Authorization() {
	}

	public Authorization(SolrAuthorizationDetails detail) {
		this.detail = detail;
	}

	public AuthorizationDetails getDetail() {
		return detail;
	}


	public List<String> getGrantedToPrincipals() {
		return detail.getPrincipals();
	}

	public String getGrantedOnRecord() {
		return detail.getTarget();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "Authorization{ " + detail + " granted to" + detail.getPrincipals() + " on " + detail.getTarget() + "}";
	}
}
