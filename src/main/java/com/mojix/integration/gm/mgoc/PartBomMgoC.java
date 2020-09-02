package com.mojix.integration.gm.mgoc;

import com.mojix.integration.gm.IntegrationField;
import com.mojix.integration.gm.Item.Padding;
import com.mojix.integration.gm.PartImportMGOImpl;
import com.mojix.pojo.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import static com.mojix.integration.gm.mgoc.PartBomMgoC.PartBomFieldsMGOC.*;

public class PartBomMgoC extends PartImportMGOImpl {

	private static final Integer MAX_BOM_QUANTITY = 15;
	private static final Integer MIN_BOM_QUANTITY = 2;

	private static final Logger logger = LoggerFactory.getLogger(PartBomMgoC.class);
	
	public enum PartBomFieldsMGOC implements IntegrationField {
		RECORD_SEQUENCE 	( 1,  6, Padding.ZEROS_LEFT),
		PLANT_CODE 			( 7,  8, Padding.BLANK_LEFT),
		PARENT_PART_NUMBER 	( 9, 16, Padding.BLANK_LEFT),
		PARENT_PART_ECL 	(17, 19, Padding.BLANK_LEFT),
		PARENT_PART_VERSION (20, 22, Padding.BLANK_LEFT),
		CONSUMING_BS 		(23, 27, Padding.BLANK_LEFT),
		PARENT_DEPARTMENT   (28, 31, Padding.BLANK_LEFT),
		PARENT_GROUP        (32, 35, Padding.BLANK_LEFT),
		PARENT_OPERATION    (36, 39, Padding.BLANK_LEFT),
		CHILD_PART_NUMBER 	(40, 47, Padding.BLANK_LEFT),
		CHILD_PART_ECL 		(48, 50, Padding.BLANK_LEFT),
		CHILD_PART_VERSION 	(51, 53, Padding.BLANK_LEFT),
		SOURCE_BS 			(54, 58, Padding.BLANK_LEFT),
		CHILD_DEPARTMENT    (59, 62, Padding.BLANK_LEFT),
		CHILD_GROUP         (63, 66, Padding.BLANK_LEFT),
		CHILD_OPERATION     (67, 70, Padding.BLANK_LEFT),
		USAGE_QUANTITY 		(71, 82, Padding.ZEROS_LEFT),
		FILLER				(83, 84, Padding.BLANK_LEFT);

		private int beginIndex;
		private int endIndex;
		private Padding padding;

		PartBomFieldsMGOC(int beginIndex, int endIndex, Padding padding) {
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.padding = padding;
		}

		@Override
		public int getBeginIndex() {
			return this.beginIndex;
		}

		@Override
		public int getEndIndex() {
			return this.endIndex;
		}

		@Override
		public Padding getPadding() {
			return this.padding;
		}

		@Override
		public String getLabelName() {
			return name();
		}
	}
	
	@Override
	public String getFileTypeIdentifier() {
		return BOM_FILE_TYPE_IDENTIFIER;
	}

	@Override
	public List<IntegrationField> getHeaders() {
		return Arrays.asList(PartBomFieldsMGOC.values());
	}

	@Override
	public List<Map<IntegrationField, String>> buildLines(String plantCode) {
		int parentIndex;
		Integer sourceBs;
		Integer consumingBSSeed;
		Integer partBomQuantity;
		Part childPart;
		Part parentPart;
		String[] childDgoSegments;
		String[] parentDgoSegments;
		Map<IntegrationField, String> mapRecord;
		List<Map<IntegrationField, String>> listRecords = new ArrayList<>();
		Map<Part, List<Part>> partBomParts = this.getParentChildrenRelation(plantCode);

		logger.info("Start generating Part Boms for MGO-C");
		for(Entry<Part, List<Part>> entry : partBomParts.entrySet()) {
			parentIndex = 0;
			parentPart = entry.getKey();
			consumingBSSeed = getNextIntRandom(99, 1) * 100;
			for(Iterator<Part> child = entry.getValue().iterator();child.hasNext();) {
				childPart = child.next();
				parentDgoSegments = separateDgoInSegments(parentPart.getDgoList().get(parentIndex++));
				childDgoSegments = getDGOSegmentsRandom(childPart);
				partBomQuantity = super.getNextIntRandom(MAX_BOM_QUANTITY, MIN_BOM_QUANTITY);
				sourceBs = super.getNextIntRandom(childPart.getTotalBuildSequences(), 1);

				mapRecord = new LinkedHashMap<>();
				mapRecord.put(RECORD_SEQUENCE, RECORD_SEQUENCE.getFormatedValue( super.getSequenceNumber() ));
				mapRecord.put(PLANT_CODE, PLANT_CODE.getFormatedValue(plantCode));
				mapRecord.put(PARENT_PART_NUMBER, PARENT_PART_NUMBER.getFormatedValue(
						parentPart.getPartNumberBase()
					));
				mapRecord.put(PARENT_PART_ECL, PARENT_PART_ECL.getFormatedValue(
						parentPart.getPartECL()
					));
				mapRecord.put(PARENT_PART_VERSION, PARENT_PART_VERSION.getFormatedValue(
						parentPart.getPartVersion()
					));

                mapRecord.put(CONSUMING_BS, CONSUMING_BS.getFormatedValue(consumingBSSeed.toString()));

				mapRecord.put(PARENT_DEPARTMENT, PARENT_DEPARTMENT.getFormatedValue(
						parentDgoSegments[0]
					));
				mapRecord.put(PARENT_GROUP, PARENT_GROUP.getFormatedValue(
						parentDgoSegments[1]
				));
				mapRecord.put(PARENT_OPERATION, PARENT_OPERATION.getFormatedValue(
						parentDgoSegments[2]
				));

				mapRecord.put(CHILD_PART_NUMBER, CHILD_PART_NUMBER.getFormatedValue(
						childPart.getPartNumberBase()
					));
				mapRecord.put(CHILD_PART_ECL, CHILD_PART_ECL.getFormatedValue(
						childPart.getPartECL()
					));
				mapRecord.put(CHILD_PART_VERSION, CHILD_PART_VERSION.getFormatedValue(
						childPart.getPartVersion()
					));

				mapRecord.put(CHILD_DEPARTMENT, CHILD_DEPARTMENT.getFormatedValue(
						childDgoSegments[0]
				));
				mapRecord.put(CHILD_GROUP, CHILD_GROUP.getFormatedValue(
						childDgoSegments[1]
				));
				mapRecord.put(CHILD_OPERATION, CHILD_OPERATION.getFormatedValue(
						childDgoSegments[2]
				));

				mapRecord.put(SOURCE_BS, SOURCE_BS.getFormatedValue(sourceBs.toString()));
				mapRecord.put(USAGE_QUANTITY, USAGE_QUANTITY.getFormatedValue(partBomQuantity.toString()));
				mapRecord.put(FILLER, FILLER.getFormatedValue(""));

				consumingBSSeed += 10;

				listRecords.add(mapRecord);
			}
		}
		logger.info("Part Boms for MGO-C generated correctly");
		
		return listRecords;
	}

	private String[] getDGOSegmentsRandom(Part part) {
		int index = super.getNextIntRandom(part.getTotalBuildSequences() - 1, 0);

		return separateDgoInSegments(part.getDgoList().get(index));
	}

	protected Map<Part, List<Part>> getParentChildrenRelation(String plantCode) {
		Part parentPart;
		int lineCount = 0;
		int totalChildrenParts;
		List<Part> childrenParts;
		Map<Part, List<Part>> parentChildren = new HashMap<>();
		List<Part> allParts = this.getPartNumbersPerPlant(plantCode);

		while(allParts != null && !allParts.isEmpty() && lineCount < this.getIntegrationParams().getPlantParams(plantCode).getBomPartRecords()) {
			parentPart = allParts.get(0);
			totalChildrenParts = Math.min(parentPart.getDgoList().size(), allParts.size() - 1);
			childrenParts = allParts.subList(1, totalChildrenParts + 1);

			parentChildren.put(parentPart, childrenParts);

			allParts = allParts.subList(totalChildrenParts + 1, allParts.size());

			lineCount += childrenParts.size();
		}

		return parentChildren;
	}

	@Override
	public String getHeader() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_C_DATE_FORMAT);
		
		return String.format(
				"1PART BOM HEADER               %s000001000000010",
				dateFormat.format(new Date())
			);
	}

	@Override
	public String getTrailer() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MGO_C_DATE_FORMAT);
		
		return String.format(
				"3PART BOM TRAILER              %s000001000000010",
				dateFormat.format(new Date())
			);
	}
		
}
