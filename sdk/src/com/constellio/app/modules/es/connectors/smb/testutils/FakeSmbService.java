package com.constellio.app.modules.es.connectors.smb.testutils;

import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;

import java.util.List;

public class FakeSmbService implements SmbShareService {
	private List<String> childrenUrls;
	private SmbFileDTO smbFileDTO;

	public FakeSmbService(List<String> childrenUrls) {
		this.childrenUrls = childrenUrls;
	}

	public FakeSmbService(SmbFileDTO smbFileDTO) {
		this.smbFileDTO = smbFileDTO;
	}

	@Override
	public SmbFileDTO getSmbFileDTO(String smbUrl) {
		return smbFileDTO;
	}

	@Override
	public List<String> getChildrenUrlsFor(String url) {
		return childrenUrls;
	}

	@Override
	public SmbModificationIndicator getModificationIndicator(String url) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public SmbFileDTO getSmbFileDTO(String url, boolean withAttachment) {
		return smbFileDTO;
	}
}
