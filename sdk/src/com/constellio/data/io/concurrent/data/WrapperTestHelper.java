package com.constellio.data.io.concurrent.data;

public interface WrapperTestHelper<T>{
	public byte[] getAValue();
	public void assertEquality(DataWrapper<T> d1, DataWrapper<T> d2);
	public void doModification(Object data);
	public DataWrapper<T> createEmptyData();
}