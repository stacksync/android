package com.stacksync.android.cache;

public class CachedFile {

	private String id;
	private String name;
	private Long version;
	private Long size;
	private String lastModified;
	private String metadata;
	private String localPath;

	public CachedFile(String id, String name, Long version, Long size, String lastModified, String metadata, String localPath) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.size = size;
		this.lastModified = lastModified;
		this.metadata = metadata;
		this.localPath = localPath;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getVersion() {
		return version;
	}

	public Long getSize() {
		return size;
	}

	public String getLastModified() {
		return lastModified;
	}
	
	public String getMetadata() {
		return metadata;
	}
	
	public String getLocalPath() {
		return localPath;
	}
	
}
