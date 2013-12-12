package com.stacksync.android.api;

import org.json.JSONObject;

public class ListResponse extends GenericResponse {

	private String fileId;
	private JSONObject metadata;

	public ListResponse () {
		super(false, 0, "");

		this.metadata = new JSONObject();
	}
	
	public ListResponse (boolean succed, int statusCode, String message, String fileId, JSONObject metadata) {
		super(succed, statusCode, message);

		this.fileId = fileId;
		this.metadata = metadata;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public JSONObject getMetadata() {
		return metadata;
	}
	
	public void setMetadata(JSONObject metadata) {
		this.metadata = metadata;
	}

}
