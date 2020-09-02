package com.mojix.pojo;

import java.util.List;

public class Part {
	
	public static final String INTERNAL_PART_NUMBER_SEPARATOR = "@";
	
	private String partNumberBase;
	private String partECL;
	private String partVersion;
	private String fullPartNumberInternal;
	private int totalBuildSequences;
	private int buildSeqSeed;
	private List<String> dgoList;

	public Part(String partNumberBase) {
		this(partNumberBase, "", "", 0, null);
	}
		
	public Part(String partNumberBase, String partECL, String partVersion, String billingCode) {
		this(partNumberBase, partECL, partVersion, 0, null);
	}

	public Part(String partNumberBase,
				String partECL,
				String partVersion,
				int buildSeqSeed,
				List<String> dgoList) {
		this.partNumberBase = partNumberBase;
		this.partECL = partECL;
		this.partVersion = partVersion;
		this.totalBuildSequences = dgoList != null ? dgoList.size() : 0;
		this.buildSeqSeed = buildSeqSeed;
		this.dgoList = dgoList;
		this.fullPartNumberInternal = getFullPartNumberInternal(partNumberBase, partECL, partVersion);
	}

	public String getPartNumberBase() {
		return partNumberBase;
	}

	public String getPartECL() {
		return partECL;
	}

	public String getPartVersion() {
		return partVersion;
	}

	public int getTotalBuildSequences() {
		return totalBuildSequences;
	}

	public int getBuildSeqSeed() {
		return buildSeqSeed;
	}

	public void setTotalBuildSequences(int totalBuildSequences) {
		this.totalBuildSequences = totalBuildSequences;
	}
	
	public boolean hasBuildSequence() {
		return totalBuildSequences > 1;
	}

	public List<String> getDgoList() {
		return dgoList;
	}

	private String getFullPartNumberInternal(String partNumberBase, String partECL, String partVersion) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(partNumberBase);
		builder.append(INTERNAL_PART_NUMBER_SEPARATOR);
		builder.append(partECL);
		builder.append(INTERNAL_PART_NUMBER_SEPARATOR);
		builder.append(partVersion);
		builder.append(INTERNAL_PART_NUMBER_SEPARATOR);
		
		return builder.toString();
	}

}
