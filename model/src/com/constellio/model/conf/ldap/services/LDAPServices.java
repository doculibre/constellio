/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.conf.ldap.services;

import java.util.*;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.constellio.model.conf.ldap.Filter;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.services.users.sync.LDAPFastBind;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.model.conf.ldap.user.LDAPUserBuilder;

public class LDAPServices {

	/*public static List<LDAPUser> getAllUsers2(LDAPDirectoryType directoryType, LdapContext ctx , String contextName) {
		if (contextName == null || contextName.isEmpty()) {
			String baseCtx;
			try {
				baseCtx = getDefaultNamingContext(ctx);
				return browseUsersFromBaseContext(directoryType, ctx, baseCtx);
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
		} else {
			try {
				return searchUsersFromContext(directoryType, ctx, contextName);
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static List<LDAPUser> getAllUsers(LDAPDirectoryType directoryType, LdapContext ctx, List<String> baseContextList) {
		List<LDAPUser> returnList;
		if (baseContextList == null || baseContextList.isEmpty()) {
			returnList = getAllUsers2(directoryType, ctx, null);
		} else {
			returnList = new ArrayList<>();
			for (String baseContext : baseContextList) {
				Collection<? extends LDAPUser> currentfetchedUsers = getAllUsers2(directoryType, ctx, baseContext);
				returnList.addAll(currentfetchedUsers);
			}
		}
		return returnList;
	}
	//Util lorsqu'on n'a pas de context name
	public static List<LDAPUser> browseUsersFromBaseContext(LDAPDirectoryType directoryType, DirContext ctx, String baseContextName)
			throws NamingException {
		List<LDAPUser> users = new ArrayList<>();
		NamingEnumeration<?> contentsEnum = ctx.list(baseContextName);
		try {
			LDAPUserBuilder userBuilder = LDAPUserBuilderFactory.getUserBuilder(directoryType);
			while (contentsEnum.hasMore()) {
				NameClassPair ncp = (NameClassPair) contentsEnum.next();
				String contentName = ncp.getName();
				String subContentName = contentName + "," + baseContextName;
				Attributes attr1 = ctx.getAttributes(subContentName,
						new String[] { "objectcategory" });
				if (attr1.get("objectcategory").toString().indexOf("CN=Person") == -1) {
					// subContexts
					List<LDAPUser> subUsers = browseUsersFromBaseContext(directoryType, ctx, subContentName);
					users.addAll(subUsers);
				} else {
					try {
						Attributes attrs = ctx.getAttributes(subContentName, userBuilder.getFetchedAttributes());//
						LDAPUser user = userBuilder.buildUser(subContentName, attrs);
						users.add(user);
					} catch (NamingException ne) {
						ne.printStackTrace();
					}
				}
			}
		} catch (PartialResultException e) {
			System.out.println("Return to root :" + e.getMessage());
			return users;
		}
		return users;
	}

		private static List<LDAPUser> searchUsersFromContext(LDAPDirectoryType directoryType, DirContext ctx, String usersContainer)
			throws NamingException {
		List<LDAPUser> users = new ArrayList<>();
		SearchControls ctls = new SearchControls();
		LDAPUserBuilder userBuilder = LDAPUserBuilderFactory.getUserBuilder(directoryType);
		ctls.setReturningAttributes(userBuilder.getFetchedAttributes());
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

		NamingEnumeration<?> answer = ctx.search(usersContainer, "(objectclass=person)", ctls);
		while (answer.hasMore()) {
			SearchResult rslt = (SearchResult) answer.next();
			Attributes attrs = rslt.getAttributes();
			LDAPUser user = userBuilder.buildUser("", attrs);
			users.add(user);
		}
		return users;
	}

	private static String getFirstString(Attribute attribute)
			throws NamingException {
		if (attribute == null || attribute.size() == 0) {
			return "";
		}
		return (String) attribute.get(0);
	}

	public static List<LDAPUser> buildUsersOfgroup(LDAPDirectoryType directoryType, LDAPGroup group, LdapContext ctx) {
		List<LDAPUser> returnUsers = new ArrayList<>();
		for (String userId : group.getMembers()) {
			LDAPUser currentUser = getUser(directoryType, userId, ctx);
			returnUsers.add(currentUser);
		}
		return returnUsers;
	}

	public static String extractDomaine(LDAPUser ldapUser) {
		return StringUtils.substringAfter(ldapUser.getId(), ",");
	}

	public static LDAPUser getUserByUserName(LDAPDirectoryType directoryType, String userName,
			LdapContext ctx) {
		try {

			SearchControls sc = new SearchControls();
			sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String filter = "(name=" + userName + ")";
			String baseCtx = getDefaultNamingContext(ctx);

			NamingEnumeration<SearchResult> results = ctx.search(baseCtx,
					filter, sc);

			LDAPUserBuilder userBuilder = LDAPUserBuilderFactory.getUserBuilder(directoryType);
			if (results.hasMoreElements()) {
				SearchResult next = results.next();
				return userBuilder.buildUser(next.getName(), next.getAttributes());
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static String getDefaultNamingContext(DirContext dirCtxt)
			throws NamingException {
		Attributes attributes = dirCtxt.getAttributes(dirCtxt.getNameInNamespace());

		Attribute attribute = attributes.get("defaultNamingContext");
		NamingEnumeration<String> all = (NamingEnumeration<String>) attribute.getAll();
		if (all.hasMoreElements()) {
			return all.next();
		} else {
			throw new RuntimeException("No Default Naming Context!");
		}
	}
	*/

	public Set<LDAPGroup> getAllGroups(LdapContext ctx , List<String> baseContextList) {
		Set<LDAPGroup> returnList = new HashSet<>();
		if (baseContextList == null || baseContextList.isEmpty()) {
			return returnList;
		} else {
			returnList = new HashSet<>();
			for (String baseContext : baseContextList) {
				Collection<? extends LDAPGroup> currentfetchedGroups;
				try {
					currentfetchedGroups = searchGroupsFromContext(ctx, baseContext);
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}
				returnList.addAll(currentfetchedGroups);
			}
		}
		return returnList;
	}

	public Set<LDAPGroup> getGroupsUsingFilter(LdapContext ctx , List<String> baseContextList, final Filter filter) {
		Set<LDAPGroup> groups = getAllGroups(ctx, baseContextList);
		CollectionUtils.filter(groups, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				LDAPGroup ldapGroup = (LDAPGroup)object;
				return filter.isAccepted(ldapGroup.getSimpleName());
			}
		});
		return groups;
	}

	private List<LDAPGroup> searchGroupsFromContext(LdapContext ctx, String groupsContainer)
			throws NamingException {
		List<LDAPGroup> groups = new ArrayList<>();
		try {
			int pageSize = 100;
			byte[] cookie = null;
			ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
			do {
				//Query
				SearchControls searchCtls = new SearchControls();
				searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchCtls.setReturningAttributes(LDAPGroup.FETCHED_ATTRIBUTES);

				NamingEnumeration results = ctx.search(groupsContainer, "(objectclass=group)", searchCtls);

					/* for each entry print out name + all attrs and values */
				while (results != null && results.hasMore()) {
					SearchResult entry = (SearchResult) results.next();
					Attribute attribute = entry.getAttributes().get("cn");

					Attributes attrs = entry.getAttributes();
					LDAPGroup group = buildLDAPGroup(attrs);
					groups.add(group);
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
					System.out.println("No controls were sent from the server");
				}
				// Re-activate paged results
				ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });

			} while (cookie != null);
		} catch (Exception e) {
			System.err.println("PagedSearch failed.");
			e.printStackTrace();
		}
		return groups;
	}

	public List<String> searchUsersIdsFromContext(LDAPDirectoryType directoryType, LdapContext ctx, String usersContainer)
			throws NamingException {
		List<String> usersIds = new ArrayList<>();
		SearchControls ctls = new SearchControls();
		String userIdAttributeName = LDAPUserBuilderFactory.getUserBuilder(directoryType).getUserIdAttribute();
		ctls.setReturningAttributes(new String[] { userIdAttributeName });
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		/////////////////////////////
		try {
			int pageSize = 100;
			byte[] cookie = null;
			ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
				do {
					//Query
					SearchControls searchCtls = new SearchControls();
					searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
					String[] returnAttributes = { "cn" };
					searchCtls.setReturningAttributes(returnAttributes);

					NamingEnumeration results = ctx.search(usersContainer, "(objectclass=person)", searchCtls);

					/* for each entry print out name + all attrs and values */
					while (results != null && results.hasMore()) {
						SearchResult entry = (SearchResult) results.next();
						String currentUserId = entry.getNameInNamespace();
						if (StringUtils.isNotBlank(currentUserId)) {
							usersIds.add(currentUserId);
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
						System.out.println("No controls were sent from the server");
					}
					// Re-activate paged results
					ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, cookie, Control.CRITICAL) });

				} while (cookie != null);
		} catch (Exception e) {
			System.err.println("PagedSearch failed.");
			e.printStackTrace();
		}
		Collections.sort(usersIds);
		return usersIds;
		////////////////////////////////////

		/*NamingEnumeration<?> answer = ctx.search(usersContainer, "(objectclass=person)", ctls);
		while (answer.hasMore()) {
			SearchResult rslt = (SearchResult) answer.next();
			//            Attributes attrs = rslt.getAttributes();
			//            Attribute currentUserIdAttribute = attrs.get(userIdAttributeName);
			//            String currentUserId = getFirstString(currentUserIdAttribute);
			String currentUserId = rslt.getNameInNamespace();
			if (StringUtils.isNotBlank(currentUserId)) {
				usersIds.add(currentUserId);
			}
		}
		return usersIds;*/
	}

	private LDAPGroup buildLDAPGroup(Attributes attrs)
			throws NamingException {
		Attribute groupNameAttribute = attrs.get(LDAPGroup.COMMON_NAME);
		Attribute groupDNameAttribute = attrs.get(LDAPGroup.DISTINGUISHED_NAME);

		if (groupNameAttribute != null && groupNameAttribute.size() > 0) {
			String groupName = (String) groupNameAttribute.get(0);
			String distinguishedName = (String) groupDNameAttribute.get(0);
			LDAPGroup returnGroup = new LDAPGroup(groupName, distinguishedName);
			//String groupName = (String) groupNameAttribute.get(0);
			//TODO parent
			Attribute members = attrs.get(LDAPGroup.MEMBER);
			if (members != null) {
				for (int i = 0; i < members.size(); i++) {
					String userId = (String) members.get(i);
					returnGroup.addUser(userId);
				}
			}
			return returnGroup;
		}
		return null;
	}

	public LdapContext connectToLDAP(List<String> domains, String url, String user, String password) {
		LDAPFastBind ldapFastBind = new LDAPFastBind(url);
		boolean authenticated = false;
		for (String domain : domains) {
			String username = user + "@" + domain;
			authenticated = ldapFastBind.authenticate(username,
					password);
			if (authenticated) {
				break;
			}
		}
		if(authenticated){
			return ldapFastBind.ctx;
		}
		authenticated = ldapFastBind.authenticate(user, password);
		if (!authenticated){
			throw new LDAPConnectionFailure(domains.toArray(), url, user);
		}
		return ldapFastBind.ctx;
	}

	public LdapContext connectToLDAP(List<String> domains, List<String> urls, String user, String password) {
		for (String url :urls){
			LdapContext ctx;
			try{
				ctx = connectToLDAP(domains, url, user, password);
				if (ctx != null){
					return ctx;
				}
			}catch (RuntimeException e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public LDAPUser getUser(LDAPDirectoryType directoryType, String userId, LdapContext ctx) {
		Attributes attrs;
		try {
			LDAPUserBuilder userBuilder = LDAPUserBuilderFactory.getUserBuilder(directoryType);
			String[] fetchedAttributes = userBuilder.getFetchedAttributes();
			attrs = ctx.getAttributes(userId, fetchedAttributes);
			LDAPUser user = userBuilder.buildUser(userId, attrs);
			return user;
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public String extractUsername(String userId) {
		return StringUtils.substringBetween(userId, "=", ",");
	}

	public boolean isUser(LDAPDirectoryType directoryType, String groupMemberId, LdapContext ctx) {
		try {
			LDAPUserBuilder userBuilder = LDAPUserBuilderFactory.getUserBuilder(directoryType);
			String searchFilter ="(&(objectClass=user)(" + userBuilder.getUserIdAttribute() + "=" + groupMemberId + "))";

			SearchControls searchControls = new SearchControls();
			searchControls.setReturningAttributes(new String[]{});

			// specify the search scope
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> found = ctx.search(groupMemberId, searchFilter, searchControls);
			return found != null && found.hasMoreElements();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getUsersUsingFilter(LDAPDirectoryType directoryType, LdapContext ctx, List<String> usersWithoutGroupsBaseContextList, final Filter filter) {
		Set<String> users = new HashSet<>();
		for(String currentContex : usersWithoutGroupsBaseContextList){
			try {
				users.addAll(searchUsersIdsFromContext(directoryType, ctx, currentContex));
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
		CollectionUtils.filter(users, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				String user = (String)object;
				return filter.isAccepted(user);
			}
		});
		return users;
	}
}
