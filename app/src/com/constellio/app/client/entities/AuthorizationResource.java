package com.constellio.app.client.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuthorizationResource {

	private List<String> principalIds;

	private List<String> recordIds;

	private List<String> roleIds = new ArrayList<>();

	private Date startDate;

	private Date endDate;

	private String collection;

	public List<String> getPrincipalIds() {
		return principalIds;
	}

	public void setPrincipalIds(List<String> principalIds) {
		this.principalIds = principalIds;
	}

	public List<String> getRecordIds() {
		return recordIds;
	}

	public void setRecordIds(List<String> recordIds) {
		this.recordIds = recordIds;
	}

	public List<String> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<String> roleIds) {
		this.roleIds = roleIds;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}
}
