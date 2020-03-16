package com.constellio.data.dao.services.sql;

import com.constellio.data.dao.dto.sql.RecordTransactionSqlDTO;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MicrosoftSqlRecordTransactionDao implements SqlRecordDao<RecordTransactionSqlDTO> {

	private final SqlConnector connector;
	public static final String TABLE_NAME = "records";
	public static final String SCHEMA_NAME = "constellio";
	private static final String DBO = "dbo";
	private static String fullTableName = SCHEMA_NAME + "." + DBO + "." + TABLE_NAME;
	private final QueryRunner queryRunner;
	private ScalarHandler<Integer> defaultHandler = new ScalarHandler<>();

	public MicrosoftSqlRecordTransactionDao(SqlConnector connector) {
		queryRunner = new QueryRunner();
		this.connector = connector;
	}

	public MicrosoftSqlRecordTransactionDao(SqlConnector connector, QueryRunner queryRunner) {
		this.queryRunner = queryRunner;
		this.connector = connector;
	}

	@Override
	public void insert(String query) throws SQLException {

		int numberOfRowsUpdated = queryRunner.insert(connector.getConnection(), query, defaultHandler);
	}

	@Override
	public void insert(RecordTransactionSqlDTO dto) throws SQLException {

		String insertQuery = "INSERT INTO " + fullTableName
							 + " (id, recordId, solrVersion, content) "
							 + "VALUES ( default , ?, ?, ?)";

		queryRunner.insert(connector.getConnection(),
				insertQuery, defaultHandler, dto.getRecordId(),
				dto.getSolrVersion(), dto.getContent());

		//dto.setId(newId);
	}

	@Override
	public void insertBulk(List<RecordTransactionSqlDTO> dtos) throws SQLException {

		String insertQuery = "INSERT INTO " + fullTableName
							 + " (id, recordId, logVersion, solrVersion, content) "
							 + "VALUES ( default ,?, ?, ?, ?)";

		Connection connection = connector.getConnection();
		PreparedStatement ps = connection.prepareStatement(insertQuery);

		final int batchSize = 1000;
		int count = 0;

		for (RecordTransactionSqlDTO transactions : dtos) {

			ps.setString(1, transactions.getRecordId());
			ps.setInt(2, transactions.getLogVersion());
			ps.setString(3, transactions.getSolrVersion());
			ps.setString(4, transactions.getContent());
			ps.addBatch();

			if (++count % batchSize == 0) {
				ps.executeBatch();
			}
		}
		ps.executeBatch();
	}

	@Override
	public void updateBulk(List<RecordTransactionSqlDTO> dtos) throws SQLException {

		String updateQuery = "UPDATE " + fullTableName
							 + " SET logVersion = ?, solrVersion= ?, content= JSON_MODIFY(content,'append $', JSON_QUERY(?)) "
							 + "WHERE recordId = ? ";

		Connection connection = connector.getConnection();
		PreparedStatement ps = connection.prepareStatement(updateQuery);

		final int batchSize = 1000;
		int count = 0;

		for (RecordTransactionSqlDTO transactions : dtos) {

			ps.setInt(1, transactions.getLogVersion());
			ps.setString(2, transactions.getSolrVersion());
			ps.setString(3, transactions.getContent());
			ps.setString(4, transactions.getRecordId());
			ps.addBatch();

			if (++count % batchSize == 0) {
				ps.executeBatch();
			}
		}
		ps.executeBatch();
	}

	@Override
	public RecordTransactionSqlDTO get(int id) throws SQLException {

		ResultSetHandler<RecordTransactionSqlDTO> handler = new BeanHandler<>(RecordTransactionSqlDTO.class);

		String selectQuery = "SELECT * FROM " + fullTableName + " WHERE id=?";

		RecordTransactionSqlDTO dto = queryRunner.query(connector.getConnection(),
				selectQuery, handler, id);

		return dto;
	}

	@Override
	public RecordTransactionSqlDTO get(String recordId) throws SQLException {

		ResultSetHandler<RecordTransactionSqlDTO> handler = new BeanHandler<>(RecordTransactionSqlDTO.class);
		String fetchQuery = "SELECT TOP 1 * FROM " + fullTableName + " WHERE recordId=?";

		RecordTransactionSqlDTO dto = queryRunner.query(connector.getConnection(),
				fetchQuery, handler, recordId);

		return dto;
	}

	@Override
	public ResultSetHandler<RecordTransactionSqlDTO> realGet(int id) throws SQLException {
		return null;
	}

	@Override
	public List<RecordTransactionSqlDTO> getAll() throws SQLException {
		ResultSetHandler<List<RecordTransactionSqlDTO>> handler = new BeanListHandler<>(RecordTransactionSqlDTO.class);

		List<RecordTransactionSqlDTO> dto = queryRunner.query(connector.getConnection(),
				"SELECT * FROM records", handler);

		if (dto == null) {
			return new ArrayList<>();
		}
		return dto;
	}

	@Override
	public List<RecordTransactionSqlDTO> getAll(int top) throws SQLException {

		if (top < 1) {
			return getAll();
		}
		ResultSetHandler<List<RecordTransactionSqlDTO>> handler = new BeanListHandler<>(RecordTransactionSqlDTO.class);

		List<RecordTransactionSqlDTO> dto = queryRunner.query(connector.getConnection(),
				"SELECT TOP(" + top + ") * FROM records", handler);

		if (dto == null) {
			return new ArrayList<>();
		}
		return dto;
	}

	@Override
	public void delete(String id) throws SQLException {
		String deleteQuery = "DELETE FROM " + fullTableName + " WHERE id =?";
		queryRunner.execute(connector.getConnection(),
				deleteQuery, id);
	}

	@Override
	public void deleteAll() throws SQLException {

		String deleteQuery = "DELETE FROM " + fullTableName + "";
		queryRunner.execute(connector.getConnection(),
				deleteQuery);
	}

	@Override
	public void deleteAll(String[] ids) throws SQLException {

		if (ids.length > 0) {
			String joinedIds = "'" + String.join("','", ids) + "'";

			String deleteQuery = String.format("DELETE FROM " + fullTableName + " WHERE IN (%s)", joinedIds);
			queryRunner.execute(connector.getConnection(),
					deleteQuery);
		}
	}

	@Override
	public void deleteAll(List<String> ids) throws SQLException {
		String deleteQuery = "DELETE FROM " + fullTableName + " WHERE recordId = ?";
		for (String id : ids) {
			queryRunner.execute(connector.getConnection(),
					deleteQuery, id);
		}
	}

	@Override
	public void deleteAllByLogVersion(int logVersion) throws SQLException {
		String deleteQuery = "DELETE FROM " + fullTableName + " WHERE logVersion < ?";

		queryRunner.execute(connector.getConnection(),
				deleteQuery, logVersion);

	}

	@Override
	public void update(RecordTransactionSqlDTO dto) throws SQLException {

		String updateQuery = "UPDATE " + fullTableName + " tr "
							 + "SET tr.recordId=?, vtr.solrVersion=?, tr.content=?) "
							 + "WHERE id=?";
		queryRunner.update(connector.getConnection(), updateQuery, dto.getRecordId(),
				dto.getSolrVersion(), dto.getContent());
	}

	@Override
	public int increaseVersion() throws SQLException {

		String updateQuery = "UPDATE versions SET version = version + 1 WHERE name = 'transactionLog' ";
		queryRunner.update(connector.getConnection(),
				updateQuery);
		return getCurrentVersion();
	}

	@Override
	public int getCurrentVersion() throws SQLException {

		ScalarHandler<Integer> scalarHandler = new ScalarHandler<>();

		Integer version = queryRunner.query(connector.getConnection(),
				"SELECT version FROM versions WHERE name = 'transactionLog' ", scalarHandler);

		if (version == null) {
			String insertQuery = "INSERT INTO versions "
								 + "(name, version) "
								 + "VALUES ('transactionLog', 1)";

			queryRunner.insert(connector.getConnection(),
					insertQuery, defaultHandler);
			return 1;
		}

		return version;
	}

	@Override
	public void resetVersion() throws SQLException {
		throw new NotImplementedException();
	}

	@Override
	public void flush() {

	}

	@Override
	public long getTableCount() throws SQLException {

		ScalarHandler<Long> scalarHandler = new ScalarHandler<>();
		String fetchQuery = "SELECT COUNT(*) FROM " + fullTableName;

		long count = ((Number) queryRunner.query(connector.getConnection(),
				fetchQuery, scalarHandler)).longValue();

		return count;
	}
}