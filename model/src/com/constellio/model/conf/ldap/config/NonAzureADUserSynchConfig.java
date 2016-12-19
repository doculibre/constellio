package com.constellio.model.conf.ldap.config;

import java.util.Collections;
import java.util.List;

public class NonAzureADUserSynchConfig {
	String user, password;
	List<String> groupBaseContextList;
	List<String> usersWithoutGroupsBaseContextList;
	List<String> userFilterGroupsList = Collections.emptyList();
}
