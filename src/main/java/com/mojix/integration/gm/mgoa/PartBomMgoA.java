package com.mojix.integration.gm.mgoa;

import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item.Padding;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.pojo.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import static com.mojix.integration.gm.mgoa.PartBomMgoA.PartBomFieldsMGOA.*;

public class PartBomMgoA extends PartImportMGOImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(PartBomMgoA.class);
	
	public enum PartBomFieldsMGOA implements IntegrationField {
		SEQUENCE_NUMBER 	( 0,  5, Padding.ZEROS_LEFT),
		PLANT_CODE 			( 6,  7, Padding.BLANK_RIGHT),
		FILLER_01			( 8,  9, Padding.BLANK_LEFT),
		PART_NUMBER_PARENT	(10, 17, Padding.ZEROS_LEFT),
		FILLER_02			(18, 19, Padding.BLANK_LEFT),
		PART_NUMBER_CHILD 	(20, 27, Padding.ZEROS_LEFT),
		FILLER_03			(28, 29, Padding.BLANK_LEFT),
		QUANTITY 			(30, 34, Padding.BLANK_LEFT);
		
		private int beginIndex;
		private int endIndex;
		private Padding padding;

		private PartBomFieldsMGOA(int beginIndex, int endIndex, Padding padding) {
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
	
	public List<Map<IntegrationField, String>> buildLines(String plantCode) {
		Map<IntegrationField, String> mapRecord;
		List<Map<IntegrationField, String>> listRecords = new ArrayList<>();
    	Map<Part, List<Part>> mapPartBom = this.getParentChildrenRelation(plantCode);
    	
    	for(Entry<Part, List<Part>> entry : mapPartBom.entrySet()) {
    		for(Iterator<Part> iterator=entry.getValue().iterator();iterator.hasNext();) {
				mapRecord = new LinkedHashMap<>();

		    	mapRecord.put(SEQUENCE_NUMBER, SEQUENCE_NUMBER.getFormatedValue(super.getSequenceNumber()));
		    	mapRecord.put(PLANT_CODE, PLANT_CODE.getFormatedValue(plantCode));
		    	mapRecord.put(FILLER_01, FILLER_01.getFormatedValue(""));
		    	mapRecord.put(PART_NUMBER_PARENT, PART_NUMBER_PARENT.getFormatedValue(entry.getKey().getPartNumberBase()));
		    	mapRecord.put(FILLER_02, FILLER_02.getFormatedValue(""));
		    	mapRecord.put(PART_NUMBER_CHILD, PART_NUMBER_CHILD.getFormatedValue(iterator.next().getPartNumberBase()));
		    	mapRecord.put(FILLER_03, FILLER_03.getFormatedValue(""));
		    	mapRecord.put(QUANTITY, QUANTITY.getFormatedValue(this.getQuantity()));

		    	listRecords.add(mapRecord);
    		}
    	}
    	
    	return listRecords;
    }
	
	/*private HashMap<String, Collection<String>> getPartBomRelation(String plantCode) {
		int numberParents;
		int numberChildren;
		int index;
		int power = 1;
		Collection<String> childrenPartNumbers;
		List<String> partNumbers = new ArrayList<>( this.getIntegrationParams().getPartNumbersPerPlant().get(plantCode) );
		HashMap<String, Collection<String>> partBoms = new HashMap<String, Collection<String>>();
		
		while((partNumbers.size()/Math.pow(10, power)) > 10) {
			power++;
		}
		
		numberParents = (int) (partNumbers.size() / Math.pow(5, power));
		numberChildren = (int) (numberParents / Math.pow(2, power));
		
		if(numberParents * numberChildren == partNumbers.size()) {
			numberChildren -= 2;
		}
		
		if(numberParents != 0 && numberChildren != 0) {
			index = 0;
			while(partBoms.size() < numberParents) {
				childrenPartNumbers = new ArrayList<String>();
				while(childrenPartNumbers.size() < numberChildren) {
					childrenPartNumbers.add(partNumbers.get(index));
					index++;
				}
				partBoms.put(partNumbers.get(index), childrenPartNumbers);
				index++;
			}
		}
		
		return partBoms;
	}*/
	
    private String getQuantity() {
    	return Integer.toString(super.getNextIntRandom(50, 1));
    }

	@Override
	public String getFileTypeIdentifier() {
		return BOM_FILE_TYPE_IDENTIFIER;
	}

	@Override
	public List<IntegrationField> getHeaders() {
		return Arrays.asList(PartBomFieldsMGOA.values());
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
