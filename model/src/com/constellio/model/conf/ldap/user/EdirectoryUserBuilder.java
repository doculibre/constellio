package com.constellio.model.conf.ldap.user;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import org.apache.commons.lang.StringUtils;

public class EdirectoryUserBuilder extends CommonUserBuilder implements LDAPUserBuilder {

	public static final String MEMBER_OF = "groupMembership";
	public static final String USER_ACCOUNT_CONTROL = "loginDisabled";
	public static final String NAME = "uid";
	public static final String SAM_ACCOUNT_NAME = "uid";
	public static final String LAST_LOGON = "loginTime";
	private static final String LIEU_TRAVAIL = "l";
	private static final String MS_EXCH_DELEGATE_LIST_BL = "msExchDelegateListBL";

	@Override
	protected Date buildLastLogonAttribute(Attribute lastLogonAttribute)
			throws NamingException {
		String loginTimeAsString = getFirstString(lastLogonAttribute);
		Date loginDate = formatLoginDate(loginTimeAsString);
		return loginDate;
	}

	private static Date formatLoginDate(String loginTimeAsString) {
		// String loginTimeAsString = "20130604184217Z";
		Date d = null;
		loginTimeAsString = StringUtils.substring(loginTimeAsString, 0, 8);
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(loginTimeAsString, pos);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return d;
	}

	@Override
	protected Boolean buildEnabled(Attribute enabledAttribute)
			throws NamingException {
		String disabledAsString = getFirstString(enabledAttribute)
				.toLowerCase().trim();
		if (disabledAsString.equals("true")) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected String getLastLoginAttributeName() {
		return LAST_LOGON;
	}

	@Override
	protected String getCompanyAttributeName() {
		return LIEU_TRAVAIL;
	}

	@Override
	protected String getGroupAttributeName() {
		return MEMBER_OF;
	}

	@Override
	protected String getNameAttributeName() {
		return NAME;
	}

	@Override
	protected String getEnabledAttributeName() {
		return USER_ACCOUNT_CONTROL;
	}

	@Override
	public String getUserIdAttribute() {
		return "dn";
	}

	public static void main(String[] args)
			throws NamingException {
		System.out.println(formatLoginDate("20130604184217Z"));
		Attributes attrs = new BasicAttributes(USER_ACCOUNT_CONTROL, "FALSE");
		EdirectoryUserBuilder test = new EdirectoryUserBuilder();
		Attribute enabledAttribute = attrs.get(test.getEnabledAttributeName());
		System.out.println(test.buildEnabled(enabledAttribute));
	}

	@Override
	protected Boolean getDefaultValueIfIsEnabledAttributeNull() {
		return true;
	}

	@Override
	public String getMsExchDelegateListBl() {
		return MS_EXCH_DELEGATE_LIST_BL;
	}

}
