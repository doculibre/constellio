package com.constellio.app.modules.es.connectors.ldap;

import static java.util.Arrays.asList;

import java.sql.Time;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.LdapContext;

import org.joda.time.LocalDate;

import com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.conf.ldap.RegexFilter;

public class TestLDAPServices implements ConnectorLDAPServices {
	Set<String> firstFetchIds = new HashSet<>(asList("id1", "id2", "id3"));
	Set<String> subsequentFetchIds = new HashSet<>(asList("id1", "id2", "id4"));
	final LocalDate now;
	private boolean throwException;
	private boolean errorDuringFirstSearch = false;

	public TestLDAPServices(LocalDate now) {
		this.now = now;
	}

	@Override
	public ConnectorLDAPSearchResult getAllObjectsUsingFilter(LdapContext ctx, String objectClass, String objectCategory,
			Set<String> contexts,
			RegexFilter filter) {
		if (throwException) {
			throw new RuntimeException("test exception");
		}
		if (TimeProvider.getLocalDate().equals(now)) {
			return new ConnectorLDAPSearchResult().setDocumentIds(firstFetchIds).setErrorDuringSearch(errorDuringFirstSearch);
		} else {
			return new ConnectorLDAPSearchResult().setDocumentIds(subsequentFetchIds).setErrorDuringSearch(
					errorDuringFirstSearch);
		}
	}

	@Override
	public Map<String, LDAPObjectAttributes> getObjectsAttributes(LdapContext ctx, Set<String> objectsIds) {
		if (throwException) {
			throw new RuntimeException("test exception");
		}
		Map<String, LDAPObjectAttributes> returnMap = new HashMap<>();
		for (String objectId : objectsIds) {
			returnMap.put(objectId, getObjectAttributes(ctx, objectId));
		}
		return returnMap;
	}

	@Override
	public LDAPObjectAttributes getObjectAttributes(LdapContext ctx, String id) {
		if (throwException) {
			throw new RuntimeException("test exception");
		}
		LDAPObjectAttributes returnAttributes = new LDAPObjectAttributes();
		String suffix;
		if (TimeProvider.getLocalDate().equals(now)) {
			suffix = "";
		} else {
			suffix = "_";
		}
		returnAttributes.addAttribute("title", new LDAPObjectAttribute().setValue("title" + id + suffix));
		returnAttributes.addAttribute("mail", new LDAPObjectAttribute().setValue("mail" + id + suffix));
		returnAttributes.addAttribute("distinguishedName", new LDAPObjectAttribute().setValue(id));
		return returnAttributes;
	}

	@Override
	public LdapContext connectToLDAP(String url, String user, String password, Boolean followReferences,
			boolean activeDirectory) {
		if (throwException) {
			throw new RuntimeException("test exception");
		}
		return new FakeLdapContext();
	}

	@Override
	public boolean isObjectEnabled(LDAPObjectAttributes object, DirectoryType directoryType) {
		return true;
	}

	public void setThrowExceptionWhenCommunicatingWithLdap(boolean throwException) {
		this.throwException = throwException;
	}

	public void setErrorWhenFetchingRemoteIds(boolean errorDuringFirstSearch) {
		this.errorDuringFirstSearch = errorDuringFirstSearch;
	}

	private class FakeLdapContext implements LdapContext {
		@Override
		public ExtendedResponse extendedOperation(ExtendedRequest request)
				throws NamingException {
			return null;
		}

		@Override
		public LdapContext newInstance(Control[] requestControls)
				throws NamingException {
			return null;
		}

		@Override
		public void reconnect(Control[] connCtls)
				throws NamingException {

		}

		@Override
		public Control[] getConnectControls()
				throws NamingException {
			return new Control[0];
		}

		@Override
		public void setRequestControls(Control[] requestControls)
				throws NamingException {

		}

		@Override
		public Control[] getRequestControls()
				throws NamingException {
			return new Control[0];
		}

		@Override
		public Control[] getResponseControls()
				throws NamingException {
			return new Control[0];
		}

		@Override
		public Attributes getAttributes(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public Attributes getAttributes(String name)
				throws NamingException {
			return null;
		}

		@Override
		public Attributes getAttributes(Name name, String[] attrIds)
				throws NamingException {
			return null;
		}

		@Override
		public Attributes getAttributes(String name, String[] attrIds)
				throws NamingException {
			return null;
		}

		@Override
		public void modifyAttributes(Name name, int mod_op, Attributes attrs)
				throws NamingException {

		}

		@Override
		public void modifyAttributes(String name, int mod_op, Attributes attrs)
				throws NamingException {

		}

		@Override
		public void modifyAttributes(Name name, ModificationItem[] mods)
				throws NamingException {

		}

		@Override
		public void modifyAttributes(String name, ModificationItem[] mods)
				throws NamingException {

		}

		@Override
		public void bind(Name name, Object obj, Attributes attrs)
				throws NamingException {

		}

		@Override
		public void bind(String name, Object obj, Attributes attrs)
				throws NamingException {

		}

		@Override
		public void rebind(Name name, Object obj, Attributes attrs)
				throws NamingException {

		}

		@Override
		public void rebind(String name, Object obj, Attributes attrs)
				throws NamingException {

		}

		@Override
		public DirContext createSubcontext(Name name, Attributes attrs)
				throws NamingException {
			return null;
		}

		@Override
		public DirContext createSubcontext(String name, Attributes attrs)
				throws NamingException {
			return null;
		}

		@Override
		public DirContext getSchema(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public DirContext getSchema(String name)
				throws NamingException {
			return null;
		}

		@Override
		public DirContext getSchemaClassDefinition(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public DirContext getSchemaClassDefinition(String name)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs, SearchControls cons)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs, SearchControls cons)
				throws NamingException {
			return null;
		}

		@Override
		public Object lookup(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public Object lookup(String name)
				throws NamingException {
			return null;
		}

		@Override
		public void bind(Name name, Object obj)
				throws NamingException {

		}

		@Override
		public void bind(String name, Object obj)
				throws NamingException {

		}

		@Override
		public void rebind(Name name, Object obj)
				throws NamingException {

		}

		@Override
		public void rebind(String name, Object obj)
				throws NamingException {

		}

		@Override
		public void unbind(Name name)
				throws NamingException {

		}

		@Override
		public void unbind(String name)
				throws NamingException {

		}

		@Override
		public void rename(Name oldName, Name newName)
				throws NamingException {

		}

		@Override
		public void rename(String oldName, String newName)
				throws NamingException {

		}

		@Override
		public NamingEnumeration<NameClassPair> list(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<NameClassPair> list(String name)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<Binding> listBindings(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public NamingEnumeration<Binding> listBindings(String name)
				throws NamingException {
			return null;
		}

		@Override
		public void destroySubcontext(Name name)
				throws NamingException {

		}

		@Override
		public void destroySubcontext(String name)
				throws NamingException {

		}

		@Override
		public Context createSubcontext(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public Context createSubcontext(String name)
				throws NamingException {
			return null;
		}

		@Override
		public Object lookupLink(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public Object lookupLink(String name)
				throws NamingException {
			return null;
		}

		@Override
		public NameParser getNameParser(Name name)
				throws NamingException {
			return null;
		}

		@Override
		public NameParser getNameParser(String name)
				throws NamingException {
			return null;
		}

		@Override
		public Name composeName(Name name, Name prefix)
				throws NamingException {
			return null;
		}

		@Override
		public String composeName(String name, String prefix)
				throws NamingException {
			return null;
		}

		@Override
		public Object addToEnvironment(String propName, Object propVal)
				throws NamingException {
			return null;
		}

		@Override
		public Object removeFromEnvironment(String propName)
				throws NamingException {
			return null;
		}

		@Override
		public Hashtable<?, ?> getEnvironment()
				throws NamingException {
			return null;
		}

		@Override
		public void close()
				throws NamingException {

		}

		@Override
		public String getNameInNamespace()
				throws NamingException {
			return null;
		}
	}
}
