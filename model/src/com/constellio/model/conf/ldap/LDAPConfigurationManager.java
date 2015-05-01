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
package com.constellio.model.conf.ldap;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.model.conf.PropertiesModelLayerConfigurationRuntimeException;
import com.constellio.model.conf.ldap.services.LDAPConnectionFailure;
import com.constellio.model.conf.ldap.services.LDAPServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.sync.LDAPFastBind;
import com.constellio.model.services.users.sync.RuntimeNamingException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public class LDAPConfigurationManager implements StatefulService {
    private static final String LDAP_CONFIGS = "ldapConfigs.properties";
    private static final long MIN_DURATION = 1000 * 60 * 10;//10mns
    private final ModelLayerFactory modelLayerFactory;
    LDAPUserSyncConfiguration userSyncConfiguration;
    LDAPServerConfiguration serverConfiguration;
    ConfigManager configManager;

    public LDAPConfigurationManager(ModelLayerFactory modelLayerFactory, ConfigManager configManager) {
        this.configManager = configManager;
        this.modelLayerFactory = modelLayerFactory;
        configManager.createPropertiesDocumentIfInexistent(LDAP_CONFIGS, new PropertiesAlteration() {
            @Override
            public void alter(Map<String, String> properties) {
                //Default values
            }
        });

    }

    @Override
    public void initialize() {
        this.userSyncConfiguration = getLDAPUserSyncConfiguration();
        this.serverConfiguration = getLDAPServerConfiguration();
    }

    @Override
    public void close() {

    }

    public void saveLDAPConfiguration(final LDAPServerConfiguration ldapServerConfiguration, final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
        validateLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);

        configManager.updateProperties(LDAP_CONFIGS, new PropertiesAlteration() {
            @Override
            public void alter(Map<String, String> properties) {
                properties.put("ldap.serverConfiguration.urls.sharpSV", joinWithSharp(ldapServerConfiguration.getUrls()));
                properties.put("ldap.serverConfiguration.domains.sharpSV", joinWithSharp(ldapServerConfiguration.getDomains()));
                if (ldapServerConfiguration.getDirectoryType() != null) {
                    properties.put("ldap.serverConfiguration.directoryType", ldapServerConfiguration.getDirectoryType().getCode());
                }
                if (ldapServerConfiguration.getLdapAuthenticationActive()) {
                    properties.put("ldap.authentication.active", "" + ldapServerConfiguration.getLdapAuthenticationActive());
                } else {
                    properties.put("ldap.authentication.active", "" + false);
                }

                properties.put("ldap.syncConfiguration.user.login", ldapUserSyncConfiguration.getUser());
                properties.put("ldap.syncConfiguration.user.password", ldapUserSyncConfiguration.getPassword());
                properties.put("ldap.syncConfiguration.groupsBaseContextList.sharpSV", joinWithSharp(ldapUserSyncConfiguration.getGroupBaseContextList()));
                properties.put("ldap.syncConfiguration.usersWithoutGroupsBaseContextList.sharpSV", joinWithSharp(ldapUserSyncConfiguration.getUsersWithoutGroupsBaseContextList()));

                if (ldapUserSyncConfiguration.getUsersFilterAcceptanceRegex() != null) {
                    properties.put("ldap.syncConfiguration.userFilter.acceptedRegex", ldapUserSyncConfiguration.getUsersFilterAcceptanceRegex());
                }
                if (ldapUserSyncConfiguration.getUsersFilterRejectionRegex() != null) {
                    properties.put("ldap.syncConfiguration.userFilter.rejectedRegex", ldapUserSyncConfiguration.getUsersFilterRejectionRegex());
                }
                if (ldapUserSyncConfiguration.getGroupsFilterAcceptanceRegex() != null) {
                    properties.put("ldap.syncConfiguration.groupFilter.acceptedRegex", ldapUserSyncConfiguration.getGroupsFilterAcceptanceRegex());
                }
                if (ldapUserSyncConfiguration.getGroupsFilterRejectionRegex() != null) {
                    properties.put("ldap.syncConfiguration.groupFilter.rejectedRegex", ldapUserSyncConfiguration.getGroupsFilterRejectionRegex());
                }
                if (ldapUserSyncConfiguration.getDurationBetweenExecution() != null) {
                    if (ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis() >= MIN_DURATION) {
                        properties.put("ldap.syncConfiguration.durationBetweenExecution", format(ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis()) + "");
                    } else {
                        //FIXME
                        //throw TOOShortDuration();
                    }
                }
            }
        });
        modelLayerFactory.getLdapAuthenticationService().reloadServiceConfiguration();
        modelLayerFactory.getLdapUserSyncManager().reloadLDAPUserSynchConfiguration();
    }

    private String format(long millis) {
        //format (\\d*d)(\\d*h)(\\d*mn) (d == day)
        long seconds = millis/1000;
        long mns = seconds/60;
        long hours = mns/60;
        mns = mns%60;
        long days = hours/24;
        hours = hours%24;
        return days + "d" + hours + "h" + mns + "mn";
    }

    private String joinWithSharp(List<String> list) {
        return StringUtils.join(list, "#");
    }

    private void validateLDAPConfiguration(LDAPServerConfiguration configs, LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
        Boolean authenticationActive = configs.getLdapAuthenticationActive();
        if(authenticationActive){
            for(String url : configs.getUrls()){
                try{
                    LDAPFastBind fastBind = new LDAPFastBind(url);
                    fastBind.close();
                }catch(RuntimeNamingException e){
                    throw new InvalidUrlRuntimeException(url, e.getMessage());
                }
            }
            if(configs.getDomains() == null || configs.getDomains().isEmpty()){
                throw new EmptyDomainsRuntimeException();
            }
            if(configs.getUrls() == null || configs.getUrls().isEmpty()){
                throw new EmptyUrlsRuntimeException();
            }
            if(ldapUserSyncConfiguration.getDurationBetweenExecution()!= null && ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis() != 0l){
                if(ldapUserSyncConfiguration.getDurationBetweenExecution().getMillis() < MIN_DURATION){
                    throw new TooShortDurationRuntimeException(ldapUserSyncConfiguration.getDurationBetweenExecution());
                }
                LDAPServices ldapServices = new LDAPServices();
                for(String url : configs.getUrls()){
                    ldapServices.connectToLDAP(configs.getDomains(), url, ldapUserSyncConfiguration.getUser(), ldapUserSyncConfiguration.getPassword());
                }
            }
        }
    }

    public LDAPServerConfiguration getLDAPServerConfiguration() {
        Map<String, String> configs = configManager.getProperties(LDAP_CONFIGS).getProperties();

        List<String> urls = getSharpSeparatedValuesWithoutBlanks(configs, "ldap.serverConfiguration.urls.sharpSV", new ArrayList<String>());
        List<String> domains = getSharpSeparatedValuesWithoutBlanks(configs, "ldap.serverConfiguration.domains.sharpSV",
                new ArrayList<String>());
        LDAPDirectoryType directoryType = getLDAPDirectoryType(configs);

        Boolean active = getBooleanValue(configs, "ldap.authentication.active", false);

        return new LDAPServerConfiguration(urls, domains, directoryType, active);

    }

    public LDAPUserSyncConfiguration getLDAPUserSyncConfiguration() {
        Map<String, String> configs = configManager.getProperties(LDAP_CONFIGS).getProperties();
        String user = getString(configs, "ldap.syncConfiguration.user.login", null);
        List<String> groupBaseContextList = getSharpSeparatedValuesWithoutBlanks(configs, "ldap.syncConfiguration.groupsBaseContextList.sharpSV",
                new ArrayList<String>());
        List<String> usersWithoutGroupsBaseContextList = getSharpSeparatedValuesWithoutBlanks(configs,
                "ldap.syncConfiguration.usersWithoutGroupsBaseContextList.sharpSV", new ArrayList<String>());
        String password = getString(configs, "ldap.syncConfiguration.user.password", "");
        RegexFilter userFilter = newRegexFilter(configs, "ldap.syncConfiguration.userFilter.acceptedRegex", "ldap.syncConfiguration.userFilter.rejectedRegex");
        RegexFilter groupFilter = newRegexFilter(configs, "ldap.syncConfiguration.groupFilter.acceptedRegex", "ldap.syncConfiguration.groupFilter.rejectedRegex");
        Duration durationBetweenExecution = newDuration(configs, "ldap.syncConfiguration.durationBetweenExecution");

        return new LDAPUserSyncConfiguration(user, password, userFilter, groupFilter, durationBetweenExecution, groupBaseContextList, usersWithoutGroupsBaseContextList);
    }

    public boolean isLDAPAuthentication() {
        Map<String, String> configs = configManager.getProperties(LDAP_CONFIGS).getProperties();
        return getBooleanValue(configs, "ldap.authentication.active", false);
    }

    private boolean getBooleanValue(Map<String, String> configs, String key, boolean defaultValue) {
        String returnValueAsString = getString(configs, key, defaultValue + "");
        if(!returnValueAsString.toLowerCase().matches("true|false")){
            throw new PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue(key,
                    returnValueAsString);
        }
        return Boolean.valueOf(returnValueAsString);
    }

    private String getString(Map<String, String> configs, String key, String defaultValue) {
        String value = configs.get(key);
        return value == null ? defaultValue : value;
    }

    private LDAPDirectoryType getLDAPDirectoryType(Map<String, String> configs) {
        String directoryTypeString = getString(configs, "ldap.serverConfiguration.directoryType", LDAPDirectoryType.ACTIVE_DIRECTORY.getCode()).toLowerCase();
        if(StringUtils.isBlank(directoryTypeString) ||
                directoryTypeString.equals(LDAPDirectoryType.ACTIVE_DIRECTORY.getCode().toLowerCase())){
            return LDAPDirectoryType.ACTIVE_DIRECTORY;
        }else if (directoryTypeString.equals(LDAPDirectoryType.E_DIRECTORY.getCode().toLowerCase())){
            return LDAPDirectoryType.E_DIRECTORY;
        }else {
            throw new PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidLdapType(directoryTypeString);
        }
    }

    private List<String> getSharpSeparatedValuesWithoutBlanks(Map<String, String> configs, String key, ArrayList<String> defaultValues) {
        String allValuesString = getString(configs, key, "");
        String[] allVales = StringUtils.split(allValuesString, "#");
        if (allVales.length == 0){
            return defaultValues;
        }else {
            List<String> returnValues = new ArrayList<>();
            for(String currentValue: allVales){
                returnValues.add(currentValue.trim());
            }
            return returnValues;
        }
    }

    private Duration newDuration(Map<String, String> configs, String key) {
        String durationString = getString(configs, key, null);
        if(durationString != null){
            //format (\\d*d)(\\d*h)(\\d*mn) (d == day)
            PeriodFormatter formatter = new PeriodFormatterBuilder()
                    .appendDays()
                    .appendLiteral("d")
                    .appendHours()
                    .appendLiteral("h")
                    .appendMinutes()
                    .appendLiteral("mn")
                    .toFormatter();
            Duration duration = formatter.parsePeriod(durationString).toStandardDuration();
            return duration;
        }
        return null;
    }

    private RegexFilter newRegexFilter(Map<String, String> configs, String acceptedRegexKey, String rejectedRegexkey) {
        String acceptedRegex= getString(configs, acceptedRegexKey, null);
        String rejectedRegex= getString(configs, rejectedRegexkey, null);

        try{
            return new RegexFilter(acceptedRegex, rejectedRegex);
        }catch(PatternSyntaxException e){
            throw new PropertiesModelLayerConfigurationRuntimeException.PropertiesModelLayerConfigurationRuntimeException_InvalidRegex(acceptedRegex + " or " + rejectedRegex);
        }
    }

    public Boolean idUsersSynchActivated() {
        LDAPUserSyncConfiguration config = getLDAPUserSyncConfiguration();
        return config!=null && config.getDurationBetweenExecution() !=null;
    }
}
