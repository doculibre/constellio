package com.constellio.app.api.extensions.params;

import com.constellio.app.services.sip.record.CollectionInfosSIPWriter;
import com.constellio.app.services.sip.record.RecordSIPWriter;
import com.constellio.app.services.sip.zip.SIPZipWriter;

public class ExportCollectionInfosSIPParams {

	CollectionInfosSIPWriter collectionInfosWriter;
	RecordSIPWriter recordSIPWriter;
	SIPZipWriter sipZipWriter;

	public ExportCollectionInfosSIPParams(CollectionInfosSIPWriter collectionInfosWriter,
										  RecordSIPWriter recordSIPWriter,
										  SIPZipWriter sipZipWriter) {
		this.collectionInfosWriter = collectionInfosWriter;
		this.recordSIPWriter = recordSIPWriter;
		this.sipZipWriter = sipZipWriter;
	}

	public CollectionInfosSIPWriter getCollectionInfosWriter() {
		return collectionInfosWriter;
	}

	public SIPZipWriter getSipZipWriter() {
		return sipZipWriter;
	}

	public RecordSIPWriter getRecordSIPWriter() {
		return recordSIPWriter;
	}
}
