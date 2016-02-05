package com.constellio.app.modules.es.connectors.ldap;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType;

public class ConnectorLDAPUserDocumentFactory {
	public static ConnectorLDAPUserDocument populateUser(ConnectorLDAPUserDocument document, LDAPObjectAttributes ldapObject,
			ConnectorLDAPInstance connectorInstance) {
		String username = getStringValue(connectorInstance, ConnectorLDAPInstance.USERNAME_ATTRIBUTE_NAME,
				ldapObject);
		String firstName = getStringValue(connectorInstance, ConnectorLDAPInstance.FIRST_NAME_ATTRIBUTE_NAME,
				ldapObject);
		String lastName = getStringValue(connectorInstance, ConnectorLDAPInstance.LAST_NAME_ATTRIBUTE_NAME,
				ldapObject);
		String email = getStringValue(connectorInstance, ConnectorLDAPInstance.EMAIL_ATTRIBUTE_NAME, ldapObject);
		String address = getStringValue(connectorInstance, ConnectorLDAPInstance.ADDRESS_ATTRIBUTE_NAME, ldapObject,
				" ");
		String distinguishedName = getStringValue(connectorInstance,
				ConnectorLDAPInstance.DISTINGUISHED_NAME_ATTRIBUTE_NAME,
				ldapObject);
		String workTitle = getStringValue(connectorInstance, ConnectorLDAPInstance.WORK_TITLE_ATTRIBUTE_NAME,
				ldapObject);
		List<String> telephones = getTelephones(connectorInstance, ldapObject);
		String displayName = getStringValue(connectorInstance, ConnectorLDAPInstance.DISPLAY_NAME_ATTRIBUTE_NAME,
				ldapObject);
		String company = getStringValue(connectorInstance, ConnectorLDAPInstance.COMPANY_ATTRIBUTE_NAME, ldapObject);
		String manager = getStringValue(connectorInstance, ConnectorLDAPInstance.MANAGER_ATTRIBUTE_NAME, ldapObject);
		String department = getStringValue(connectorInstance, ConnectorLDAPInstance.DEPARTMENT_ATTRIBUTE_NAME,
				ldapObject);
		DirectoryType directoryType = connectorInstance.getDirectoryType();

		boolean enabled;
		if (isUserEnabled(ldapObject, directoryType)) {
			enabled = true;
		} else {
			enabled = false;
		}
		return document.setEnabled(enabled).setUsername(username).setFirstName(firstName).setLastName(lastName).setEmail(email)
				.setDistinguishedName(distinguishedName).setAddress(address).setWorkTitle(workTitle).setTelephone(telephones)
				.setDisplayName(displayName).setCompany(company).setManager(manager).setDepartment(department);

	}

	private static List<String> getTelephones(ConnectorLDAPInstance connectorInstance,
			LDAPObjectAttributes ldapObject) {
		List<String> returnList = new ArrayList<>();
		List<String> telephonesAttributesNames = connectorInstance.getTelephone();
		if (telephonesAttributesNames != null) {
			for (String attributeName : telephonesAttributesNames) {
				LDAPObjectAttribute attribute = ldapObject.get(attributeName);
				if (attribute != null) {
					String value = attribute.getStringValue();
					if (StringUtils.isNotBlank(value)) {
						returnList.add(value);
					}
				}
			}
		}
		return returnList;
	}

	private static String getStringValue(ConnectorLDAPInstance connectorInstance, String metadataName,
			LDAPObjectAttributes ldapObject) {
		return getStringValue(connectorInstance, metadataName, ldapObject, "");
	}

	private static String getStringValue(ConnectorLDAPInstance connectorInstance, String metadataName,
			LDAPObjectAttributes ldapObject, String valuesSeparator) {
		StringBuilder stb = new StringBuilder();
		Object value = connectorInstance.get(metadataName);
		List<String> attributesNames = new ArrayList<>();
		if (value instanceof List) {
			attributesNames.addAll((List<String>) value);
		} else {
			attributesNames.add((String) value);
		}
		for (String attributeName : attributesNames) {
			LDAPObjectAttribute attribute = ldapObject.get(attributeName);
			if (attribute != null) {
				List<Object> currentValue = attribute.getValue();
				if (currentValue != null) {
					for (Object subValue : currentValue) {
						stb.append(subValue + valuesSeparator);
					}
				}
			}
		}
		return stb.toString();
	}

	private static boolean isUserEnabled(LDAPObjectAttributes ldapObject, DirectoryType directoryType) {
		switch (directoryType) {
		case ACTIVE_DIRECTORY:
			return isADUserEnabled(ldapObject);
		case E_DIRECTORY:
			return isEDirectoryEnabled(ldapObject);
		default:
			throw new RuntimeException("Unsupported type " + directoryType);
		}
	}

	private static boolean isEDirectoryEnabled(LDAPObjectAttributes ldapObject) {
		//TODO
		return true;
	}

	private static boolean isADUserEnabled(LDAPObjectAttributes ldapObject) {
		boolean enabled = false;
		LDAPObjectAttribute enabledAtt = ldapObject.get("userAccountControl");
		if (enabledAtt != null) {
			List<Object> enabledAttribute = enabledAtt.getValue();
			if (enabledAttribute != null && !enabledAttribute.isEmpty()) {
				long lng = Long.parseLong(enabledAttribute.get(0).toString());
				long secondBit = lng & 2; // get bit 2
				if (secondBit == 0) {
					enabled = true;
				}
			}
		}
		return enabled;
	}
}
