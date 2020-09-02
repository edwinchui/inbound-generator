package com.mojix.integration.gm.mgoa;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.csv.CSVRecord;

import com.mojix.dao.CsvFilesReader;
import com.mojix.dao.CsvFilesReader.SampleDataFiles;
import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item.Padding;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.pojo.Part;

import static com.mojix.integration.gm.mgoa.DiscretePartMgoA.DiscreteFieldsMGOA.*;

public class DiscretePartMgoA extends PartImportMGOImpl {

	public enum DiscreteFieldsMGOA implements IntegrationField {
		SEQUENCE_NUMBER		( 0,   5, Padding.ZEROS_LEFT),
		PLANT_CODE			( 6,   7, Padding.BLANK_RIGHT),
		FILLER_01			( 8,   9, Padding.BLANK_LEFT),
		PART_NUMBER			(10,  17, Padding.ZEROS_LEFT),
		FILLER_02			(18,  19, Padding.BLANK_LEFT),
		PART_STATUS_CODE	(20,  20, Padding.BLANK_RIGHT),
		FILLER_03			(21,  22, Padding.BLANK_LEFT),
		PART_NAME			(23,  37, Padding.BLANK_RIGHT),
		FILLER_04			(38,  39, Padding.BLANK_LEFT),
		PART_COST			(40,  55, Padding.ZEROS_LEFT),
		FILLER_05			(56,  56, Padding.BLANK_LEFT),
		PRIMARY_SUPPLIER	(57,  57, Padding.BLANK_RIGHT),
		SUPPLIER_CODE		(58,  66, Padding.ZEROS_LEFT),
		SUPPLIER_NAME		(67, 101, Padding.BLANK_RIGHT);
		
		private int beginIndex;
		private int endIndex;
		private Padding padding;
		
		DiscreteFieldsMGOA(int beginIndex, int endIndex, Padding padding) {
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
	
	public String getFileTypeIdentifier() {
		return DISCRETE_PART_FILE_TYPE_IDENTIFIER;
	}

	@Override
	public List<IntegrationField> getHeaders() {
		return Arrays.asList(DiscreteFieldsMGOA.values());
	}

	public List<Map<IntegrationField, String>> buildLines(String plantCode) {
		CSVRecord supplier;
		CSVRecord partDetail;
		String partNumber;
		Map<IntegrationField, String> mapRecord;
		List<Map<IntegrationField, String>> listRecords = new ArrayList<>();
		ArrayList<Part> partNumbersByPlant = new ArrayList<>();
		
		for(int i=0;i<this.getIntegrationParams().getPlantParams(plantCode).getDiscretePartRecords();i++) {
			supplier = CsvFilesReader.getInstance().getRandomRecord(SampleDataFiles.SUPPLIERS_CSV_FILE);
			partDetail = CsvFilesReader.getInstance().getRandomRecord(SampleDataFiles.PART_NAMES_CSV_FILE);
			partNumber = this.getPartNumber(plantCode);
			partNumbersByPlant.add( new Part(partNumber) );

			mapRecord = new HashMap<>();
			mapRecord.put(SEQUENCE_NUMBER, SEQUENCE_NUMBER.getFormatedValue(super.getSequenceNumber()));
			mapRecord.put(PLANT_CODE, PLANT_CODE.getFormatedValue(plantCode));
			mapRecord.put(FILLER_01, FILLER_01.getFormatedValue(""));
			mapRecord.put(PART_NUMBER, PART_NUMBER.getFormatedValue(partNumber));
			mapRecord.put(FILLER_02, FILLER_02.getFormatedValue(""));
			mapRecord.put(PART_STATUS_CODE, PART_STATUS_CODE.getFormatedValue("C"));
			mapRecord.put(FILLER_03, FILLER_03.getFormatedValue(""));
			mapRecord.put(PART_NAME, PART_NAME.getFormatedValue(partDetail.get("name")));
			mapRecord.put(FILLER_04, FILLER_04.getFormatedValue(""));
			mapRecord.put(PART_COST, PART_COST.getFormatedValue(partDetail.get("cost")));
			mapRecord.put(FILLER_05, FILLER_05.getFormatedValue(""));
			mapRecord.put(PRIMARY_SUPPLIER, PRIMARY_SUPPLIER.getFormatedValue("P"));
			mapRecord.put(SUPPLIER_CODE, SUPPLIER_CODE.getFormatedValue(supplier.get("code")));
			mapRecord.put(SUPPLIER_NAME, SUPPLIER_NAME.getFormatedValue(supplier.get("name")));
			listRecords.add(mapRecord);
		}
		this.getIntegrationParams().addPartNumbersPerPlant(plantCode, partNumbersByPlant);
		
		return listRecords;
	}
	
	private String getPartNumber(String plantCode) {
		int maxPartNumberValue = super.getMaxIntValueByLength(DiscreteFieldsMGOA.PART_NUMBER.getSize());
		
		return Integer.toString(super.getNextIntRandom(maxPartNumberValue, 100));
	}
	
	@Override
	public String getHeader() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_A_DATE_FORMAT);
		
		return String.format(
				"HEADER  %s",
				dateFormat.format(new Date())
			);
	}

	@Override
	public String getTrailer() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_A_DATE_FORMAT);
		
		return String.format(
				"TRAILER  %s",
				dateFormat.format(new Date())
			);
	}
	
}