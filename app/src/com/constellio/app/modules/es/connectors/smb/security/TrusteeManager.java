package com.constellio.app.modules.es.connectors.smb.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TrusteeManager {

	private static final String TRUSTEE_FOLDER = "trustee_const";

	private static final String TRUSTEE_ELEMENT = "trustee";
	private static final String INHERITED_RIGHTS_MASKS_ELEMENT = "inherited_rights_mask";
	private static final String PATH_ATTRIBUTE = "path";
	private static final String NAME_ELEMENT = "name";
	private static final String RIGHTS_ELEMENT = "rights";

	private Map<String, ReadTrustees> shareTrustees = new HashMap<String, ReadTrustees>();
	private Set<String> readMasks = new HashSet<String>();

	private boolean isTrusteeFilePresent = true;

	public TrusteeManager() {
	}

	private ReadTrustees getTrusteesForFile(SmbFile smbFile)
			throws ParserConfigurationException, SAXException, IOException {
		String share = smbFile.getShare();
		if (share != null) {
			ReadTrustees readTrustees = shareTrustees.get(share);
			if (readTrustees == null) {
				// Load a new trustee
				readTrustees = new ReadTrustees();
				shareTrustees.put(share, readTrustees);

				// SmbFile trusteeDatabaseFile = new SmbFile(smbFile, "/" + share + "/._NETWARE/.trustee_database.xml");
				SmbFile trusteeDatabaseFile = new SmbFile(smbFile, "/" + share + "/" + TRUSTEE_FOLDER + "/.trustee_database.xml");

				if (trusteeDatabaseFile.exists()) {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = null;
					InputStream trusteeIs = null;
					try {
						doc = dBuilder.parse(trusteeDatabaseFile.getInputStream());
					} finally {
						IOUtils.closeQuietly(trusteeIs);
					}
					doc.getDocumentElement().normalize();

					NodeList trusteeList = doc.getElementsByTagName(TRUSTEE_ELEMENT);

					for (int temp = 0; temp < trusteeList.getLength(); temp++) {
						Node nNode = trusteeList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) nNode;
							String path = eElement.getAttribute(PATH_ATTRIBUTE);
							String name = eElement.getElementsByTagName(NAME_ELEMENT).item(0).getTextContent();
							// String simpleName = StringUtils.substringBetween(name, ".");
							// if (StringUtils.startsWith(name, ".") && simpleName == null) {
							// //Case where the whole domain can access the path
							// simpleName = "Everyone";
							// }
							if (StringUtils.isNotBlank(name)) {
								String rights = eElement.getElementsByTagName(RIGHTS_ELEMENT).item(0).getTextContent();
								// R = read, S = admin
								if (StringUtils.contains(rights, "R") || StringUtils.contains(rights, "S")) {
									readTrustees.add(path, name);
								}
							}
						}
					}

					NodeList masksList = doc.getElementsByTagName(INHERITED_RIGHTS_MASKS_ELEMENT);
					for (int temp = 0; temp < masksList.getLength(); temp++) {
						Node nNode = masksList.item(temp);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element eElement = (Element) nNode;
							String path = eElement.getAttribute(PATH_ATTRIBUTE);
							String rights = eElement.getElementsByTagName(RIGHTS_ELEMENT).item(0).getTextContent();
							// R = read, S = admin
							if (StringUtils.contains(rights, "R") || StringUtils.contains(rights, "S")) {
								readMasks.add(path);
							}
						}
					}
				} else {
					isTrusteeFilePresent = false;
				}
			}
			return readTrustees;
		}
		return null;
	}

	public Set<String> getNames(SmbFile smbFile) {
		Set<String> names = new TreeSet<String>();
		try {
			if (!isTrusteeFilePresent) {
				return names;
			}
			ReadTrustees readTrustees = getTrusteesForFile(smbFile);

			String canonicalPath = smbFile.getCanonicalPath();
			String pathAfterProtocol = StringUtils.substringAfter(canonicalPath, "smb://");
			String contextPath = StringUtils.substringAfter(pathAfterProtocol, "/");
			String shareContextPath = "/" + StringUtils.substringAfter(contextPath, "/");
			String shareContextPathNoTrailing = StringUtils.removeEnd(shareContextPath, "/");

			Collection<String> pathTrustees = readTrustees.get(shareContextPathNoTrailing);
			if (pathTrustees != null) {
				names.addAll(pathTrustees);
			}
			if (!this.readMasks.contains(shareContextPathNoTrailing)) {
				// No mask, retrieve parent rights
				SmbFile parent;
				if (smbFile.isDirectory()) {
					parent = new SmbFile(smbFile, "..");
				} else {
					parent = new SmbFile(smbFile, ".");
				}
				if (parent.exists() && !parent.getCanonicalPath().equals(smbFile.getCanonicalPath())) {
					// Recursive
					names.addAll(getNames(parent));
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return names;
	}

	private static class ReadTrustees {
		private Map<String, Collection<String>> trusteesMap = new HashMap<String, Collection<String>>();

		public Collection<String> get(String path) {
			return trusteesMap.get(path);
		}

		public void add(String path, String name) {
			Collection<String> names = trusteesMap.get(path);
			if (names == null) {
				names = new TreeSet<String>();
				trusteesMap.put(path, names);
			}
			names.add(name.toLowerCase());
		}
	}

	public static void main(String[] args)
			throws SAXException, IOException, ParserConfigurationException {
		String user = "";
		String pass = "";
		String domain = "";

		String sharedFolder = "tmp";
		String path = "smb://WIN-87DVRU8DGM1/" + sharedFolder + "/ME/COM_SAGIR/Agent multiplicateur/test.rtf";
		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, pass);
		SmbFile smbFile = new SmbFile(path, auth);

		TrusteeManager trustee = new TrusteeManager();
		for (String name : trustee.getNames(smbFile)) {
			System.out.println(name);
		}
	}
}
