/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.io.concurrent.data;

import static org.assertj.core.api.Assertions.assertThat;

public class TextWrapperTestHelper implements WrapperTestHelper<String>{

	@Override
	public byte[] getAValue() {
		String text = "it is a test";
		return text.getBytes();
	}
	
	@Override
	public StringView createEmptyData() {
		return new StringView();
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