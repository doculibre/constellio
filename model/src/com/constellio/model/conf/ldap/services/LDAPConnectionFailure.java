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
