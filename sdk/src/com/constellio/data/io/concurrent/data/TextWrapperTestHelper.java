package com.constellio.data.io.concurrent.data;

import static org.assertj.core.api.Assertions.assertThat;

public class TextWrapperTestHelper implements WrapperTestHelper<String>{

	@Override
	public byte[] getAValue() {
		String text = "it is a test";
		return text.getBytes();
	}
	
	@Override
	public TextView createEmptyData() {
		return new TextView();
	}

	@Override
	public void assertEquality(DataWrapper<String> textData, DataWrapper<String> newTextData) {
		assertThat(textData.getData()).isEqualTo(newTextData.getData());
	}

	@Override
	public void doModification(Object data) {
		String str = (String) data;
		str = str.substring(str.length());
	}
	
}