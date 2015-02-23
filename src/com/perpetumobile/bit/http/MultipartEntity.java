package com.perpetumobile.bit.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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
	protected String fileName = null;
	protected int contentLength = 0;
	protected String contentType = null;
	protected InputStream contentStream = null;

	public DataPart(String partName, String fileName, int contentLength, String contentType, InputStream contentStream) {
		this.partName = partName;
		this.fileName = fileName;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.contentStream = contentStream;
	}
	
	public DataPart(String partName, String contentType, File file) throws IOException {
		this.partName = partName;
		this.fileName = file.getName();
		this.contentLength = (int)file.length();
		this.contentType = contentType;
		this.contentStream = new FileInputStream(file);
	}
	
	public void writeFileName(OutputStream os) throws IOException {
		if(!Util.nullOrEmptyString(fileName)) {
			os.write(MultipartEntity.filenameKey);
			os.write(fileName.getBytes());
			os.write(MultipartEntity.quote);
		}
	}
	
	public void writeContentLength(OutputStream os) throws IOException {
		if(contentLength > 0) {
			os.write(MultipartEntity.crlf);
			os.write(MultipartEntity.contentLengthKey);
			os.write(Integer.toString(contentLength).getBytes());
		}
	}
	
	public void writeContentType(OutputStream os) throws IOException {
		os.write(MultipartEntity.crlf);
		if(Util.nullOrEmptyString(contentType)) {
			os.write(MultipartEntity.contentTypeApplicationOctetStream);
		} else {
			os.write(MultipartEntity.contentTypeKey);
			os.write(contentType.getBytes());
		}
	}
	
	public void writeContent(OutputStream os) throws IOException {
		os.write(MultipartEntity.crlf);
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = contentStream.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		contentStream.close();
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
	
	public void addDataPart(String partName, InputStream contentStream) {
		dataParts.add(new DataPart(partName, null, 0, null, contentStream));
	}
	
	public void addDataPart(String partName, String contentType, InputStream contentStream) {
		dataParts.add(new DataPart(partName, null, 0, contentType, contentStream));
	}
	
	public void addDataPart(String partName, String fileName, int contentLength, String contentType, InputStream contentStream) {
		dataParts.add(new DataPart(partName, fileName, contentLength, contentType, contentStream));
	}
	
	public void addDataPart(String partName, String contentType, File file) throws IOException {
		dataParts.add(new DataPart(partName, contentType, file));
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
		part.writeFileName(os);
		part.writeContentType(os);
		part.writeContentLength(os);
		os.write(crlf);
		os.write(contentTransferEncodingBinary);
		os.write(crlf);
		part.writeContent(os);
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
