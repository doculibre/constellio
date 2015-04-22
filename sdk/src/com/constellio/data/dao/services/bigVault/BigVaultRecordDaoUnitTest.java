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
package com.constellio.data.dao.services.bigVault;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;

public class BigVaultRecordDaoUnitTest {

	@Mock DataLayerLogger dataLayerLogger;
	@Mock DataStoreTypesFactory typesFactory;
	@Mock BigVaultServer bigVaultServer;

	BigVaultRecordDao recordDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		recordDao = new BigVaultRecordDao(bigVaultServer, typesFactory, null, dataLayerLogger);
	}

	@Test
	public void givenTextMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertBigVaultValueToSolrValue("textMetadata_s", null)).isEqualTo("__NULL__");
	}

	@Test
	public void givenTextMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertSolrValueToBigVaultValue("textMetadata_s", "__NULL__")).isNull();
	}

	@Test
	public void givenBooleanMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertBigVaultValueToSolrValue("booleanMetadata_s", null)).isEqualTo("__NULL__");
	}

	@Test
	public void givenBooleanMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertSolrValueToBigVaultValue("booleanMetadata_s", "__NULL__")).isNull();
	}

	@Test
	public void givenNumberMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertBigVaultValueToSolrValue("numberMetadata_d", null)).isEqualTo(Integer.MIN_VALUE);
	}

	@Test
	public void givenNumberMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertSolrValueToBigVaultValue("numberMetadata_d", Integer.MIN_VALUE)).isNull();
	}

	@Test
	public void givenTextMultivalueMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("textMultivalueMetadata_ss", null);
		assertThat(nullValue).isInstanceOf(ArrayList.class);
		assertThat((ArrayList) nullValue).containsExactly("__NULL__");
	}

	@Test
	public void givenTextMultivalueMetadataWhenConvertingEmptyListForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("textMultivalueMetadata_ss", new ArrayList<>());
		assertThat(nullValue).isInstanceOf(ArrayList.class);
		assertThat((ArrayList) nullValue).containsExactly("__NULL__");
	}

	@Test
	public void givenTextMultivalueMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		Object returnedValue = recordDao.convertSolrValueToBigVaultValue("textMultivalueMetadata_ss", Arrays.asList("__NULL__"));
		assertThat(returnedValue).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenBooleanMultivalueMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("booleanMultivalueMetadata_ss", null);
		assertThat(nullValue).isInstanceOf(ArrayList.class);
		assertThat((ArrayList) nullValue).containsExactly("__NULL__");
	}

	@Test
	public void givenBooleanMultivalueMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		Object returnedValue = recordDao
				.convertSolrValueToBigVaultValue("booleanMultivalueMetadata_ss", Arrays.asList("__NULL__"));
		assertThat(returnedValue).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenNumberMultivalueMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("numberMultivalueMetadata_ds", null);
		assertThat(nullValue).isInstanceOf(ArrayList.class);
		assertThat((ArrayList) nullValue).containsExactly(Integer.MIN_VALUE);

	}

	@Test
	public void givenNumberMultivalueMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		Object returnedValue = recordDao.convertSolrValueToBigVaultValue("numberMultivalueMetadata_ds", Arrays.asList(
				Integer.MIN_VALUE));
		assertThat(returnedValue).isEqualTo(new ArrayList<>());
	}

	@Test
	public void whenFlushingThenSoftCommitBigVaultSolrServer()
			throws Exception {

		recordDao.flush();

		verify(bigVaultServer).softCommit();

	}
}
