package com.constellio.app.services.sip.mets;

public class MetsContentFileReference {

	private String checkSum;

	private String checkSumType;

	private String dmdid;

	private String id;

	private long size;

	private String path;

	private String title;

	private String use;

	public String getUse() {
		return use;
	}

	public MetsContentFileReference setUse(String use) {
		this.use = use;
		return this;
	}

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
