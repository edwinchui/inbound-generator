package com.mojix.integration;

public class PlantParams {

	private String code;
	private int discretePartRecords;
	private int bomPartRecords;
	private int partAssignmentRecords;
	
	public PlantParams(String code, int discretePartRecords, int bomPartRecords, int partAssignmentRecords) {
		super();
		this.code = code;
		this.discretePartRecords = discretePartRecords;
		this.bomPartRecords = bomPartRecords;
		this.partAssignmentRecords = partAssignmentRecords;
	}

	public String getCode() {
		return code;
	}

	public int getDiscretePartRecords() {
		return discretePartRecords;
	}

	public int getBomPartRecords() {
		return bomPartRecords;
	}

	public int getPartAssignmentRecords() {
		return partAssignmentRecords;
	}
	
	
}
