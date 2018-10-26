package com.constellio.model.entities.security;

import com.constellio.model.entities.security.global.AuthorizationDetails;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class Authorization {

	AuthorizationDetails detail;

	List<String> grantedToPrincipals = new ArrayList<>();

	public Authorization() {
	}

	public Authorization(AuthorizationDetails detail, List<String> grantedToPrincipals) {
		this.detail = detail;
		this.grantedToPrincipals = grantedToPrincipals;
	}

	public AuthorizationDetails getDetail() {
		return detail;
	}

	public void setDetail(XMLAuthorizationDetails detail) {
		this.detail = detail;
	}

	public List<String> getGrantedToPrincipals() {
		return grantedToPrincipals;
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
		return "Authorization{ " + detail + " granted to" + grantedToPrincipals + " on " + detail.getTarget() + "}";
	}
}
