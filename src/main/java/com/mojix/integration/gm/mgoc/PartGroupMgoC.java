package com.mojix.integration.gm.mgoc;

import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item.Padding;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.pojo.Part;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mojix.integration.gm.mgoc.PartGroupMgoC.PartDGOFieldsMGOC.*;

public class PartGroupMgoC extends PartImportMGOImpl {

	private static Logger logger = LoggerFactory.getLogger(PartImportMGOImpl.class);
	
	public enum PartDGOFieldsMGOC implements IntegrationField {
		RECORD_SEQUENCE ( 1,  6, Padding.ZEROS_LEFT),
        PLANT_CODE      ( 7,  8, Padding.BLANK_LEFT),
        PART_NUMBER     ( 9, 16, Padding.BLANK_LEFT),
        PART_ECL        (17, 19, Padding.BLANK_LEFT),
        PART_VERSION    (20, 22, Padding.BLANK_LEFT),
        DEPARTMENT      (23, 26, Padding.BLANK_LEFT),
        GROUP           (27, 30, Padding.BLANK_LEFT),
        OPERATION       (31, 34, Padding.BLANK_LEFT),
        BUILD_SEQUENCE  (35, 39, Padding.BLANK_LEFT),
        INPT_TYPE  		(40, 40, Padding.BLANK_LEFT),
        TOTAL_COST      (41, 56, Padding.ZEROS_LEFT),
        FILLER			(57, 60, Padding.BLANK_LEFT);
		

		private int beginIndex;
		private int endIndex;
		private Padding padding;
		
		PartDGOFieldsMGOC(int beginIndex, int endIndex, Padding padding) {
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.padding = padding;
		}
		
		@Override
		public Padding getPadding() {
			return this.padding;
		}

		@Override
		public String getLabelName() {
			return name();
		}

		@Override
		public int getBeginIndex() {
			return this.beginIndex;
		}

		@Override
		public int getEndIndex() {
			return this.endIndex;
		}
		
	}
	
	@Override
	public String getFileTypeIdentifier() {
		return DGO_FILE_TYPE_IDENTIFIER;
	}

	@Override
	public List<IntegrationField> getHeaders() {
		return Arrays.asList(PartDGOFieldsMGOC.values());
	}

	@Override
	public List<Map<IntegrationField, String>> buildLines(String plantCode) {
		int buildSeqSeed;
		int lineCount = 0;
		Part partNumber;
		Map<IntegrationField, String> mapRecord;
		List<Part> partList = getPartNumbersPerPlant(plantCode);
		List<Map<IntegrationField, String>> listRecords = new ArrayList<>();
		BigDecimal partCost = getNextDoubleRandom(BigDecimal.valueOf(30), 5);

		for(int i=0;i<partList.size();i++) {
			partNumber = partList.get(i);

			buildSeqSeed = partNumber.getBuildSeqSeed();
			fixTotalBuildSequences(plantCode, partNumber, lineCount, this.getIntegrationParams().getTotalValidBillingCodes(plantCode));
			
			for(int j=0;j<partNumber.getTotalBuildSequences();j++) {
				mapRecord = new LinkedHashMap<>();
				appendNewRecord(mapRecord, buildSeqSeed, plantCode, partNumber, partCost, partNumber.getDgoList().get(j));
				listRecords.add(mapRecord);
				buildSeqSeed += 10;
				partCost = partCost.add(getNextDoubleRandom(BigDecimal.TEN, 5));
			}
			lineCount += partNumber.getTotalBuildSequences();
		}

		return listRecords;
	}

	private void fixTotalBuildSequences(String plantCode,
										Part part,
									    final int currentLineCount,
									    final int totalOperations) {
		int difference;
		
		if(part.getTotalBuildSequences() > totalOperations) {
			difference = this.getIntegrationParams().getPlantParams(plantCode).getPartAssignmentRecords() -
					currentLineCount;
			part.setTotalBuildSequences(
					Math.min(totalOperations, difference)
				);
		}
	}

	private void appendNewRecord(Map<IntegrationField, String> mapRecord,
								 final Integer buildSequence,
								 final String plantCode,
								 Part partNumber,
								 final BigDecimal partCost,
								 final String dgo) {
		String[] dgoSegments = separateDgoInSegments(dgo);

		mapRecord.put(RECORD_SEQUENCE, RECORD_SEQUENCE.getFormatedValue( super.getSequenceNumber() ));
		mapRecord.put(PLANT_CODE, PLANT_CODE.getFormatedValue(plantCode));
		mapRecord.put(PART_NUMBER, PART_NUMBER.getFormatedValue(
				partNumber.getPartNumberBase()
			));
		mapRecord.put(PART_ECL, PART_ECL.getFormatedValue(
				partNumber.getPartECL()
			));
		mapRecord.put(PART_VERSION, PART_VERSION.getFormatedValue(
				partNumber.getPartVersion()
			));
		mapRecord.put(DEPARTMENT, DEPARTMENT.getFormatedValue(
				dgoSegments[0]
			));
		mapRecord.put(GROUP, GROUP.getFormatedValue(
				dgoSegments[1]
			));
		mapRecord.put(OPERATION, OPERATION.getFormatedValue(
				dgoSegments[2]
			));
		if(buildSequence != null) {
			mapRecord.put(
					BUILD_SEQUENCE,
					BUILD_SEQUENCE.getFormatedValue(Integer.toString((buildSequence)))
			);
		} else {
			mapRecord.put(BUILD_SEQUENCE, StringUtils.leftPad(
						"",
						PartDGOFieldsMGOC.BUILD_SEQUENCE.getSize(),
						' '
					));
		}
		mapRecord.put(INPT_TYPE, INPT_TYPE.getFormatedValue(""));
		mapRecord.put(TOTAL_COST, TOTAL_COST.getFormatedValue(partCost.toString()));
		mapRecord.put(FILLER, FILLER.getFormatedValue(""));
	}

	@Override
	public String getHeader() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_C_DATE_FORMAT);
		
		return String.format(
				"1DGO PART HEADER               %s000001000000010",
				dateFormat.format(new Date())
			);
	}

	@Override
	public String getTrailer() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_C_DATE_FORMAT);
		
		return String.format(
				"3DGO PART TRAILER              %s000001000000010",
				dateFormat.format(new Date())
			);
	}
	
}
