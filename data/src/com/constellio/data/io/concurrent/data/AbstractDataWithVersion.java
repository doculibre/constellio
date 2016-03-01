package com.constellio.data.io.concurrent.data;

public abstract class AbstractDataWithVersion<T> implements DataWrapper<T> {
	protected DataWithVersion dataWithVersion;
	
	@Override
	public void initWithDataWithVersion(DataWithVersion dataWithVersion) {
		this.dataWithVersion = dataWithVersion;
		init(dataWithVersion.getData());
	}
	
	@Override
	public DataWithVersion toDataWithVersion() {
		if (dataWithVersion == null)
			dataWithVersion = new DataWithVersion(null, null);
		return dataWithVersion.setData(toBytes());
	}
}
