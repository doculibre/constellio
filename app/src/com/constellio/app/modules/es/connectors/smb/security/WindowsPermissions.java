/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.constellio.app.modules.es.connectors.smb.security;

import jcifs.smb.ACE;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * http://svn.apache.org/repos/asf/manifoldcf/trunk/connectors/jcifs/connector/src/main/java/org/apache/manifoldcf/crawler/
 * connectors/sharedrive/SharedDriveConnector.java
 *
 * @author Benoit, Nicolas
 */
public class WindowsPermissions {

	private static final Logger LOG = Logger.getLogger(WindowsPermissions.class.getName());

	private List<String> allowTokenShare = new ArrayList<String>();
	private List<String> denyTokenShare = new ArrayList<String>();
	private List<String> allowTokenDocument = new ArrayList<String>();
	private List<String> denyTokenDocument = new ArrayList<String>();

	private String permissionsHash = null;

	private SmbFile file;
	private TrusteeManager trusteeManager;
	private boolean skipSharePermissions;
	private boolean skipACL;

	private Set<String> errors = new HashSet<>();

	public WindowsPermissions(SmbFile file, TrusteeManager trusteeManager, boolean skipSharePermissions, boolean skipACL) {
		this.file = file;
		this.trusteeManager = trusteeManager;
		this.skipSharePermissions = skipSharePermissions;
		this.skipACL = skipACL;
	}

	public void process() {
		if (!skipACL) {
			boolean foundNovellPermissions = processNovellPermissions(file, trusteeManager);
			if (!foundNovellPermissions) {
				processNTFSPermissions(file);
				if (!skipSharePermissions) {
					processSharePermissions(file);
				}
			}
			computePermissionsHash();
		}
	}

	private void computePermissionsHash() {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");

			update(md, allowTokenDocument);
			update(md, denyTokenDocument);
			update(md, allowTokenShare);
			update(md, denyTokenShare);

			permissionsHash = Base64.encodeBase64String(md.digest());

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	private void update(MessageDigest md, List<String> tokens) {
		for (String token : tokens) {
			md.update(token.getBytes());
		}
	}

	public List<String> getAllowTokenDocument() {
		return allowTokenDocument;
	}

	public List<String> getAllowTokenShare() {
		return allowTokenShare;
	}

	public List<String> getDenyTokenDocument() {
		return denyTokenDocument;
	}

	public List<String> getDenyTokenShare() {
		return denyTokenShare;
	}

	public String getPermissionsHash() {
		return permissionsHash;
	}

	protected boolean processNTFSPermissions(SmbFile file) {
		ACE[] documentAces = null;
		for (int tries = 5; tries >= 0; tries--) {
			try {
				documentAces = file.getSecurity(true);
				break;
			} catch (IOException ioe) {
				if (ioe instanceof SmbException && StringUtils.containsIgnoreCase(ioe.getMessage(), "The system cannot find the file specified.")) {
					//Novell or missing file
					break;
				}
				if (tries == 0) {
					LOG.warning("Exception (NTFS PERMISSIONS)) : " + file.getCanonicalPath() + " (" + ioe.getClass()
							.getCanonicalName() + ": " + ioe.getMessage());
					errors.add("Exception (NTFS PERMISSIONS) :" + ioe.getMessage());
				}
			}
		}
		updateACE(documentAces, allowTokenDocument, denyTokenDocument);
		if (documentAces != null) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean processNovellPermissions(SmbFile file, TrusteeManager trusteeManager) {
		Set<String> names = trusteeManager.getNames(file);
		for (String name : names) {
			allowTokenDocument.add(name);
		}
		return !names.isEmpty();
	}

	protected void processSharePermissions(SmbFile file) {
		ACE[] shareAces = null;
		for (int tries = 5; tries >= 0; tries--) {
			try {
				shareAces = file.getShareSecurity(true);
				// Success, exit try-loop
				break;
			} catch (IOException ioe) {
				if (tries == 0) {
					LOG.warning("Exception (SHARE PERMISSIONS)) : " + file.getCanonicalPath() + " (" + ioe.getClass()
							.getCanonicalName() + ": " + ioe.getMessage());
					errors.add("Exception (SHARE PERMISSIONS) :" + ioe.getMessage());
				}
			}
		}
		updateACE(shareAces, allowTokenShare, denyTokenShare);
	}

	private void updateACE(ACE[] aces, List<String> allow, List<String> deny) {
		if (aces == null) {
			allow.add("S-1-1-0");
		} else {
			for (ACE ace : aces) {
				if ((ace.getAccessMask() & ACE.FILE_READ_DATA) != 0) {
					String aceSid = ace.getSID()
							.toString();
					if (ace.isAllow()) {
						allow.add(aceSid);
					} else {
						deny.add(aceSid);
					}
				}
			}
		}
		if (deny.isEmpty()) {
			deny.add("DEAD_AUTHORITY");
		}
	}

	public String getErrors() {
		String result = "";
		for (String error : errors) {
			result += error + ", ";
		}
		return result;
	}
}
