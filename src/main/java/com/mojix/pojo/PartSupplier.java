package com.mojix.pojo;

public class PartSupplier {
	private String code;
	private String name;
	private String primarySupplier;
	
	public PartSupplier(String code, String name, String primarySupplier) {
		this.code = code;
		this.name = name;
		this.primarySupplier = primarySupplier;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrimarySupplier() {
		return primarySupplier;
	}

	public void setPrimarySupplier(String primarySupplier) {
		this.primarySupplier = primarySupplier;
	}
	
}
