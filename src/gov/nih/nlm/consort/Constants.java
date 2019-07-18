package gov.nih.nlm.consort;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import gov.nih.nlm.ling.util.FileUtils;

public class Constants {
	
	public static Map<String,String> CONSORT_ID_ITEMS = new HashMap<>();
	public static Map<String,String> CONSORT_ITEM_IDS = new HashMap<>();
	
	public static List<String> ITEM_IDS= Arrays.asList("3a","3b","4a","4b","5","6a","6b","7a","7b","8a","8b","9","10","11a","11b","12a","12b");
	public static List<String> METHOD_ITEM_IDS = Arrays.asList("3a","3b","4a","4b","5","6a","6b","7a","7b","8a","8b","9","10","11a","11b","12a","12b");
	
	public static Pattern ID_PATTERN = Pattern.compile("^([0-9]+)([a-b]?)");

	public static List<String> POTENTIAL_METHOD_TITLES = Arrays.asList("design","method","material","experimental","statistic","outcome","data collection","intervention","procedure","conduct","random","measure",
			"subject","cohort","participant","patient","implementation","endpoint","population","sample size","treatment");
	
	
	public static void loadCONSORTItems(String filename) throws IOException {
		final List<String> lines = FileUtils.linesFromFile(filename, "UTF8");
		for (String l: lines) {
			String[] els = l.split("\\|");
			String id = els[0];
			String name = els[1];
			CONSORT_ID_ITEMS.put(id, name);
			CONSORT_ITEM_IDS.put(name,id);
		}
	}
}
