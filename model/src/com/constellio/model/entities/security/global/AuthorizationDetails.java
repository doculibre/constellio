package com.constellio.model.entities.security.global;

import java.io.Serializable;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * Created by Constellio on 2016-12-21.
 */
public interface AuthorizationDetails extends Serializable {

	public List<String> getRoles();

	public LocalDate getStartDate();

	public LocalDate getEndDate();

	public String getId();

	public String getCollection();

	public boolean isSynced();

	AuthorizationDetails withNewEndDate(LocalDate endate);

	boolean isFutureAuthorization();

	boolean isActiveAuthorization();

	String getTarget();
}
