package com.constellio.data.dao.services.sql;

public class SqlRecordDaoFactory {

	private final SqlConnector sqlConnector;

	public SqlRecordDaoFactory(SqlConnector sqlConnector){

		this.sqlConnector = sqlConnector;
	}

	public SqlRecordDao getRecordDao(SqlRecordDaoType daoType){

		if(daoType==SqlRecordDaoType.RECORDS){
			return new MicrosoftSqlRecordTransactionDao(sqlConnector);
		}
		else if(daoType==SqlRecordDaoType.TRANSACTIONS){
			return new MicrosoftSqlTransactionDao(sqlConnector);
		}
		else{
			return new MicrosoftSqlTransactionDao(sqlConnector);
		}
	}

}
