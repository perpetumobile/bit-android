package com.perpetumobile.bit.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;

class FormPart {
	protected String partName = null;
	protected String value = null;
	
	public FormPart(String partName, String value) {
		this.partName = partName;
		this.value = value;
	}
}

class DataPart {
	protected String partName = null;
	protected String contentType = null;
	protected String fileName = null;
	protected byte[] data = null;

	public DataPart(String partName, String contentType, String fileName, byte[] data) {
		this.partName = partName;
		this.contentType = contentType;
		this.fileName = fileName;
		this.data = data;
	}
	
	public void writeContentType(OutputStream os) throws IOException {
		if(Util.nullOrEmptyString(contentType)) {
			os.write(MultipartEntity.contentTypeApplicationOctetStream);
		} else {
			os.write(MultipartEntity.contentTypeKey);
			os.write(contentType.getBytes());
		}
	}
}

public class MultipartEntity {

	static private Logger logger = new Logger(MultipartEntity.class);

	static final public byte[] crlf = "\r\n".getBytes();
	static final public byte[] quote = "\"".getBytes();
	
	static final public byte[] delimiter = "--".getBytes();
	
	static final public byte[] contentTypeKey = "Content-Type: ".getBytes();
	static final public byte[] contentTypeTextPlain = "Content-Type: text/plain".getBytes();
	static final public byte[] contentTypeApplicationOctetStream = "Content-Type: application/octet-stream".getBytes();
	
	static final public byte[] contentLengthKey = "Content-Length: ".getBytes();
	
	static final public byte[] contentTransferEncodingBinary = "Content-Transfer-Encoding: binary".getBytes();
	
	static final public byte[] contentDispositionFormData_nameKey = "Content-Disposition: form-data; name=\"".getBytes();
	
	static final public byte[] filenameKey = "; filename=\"".getBytes();
	
	static final public String boundary = "BITSimpleBoundaryyradnuoBelpmiSTIB";
	
	protected ArrayList<FormPart> formParts = new ArrayList<FormPart>();
	protected ArrayList<DataPart> dataParts = new ArrayList<DataPart>();
	
	public MultipartEntity() {
	}
	
	public void addFormPart(String partName, String value) {
		formParts.add(new FormPart(partName, value));
	}
	
	public void addFilePart(String partName, String contentType, File file) throws IOException {
		ByteBuffer data = Util.readBinaryFile(file);
		dataParts.add(new DataPart(partName, contentType, file.getName(), data.array()));
	}
	
	public void addDataPart(String partName, String contentType, String fileName, byte[] data) {
		dataParts.add(new DataPart(partName, contentType, fileName, data));
	}
	
	static public void prepareConnection(HttpURLConnection c) {
		try {
			c.setRequestMethod("POST");
			c.setDoInput(true);
			c.setDoOutput(true);
			// c.setRequestProperty("Connection", "Keep-Alive");
			c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		} catch (Exception e) {
			logger.error("MultiPartEntity.prepareConnection exception", e);
		}
	}
	
	public void writeFormPart(OutputStream os, FormPart part) throws IOException {
		os.write(delimiter);
		os.write(boundary.getBytes());
		os.write(crlf);
		os.write(contentTypeTextPlain);
		os.write(crlf);
		os.write(contentDispositionFormData_nameKey);
		os.write(part.partName.getBytes());
		os.write(quote);
		os.write(crlf);
		os.write(crlf);
		os.write(part.value.getBytes());
		os.write(crlf);
	}
	
	public void writeDataPart(OutputStream os, DataPart part) throws IOException {
		os.write(delimiter);
		os.write(boundary.getBytes());
		os.write(crlf);
		os.write(contentDispositionFormData_nameKey);
		os.write(part.partName.getBytes());
		os.write(quote);
		os.write(filenameKey);
		os.write(part.fileName.getBytes());
		os.write(quote);
		os.write(crlf);
		part.writeContentType(os);
		os.write(crlf);
		os.write(contentLengthKey);
		os.write(Integer.toString(part.data.length).getBytes());
		os.write(crlf);
		os.write(contentTransferEncodingBinary);
		os.write(crlf);
		os.write(crlf);
		os.write(part.data);
		os.write(crlf);
	}
	
	public void write(OutputStream os) throws IOException {
		for(FormPart fp : formParts) {
			writeFormPart(os, fp);
		}
		for(DataPart dp : dataParts) {
			writeDataPart(os, dp);
		}
		os.write(delimiter);
		os.write(boundary.getBytes());
		os.write(delimiter);
		os.write(crlf);
	}
	
}
