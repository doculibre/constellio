package com.constellio.model.services.records.cache;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.cache.CompiledDTOStats.CompiledDTOStatsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.CharEncoding.UTF_8;

public class DTOUtilsByteArrayDataOutputStream implements Closeable {
	ByteArrayOutputStream byteArrayOutputStream;
	DataOutputStream dataOutputStream;
	boolean persisted;

	int length;
	List<List<Object>> debugInfos;

	CompiledDTOStatsBuilder statsBuilder;

	public DTOUtilsByteArrayDataOutputStream(boolean persisted, CompiledDTOStatsBuilder statsBuilder) {
		this.byteArrayOutputStream = new ByteArrayOutputStream();
		this.dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		this.persisted = persisted;
		this.statsBuilder = statsBuilder;

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			debugInfos = new ArrayList<>();
		}
	}

	public void write(Metadata relatedMetadata, @NotNull byte[] b) throws IOException {
		logLength(relatedMetadata, b.length);
		dataOutputStream.write(b);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, b.length, "Byte array", StringUtils.join(b, ','));
		}
	}

	public void write(Metadata relatedMetadata, int b) throws IOException {
		logLength(relatedMetadata, Byte.BYTES);
		dataOutputStream.write(b);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Byte.BYTES, "int as byte", "" + b);
		}
	}

	public void write(Metadata relatedMetadata, byte[] b, int off, int len) throws IOException {
		logLength(relatedMetadata, len);
		dataOutputStream.write(b, off, len);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Byte.BYTES, "bytes array at", "" + StringUtils.join(b, ",") + "[" + off + "," + len + "]");
		}
	}

	public void flush() throws IOException {
		dataOutputStream.flush();
	}

	public void writeBoolean(Metadata relatedMetadata, boolean v) throws IOException {
		logLength(relatedMetadata, Byte.BYTES);
		dataOutputStream.writeBoolean(v);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Byte.BYTES, "boolean", "" + v);
		}
	}

	public void writeByte(Metadata relatedMetadata, int v) throws IOException {
		logLength(relatedMetadata, Byte.BYTES);
		dataOutputStream.writeByte(v);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Byte.BYTES, "byte", "" + v);
		}
	}

	public void writeShort(Metadata relatedMetadata, int v)
			throws IOException {
		if (v > Short.MAX_VALUE || v < Short.MIN_VALUE) {
			throw new IllegalArgumentException("Cannot write that int as a short value : " + v);
		}
		logLength(relatedMetadata, Short.BYTES);
		dataOutputStream.writeShort(v);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Short.BYTES, "short", "" + v);
		}
	}


	/**
	 * Value ranging from 0 to 65534 are written on 2 bytes
	 * Value ranging from 65536 to 131070 are written on 4 byte
	 * Value ranging from 131072 to Max int are written on 8 byte
	 * etc
	 */
	public void writeCompactedPositiveIntFromByteArray_2_4_8(Metadata relatedMetadata, int value) throws IOException {
		if (value < 65535) {
			writeShort(relatedMetadata, (short) (value - Short.MAX_VALUE));

		} else {
			writeShort(relatedMetadata, Short.MIN_VALUE);

			if (value < 131070) {
				writeShort(relatedMetadata, (short) (value - 65535 - Short.MAX_VALUE));
			} else {
				writeShort(relatedMetadata, Short.MIN_VALUE);
				writeInt(relatedMetadata, value);
			}

		}
	}

	/**
	 * Value ranging from -127 to 127 are written on 1 bytes
	 * Value ranging from -254 to 254 are written on 2 bytes
	 * Value ranging from -32768 to 32767 are written on 4 bytes
	 * etc
	 */
	public void writeCompactedShortFromByteArray_1_2_4(Metadata relatedMetadata, short value) throws IOException {
		if (value >= -127 && value <= 127) {
			writeByte(relatedMetadata, (byte) value);

		} else {
			writeByte(relatedMetadata, Byte.MIN_VALUE);
			if (value >= -254 && value <= 254) {
				if (value < 0) {
					writeByte(relatedMetadata, (byte) (value + Byte.MAX_VALUE));
				} else {
					writeByte(relatedMetadata, (byte) (value - Byte.MAX_VALUE));
				}
			} else {
				writeByte(relatedMetadata, Byte.MIN_VALUE);
				writeShort(relatedMetadata, value);
			}

		}

	}


	public void writeChar(Metadata relatedMetadata, int v) throws IOException {
		logLength(relatedMetadata, Character.BYTES);
		dataOutputStream.writeChar(v);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Character.BYTES, "char", "" + v);
		}
	}

	public void writeInt(Metadata relatedMetadata, int v) throws IOException {
		logLength(relatedMetadata, Integer.BYTES);
		dataOutputStream.writeInt(v);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Integer.BYTES, "int", "" + v);
		}
	}

	public void writeLong(Metadata relatedMetadata, long v) throws IOException {
		logLength(relatedMetadata, Long.BYTES);
		dataOutputStream.writeLong(v);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Long.BYTES, "long", "" + v);
		}
	}

	public void writeFloat(Metadata relatedMetadata, float v) throws IOException {
		logLength(relatedMetadata, Float.BYTES);
		dataOutputStream.writeFloat(v);


		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Float.BYTES, "float", "" + v);
		}
	}

	public void writeDouble(Metadata relatedMetadata, double v) throws IOException {
		logLength(relatedMetadata, Double.BYTES);
		dataOutputStream.writeDouble(v);


		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, Double.BYTES, "double", "" + v);
		}
	}

	public void writeBytes(Metadata relatedMetadata, String s, boolean utf8) throws IOException {
		if (utf8) {
			byte[] bytes = s.getBytes(UTF_8);
			int size = bytes.length;

			logLength(relatedMetadata, size);
			dataOutputStream.write(bytes);

			if (Toggle.DEBUG_DTOS.isEnabled()) {
				logDebugInfo(relatedMetadata, size, "String as UTF-8 bytes", s);
			}
		} else {
			logLength(relatedMetadata, s.length() * Byte.BYTES);
			dataOutputStream.writeBytes(s);

			if (Toggle.DEBUG_DTOS.isEnabled()) {
				logDebugInfo(relatedMetadata, s.length() * Byte.BYTES, "String as bytes", s);
			}
		}
	}

	public void writeChars(Metadata relatedMetadata, String s) throws IOException {
		logLength(relatedMetadata, s.length() * Character.BYTES);
		dataOutputStream.writeChars(s);


		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, s.length() * Character.BYTES, "String as chars", s);
		}
	}

	public void writeUTF(Metadata relatedMetadata, String str) throws IOException {
		int sizeBefore = dataOutputStream.size();
		dataOutputStream.writeUTF(str);
		int sizeAfter = dataOutputStream.size();
		logLength(relatedMetadata, sizeAfter - sizeBefore);

		if (Toggle.DEBUG_DTOS.isEnabled()) {
			logDebugInfo(relatedMetadata, sizeAfter - sizeBefore, "String as UTF", str);
		}
	}

	public int size() {
		return dataOutputStream.size();
	}

	@Override
	public void close() throws IOException {
		byteArrayOutputStream.close();
		dataOutputStream.close();
	}

	public byte[] toByteArray() {
		try {
			flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	private void logLength(Metadata relatedMetadata, long length) {
		this.length += length;
		if (statsBuilder != null) {
			statsBuilder.log(relatedMetadata.getCollectionId(), relatedMetadata.getTypeId(), relatedMetadata.getId(), length, persisted);
		}
	}

	private void logDebugInfo(Metadata relatedMetadata, long valueLength, String type, String valuePresentation) {

		if (Toggle.DEBUG_DTOS.isEnabled()) {

			long start = this.length - valueLength;

			debugInfos.add(toDebugInfos(relatedMetadata, start, valueLength, type, valuePresentation));
		}

	}

	@NotNull
	static List<Object> toDebugInfos(Metadata relatedMetadata, long start, long valueLength, String type,
									 String valuePresentation) {
		List<Object> info = new ArrayList<>();
		info.add(start);
		info.add(valueLength);
		info.add(((relatedMetadata == null ? "" : relatedMetadata.getLocalCode()) + "                    ").substring(0, 20));
		info.add((type + "              ").substring(0, 15));
		info.add(valuePresentation);
		return info;
	}

	public List<List<Object>> getDebugInfosIncrementingOffSets(int bytesCount) {

		List<List<Object>> returnedInfos = new ArrayList<>();

		for (List<Object> info : debugInfos) {
			List<Object> newInfo = new ArrayList<>(info);
			long offset = (long) newInfo.get(0);
			newInfo.set(0, offset + bytesCount);
			returnedInfos.add(newInfo);
		}

		return returnedInfos;
	}
}
