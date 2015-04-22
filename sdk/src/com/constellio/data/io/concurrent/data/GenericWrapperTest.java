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

import java.util.Arrays;

import org.apache.hadoop.hdfs.server.datanode.dataNodeHome_jsp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class GenericWrapperTest{
	@Parameters(name = "{index}: Test {0}")
	public static Iterable<Object[]> setUpParameters() {
		return Arrays.asList(new Object[][] { 
			{ new TextWrapperTestHelper() },
			{ new XmlWrapperTestHelper() },
		});
	}
	
	public GenericWrapperTest(WrapperTestHelper<?> testHelper) {
		this.testHelper = testHelper;
	}
	
	private WrapperTestHelper<?> testHelper;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void givenAByteArrayConstructedFromDataWhenConstructingAnotherDataFromTheByteArrayThenTwoDatasAreEqual(){
		//given
		DataWrapper data = testHelper.createEmptyData();
		data.init(testHelper.getAValue());
		
		//when
		DataWrapper newTextData = testHelper.createEmptyData();
		newTextData.init(data.toBytes());
		
		//then
		testHelper.assertEquality(data, newTextData);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void givenADataWhenModifingBinaryDataThenTheDataIsImutable(){
		//given
		DataWrapper dataWrapper = testHelper.createEmptyData();
		dataWrapper.init(testHelper.getAValue());
		byte[] bytes = dataWrapper.toBytes();
		byte[] copy = Arrays.copyOf(bytes, bytes.length);

		//when
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = 0;
		
		//then
		assertThat(copy).isEqualTo(dataWrapper.toBytes());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> void givenADataWrapperWhenConveringToDataWithVersionAndGetItBackTheValuesAreTheSame(){
		DataWithVersion dataWithVersion = new DataWithVersion(testHelper.getAValue(), null);
		
		DataWrapper createByDataVersion = testHelper.createEmptyData();
		createByDataVersion = dataWithVersion.getView(createByDataVersion);
		
		DataWrapper dataWrapper = testHelper.createEmptyData();
		dataWrapper.init(testHelper.getAValue());
		testHelper.assertEquality(dataWrapper, createByDataVersion);
	}
	
}
