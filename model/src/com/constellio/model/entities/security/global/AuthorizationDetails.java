package com.constellio.model.entities.security.global;

import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by Constellio on 2016-12-21.
 */
public interface AuthorizationDetails {

	public List<String> getRoles();

	public LocalDate getStartDate();

	public LocalDate getEndDate();

	public String getId();

	public String getCollection();

	public boolean isSynced();

	AuthorizationDetails withNewEndDate(LocalDate endate);

	boolean isFutureAuthorization();
}
