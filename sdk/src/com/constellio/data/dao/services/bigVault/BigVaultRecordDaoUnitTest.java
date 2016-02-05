package com.constellio.data.dao.services.bigVault;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;

public class BigVaultRecordDaoUnitTest {

	static List<Object> listWithOneNull = new ArrayList<>();

	static {
		listWithOneNull.add(null);
	}

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
		assertThat(recordDao.convertBigVaultValueToSolrValue("textMetadata_s", null)).isNull();
	}

	@Test
	public void givenBooleanMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertBigVaultValueToSolrValue("booleanMetadata_s", null)).isNull();
	}

	@Test
	public void givenNumberMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		assertThat(recordDao.convertBigVaultValueToSolrValue("numberMetadata_d", null)).isNull();
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
		assertThat(nullValue).isNull();
	}

	@Test
	public void givenTextMultivalueMetadataWhenConvertingEmptyListForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("textMultivalueMetadata_ss", new ArrayList<>());
		assertThat(nullValue).isNull();
	}

	@Test
	public void givenTextMultivalueMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		Object returnedValue = recordDao.convertSolrValueToBigVaultValue("textMultivalueMetadata_ss", listWithOneNull);
		assertThat(returnedValue).isEqualTo(listWithOneNull);
	}

	@Test
	public void givenBooleanMultivalueMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("booleanMultivalueMetadata_ss", null);
		assertThat(nullValue).isNull();
	}

	@Test
	public void givenBooleanMultivalueMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		Object returnedValue = recordDao
				.convertSolrValueToBigVaultValue("booleanMultivalueMetadata_ss", listWithOneNull);
		assertThat(returnedValue).isEqualTo(listWithOneNull);
	}

	@Test
	public void givenNumberMultivalueMetadataWhenConvertingNullForSolrValueThenRightValueReturned()
			throws Exception {
		Object nullValue = recordDao.convertBigVaultValueToSolrValue("numberMultivalueMetadata_ds", null);
		assertThat(nullValue).isNull();

	}

	@Test
	public void givenNumberMultivalueMetadataWhenConvertingSolrValueForNullThenRightValueReturned()
			throws Exception {
		Object returnedValue = recordDao.convertSolrValueToBigVaultValue("numberMultivalueMetadata_ds", asList(
				Integer.MIN_VALUE));
		assertThat(returnedValue).isEqualTo(asList(Integer.MIN_VALUE));
	}

	@Test
	public void whenFlushingThenSoftCommitBigVaultSolrServer()
			throws Exception {

		recordDao.flush();

		verify(bigVaultServer).softCommit();

	}
}
