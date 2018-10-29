package com.constellio.model.entities.security.global;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.List;

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

	boolean isNegative();

	AuthorizationDetails withNewEndDate(LocalDate endate);

	boolean isFutureAuthorization();

	boolean isActiveAuthorization();

	boolean isOverrideInherited();

	String getTarget();

	String getTargetSchemaType();
}
