package com.constellio.app.modules.es.connectors.smb.service;

import java.util.List;

public interface SmbShareService {
	public SmbFileDTO getSmbFileDTO(String url);

	public SmbFileDTO getSmbFileDTO(String url, boolean withAttachment);

	public List<String> getChildrenUrlsFor(String url);

	public SmbModificationIndicator getModificationIndicator(String url);
}
