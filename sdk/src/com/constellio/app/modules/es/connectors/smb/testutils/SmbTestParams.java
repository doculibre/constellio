package com.constellio.app.modules.es.connectors.smb.testutils;

import java.util.Arrays;
import java.util.List;

public class SmbTestParams {
	public static final String EXISTING_SHARE = "smb://ip/";
	public static final String EXISTING_FOLDER = "folder/";
	public static final String EXISTING_FILE = "file.txt";
	public static final String EXISTING_FILE_EXT = "txt";
	public static final String EXISTING_FILE_CONTENT = "file content";
	public static final long EXISTING_FILE_LENGTH = EXISTING_FILE_CONTENT.length();
	public static final String EXISTING_FILE_LANG = "fr";
	public static final String EXISTING_FILE_PERMISSION_HASH = "permissionHash";

	public static final String GONE_FILE = "gonefile";
	public static final String DIFFERENT_SHARE = "smb://differentip/";
	public static final String TRAVERSAL_CODE = "zeTaversalCode";

	public static final String DOMAIN = "domain";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String INSTANCE_CODE = "smbInstanceCode";
	public static final String CONNECTOR_TITLE = "smbConnectorTitle";
	public static final String CONNECTOR_OBSERVER = "defaultConnectorObserver";

	public static final List<String> ALLOW_TOKENS = Arrays.asList("allowToken1", "allowToken2");
	public static final List<String> ALLOW_SHARE_TOKENS = Arrays.asList("allowShareToken1", "allowShareToken2");
	public static final List<String> DENY_TOKENS = Arrays.asList("denyToken1", "denyToken2");
	public static final List<String> DENY_SHARE_TOKENS = Arrays.asList("denyShareToken1", "denyShareToken2");

	public static final String FILE_NAME = "file.txt";
	public static final String FILE_CONTENT = "This file is not empty";

	public static final String FOLDER_NAME = "folder/";

	public static final String ANOTHER_FILE_NAME = "another_file.txt";
	public static final String ANOTHER_FILE_CONTENT = "Also not empty";
}
