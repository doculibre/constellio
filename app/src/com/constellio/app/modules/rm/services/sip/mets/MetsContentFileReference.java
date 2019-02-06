package com.constellio.app.modules.rm.services.sip.mets;

public class MetsContentFileReference {

	//	      <file CHECKSUM="CHECKSUM{{/data/X/X13/zeFolderId/document-document1-1.0.doc}}" CHECKSUMTYPE="SHA-256" DMDID="DOCUMENT-document1" ID="_document1" SIZE="188416">
	//        <FLocat LOCTYPE="URL" xlink:href="/data/X/X13/zeFolderId/document-document1-1.0.doc" xlink:title="content1.doc" />
	//      </file>

	String checkSum;

	String checkSumType;

	String dmdid;

	String id;

	long size;

	String path;

	String title;

	public String getCheckSum() {
		return checkSum;
	}

	public MetsContentFileReference setCheckSum(String checkSum) {
		this.checkSum = checkSum;
		return this;
	}

	public String getCheckSumType() {
		return checkSumType;
	}

	public MetsContentFileReference setCheckSumType(String checkSumType) {
		this.checkSumType = checkSumType;
		return this;
	}

	public String getDmdid() {
		return dmdid;
	}

	public MetsContentFileReference setDmdid(String dmdid) {
		this.dmdid = dmdid;
		return this;
	}

	public String getId() {
		return id;
	}

	public MetsContentFileReference setId(String id) {
		this.id = id;
		return this;
	}

	public long getSize() {
		return size;
	}

	public MetsContentFileReference setSize(long size) {
		this.size = size;
		return this;
	}

	public String getPath() {
		return path;
	}

	public MetsContentFileReference setPath(String path) {
		this.path = path;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public MetsContentFileReference setTitle(String title) {
		this.title = title;
		return this;
	}


}
