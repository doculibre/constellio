package com.constellio.app.modules.es.connectors.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.services.LDAPServices;

public class ConnectorLDAPServicesImpl implements ConnectorLDAPServices {
	private static final Logger LOGGER = LogManager.getLogger(ConnectorLDAPServicesImpl.class);

	@Override
	public ConnectorLDAPSearchResult getAllObjectsUsingFilter(LdapContext ctx, String objectClass, String objectCategory,
			Set<String> searchContextsNames, RegexFilter filter) {
		Set<String> usersIds = new HashSet<>();
		String userIdAttributeName = "cn";
		String searchFilter = computeSearchFilter(objectCategory, objectClass);
		boolean errorDuringSearch = false;
		/////////////////////////////
		try {
			for (String currentContext : searchContextsNames) {
				ConnectorLDAPSearchResult currentResult = getAllObjectsUsingFilter(ctx, filter, searchFilter, userIdAttributeName,
						currentContext);
				if(currentResult.isErrorDuringSearch()){
					errorDuringSearch = true;
				}
				usersIds.addAll(currentResult.getDocumentIds());
			}
		} catch (Exception e) {
			errorDuringSearch = true;
			LOGGER.warn("PagedSearch failed.", e);
		}

		return new ConnectorLDAPSearchResult().setDocumentIds(usersIds).setErrorDuringSearch(errorDuringSearch);
	}

	ConnectorLDAPSearchResult getAllObjectsUsingFilter(LdapContext ctx, RegexFilter filter, String searchFilter,
			String userIdAttributeName,
			String contextName) {
		Set<String> objectsIds = new HashSet<>();
		/////////////////////////////
		Boolean errorDuringSearch = false;
		try {
			int pageSize = 100;
			byte[] cookie = null;
			ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
			do {
				//Query
				SearchControls searchCtls = new SearchControls();
				searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String[] returnAttributes = { userIdAttributeName };
				searchCtls.setReturningAttributes(returnAttributes);

				NamingEnumeration results = ctx.search(contextName, searchFilter, searchCtls);

					/* for each entry print out name + all attrs and values */
				while (results != null && results.hasMore()) {
					SearchResult entry = (SearchResult) results.next();
					String currentUserId = entry.getNameInNamespace();
					if (StringUtils.isNotBlank(currentUserId)) {
						if (filter != null) {
							if (filter.isAccepted(getSimpleName(currentUserId.toLowerCase()))) {
								objectsIds.add(currentUserId);
							}
						} else {
							objectsIds.add(currentUserId);
						}
					}
				}

				// Examine the paged results control response
				Control[] controls = ctx.getResponseControls();
				if (controls != null) {
					for (int i = 0; i < controls.length; i++) {
						if (controls[i] instanceof PagedResultsResponseControl) {
							PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
							cookie = prrc.getCookie();
						}
					}
				} else {
					LOGGER.warn("No controls were sent from the server");
				}
				// Re-activate paged results
				ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });

			} while (cookie != null);
		} catch (Exception e) {
			errorDuringSearch = true;
			LOGGER.error("PagedSearch failed.", e);
		}
		return new ConnectorLDAPSearchResult().setDocumentIds(objectsIds).setErrorDuringSearch(errorDuringSearch);
	}

	static String getSimpleName(String userId) {
		String userNameEndingWithCommaAndOtherString = StringUtils.substringBetween(userId, "=", "=");
		return StringUtils.substringBeforeLast(userNameEndingWithCommaAndOtherString, ",");
	}

	private Set<String> getMainContextes(LdapContext ctx)
			throws NamingException {
		Set<String> returnContextes = new HashSet<>();
		Attributes attributes = ctx.getAttributes("", new String[] { "namingContexts" });
		Attribute attribute = attributes.get("namingContexts");
		NamingEnumeration<?> all = attribute.getAll();
		while (all.hasMore()) {
			String next = (String) all.next();
			String nextWithoutCaps = next.toLowerCase();
			if (nextWithoutCaps.startsWith("ou") || nextWithoutCaps.startsWith("dc")) {
				returnContextes.add(next);
			}
		}
		return returnContextes;
	}

	static String computeSearchFilter(String objectCategory, String objectClass) {
		if (StringUtils.isNotBlank(objectClass)) {
			if (StringUtils.isNotBlank(objectCategory)) {
				return "(&(objectCategory=" + objectCategory + ")(objectClass=" + objectClass + "))";
			} else {
				return "(objectClass=" + objectClass + ")";
			}
		} else {
			if (StringUtils.isNotBlank(objectCategory)) {
				return "(objectCategory=" + objectCategory + ")";
			}
		}
		LOGGER.error("Should specify at least one of objectClass, objectCategory; given values were : " + objectClass
				+ objectCategory);
		throw new InvalidSearchFilterRuntimeException(objectClass, objectCategory);
	}

	@Override
	public Map<String, LDAPObjectAttributes> getObjectsAttributes(LdapContext ctx, Set<String> objectsIds) {
		Map<String, LDAPObjectAttributes> returnMap = new HashMap<>();
		for (String objectId : objectsIds) {
			returnMap.put(objectId, getObjectAttributes(ctx, objectId));
		}
		return returnMap;
	}

	@Override
	public LDAPObjectAttributes getObjectAttributes(LdapContext ctx, String objectsId) {
		Attributes attrs;
		try {
			attrs = ctx.getAttributes(objectsId, null);
			return new LDAPObjectAttributes(attrs);
		} catch (NamingException e) {
			//TODO 
			throw new RuntimeException(e);
		}
	}

	@Override
	public LdapContext connectToLDAP(String url, String user, String password, Boolean followReferences,
			boolean activeDirectory) {
		return new LDAPServices().connectToLDAP(new ArrayList<String>(), url, user, password, followReferences, activeDirectory);
	}

	@Override
	public boolean isObjectEnabled(LDAPObjectAttributes object, DirectoryType directoryType) {
		return false;
	}

	public static class InvalidSearchFilterRuntimeException extends RuntimeException {
		public InvalidSearchFilterRuntimeException(
				String objectClass, String objectCategory) {
			super("Should specify at least one of objectClass, objectCategory; given values were : " + objectClass
					+ objectCategory);
		}
	}
}
