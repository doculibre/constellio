package com.constellio.app.modules.es.connectors.smb.service;

import java.util.List;

public interface SmbService {
	public SmbFileDTO getSmbFileDTO(String url);
	public SmbFileDTO getSmbFileDTO(String url, boolean withAttachment);

	public List<String> getChildrenUrlsFor(String url);

	public SmbModificationIndicator getModificationIndicator(String url);

	public static class SmbModificationIndicator {
		private String permissionsHash = "Hash to replace";
		private double size = -4;
		private long lastModified = 0;

		public String getPermissionsHash() {
			return permissionsHash;
		}

		public void setPermissionsHash(String permissionsHash) {
			this.permissionsHash = permissionsHash;
		}

		public double getSize() {
			return size;
		}

		public void setSize(double size) {
			this.size = size;
		}

		public long getLastModified() {
			return lastModified;
		}

		public void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}

	}
}
