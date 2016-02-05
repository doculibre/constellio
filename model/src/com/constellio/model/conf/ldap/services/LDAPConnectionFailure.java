package com.constellio.model.conf.ldap.services;

import org.apache.commons.lang.StringUtils;

public class LDAPConnectionFailure extends RuntimeException {
    private final String url;
    private final Object[] domains;
    private final String user;

    public LDAPConnectionFailure(Object[] domains, String url, String user) {
        super("Couldn't connect with user " + user + " to " + url + ", with domains " + StringUtils.join(domains, ","));
        this.url = url;
        this.domains = domains;
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public Object[] getDomains() {
        return domains;
    }

    public String getUser() {
        return user;
    }
}
