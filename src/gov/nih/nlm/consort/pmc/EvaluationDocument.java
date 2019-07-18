package gov.nih.nlm.consort.pmc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import gov.nih.nlm.ling.util.FileUtils;

public class EvaluationDocument {
	private static Logger log = Logger.getLogger(EvaluationDocument.class.getName());	
	
	public static Map<String, String> label_index_map;
	public static Map<String, String> label_consort_item_id_map;
		
	public static void initMap() {
		label_index_map = new HashMap<String, String>();
		label_index_map.put("1", "Title (Randomized)");
		label_index_map.put("2", "Structured Abstract");
		label_index_map.put("4", "Background");
		label_index_map.put("5", "Objective");
		label_index_map.put("7", "Trial Design");
		label_index_map.put("8", "Changes TO Trial Design");
		label_index_map.put("9", "Eligibility Criteria");
		label_index_map.put("10", "Data Collection Setting");
		label_index_map.put("11", "Interventions");
		label_index_map.put("12", "Outcomes");
		label_index_map.put("13", "Changes to Outcomes");
		label_index_map.put("14", "Sample Size Determination");
		label_index_map.put("15", "Interim Analyses/Stopping Guidelines");
		label_index_map.put("16", "Random Allocation Sequence Generation");
		label_index_map.put("17", "Randomization Type");
		label_index_map.put("18", "Allocation Concealment Mechanism");
		label_index_map.put("19", "Randomization Implementation");
		label_index_map.put("20", "Blinding Procedure");
		label_index_map.put("21", "Similarity of Interventions");
		label_index_map.put("22", "Statistical Methods for Outcome Comparison");
		label_index_map.put("23", "Statistical Methods for Other Analyses");
		label_index_map.put("25", "Participant Flow");
		label_index_map.put("26", "Participant Loss and Exclusion");
		label_index_map.put("27", "Recruitment Period/Follow-Up");
		label_index_map.put("28", "Trial Stopping");
		label_index_map.put("29", "Baseline Data");
		label_index_map.put("30", "Numbers Analyzed");
		label_index_map.put("31", "Outcome Results");
		label_index_map.put("32", "Binary Outcome Results");
		label_index_map.put("33", "Ancillary Analyses");
		label_index_map.put("34", "Harms");
		label_index_map.put("36", "Limitations");
		label_index_map.put("37", "Generalizability");
		label_index_map.put("38", "Interpretation");
		label_index_map.put("40", "Registry/Number");
		label_index_map.put("41", "Protocol Access");
		label_index_map.put("42", "Funding");
		
		label_consort_item_id_map = new HashMap<String, String>();
		label_consort_item_id_map.put("1", "1a");
		label_consort_item_id_map.put("2", "1b");
		label_consort_item_id_map.put("4", "2a");
		label_consort_item_id_map.put("5", "2b");
		label_consort_item_id_map.put("7", "3a");
		label_consort_item_id_map.put("8", "3b");
		label_consort_item_id_map.put("9", "4a");
		label_consort_item_id_map.put("10", "4b");
		label_consort_item_id_map.put("11", "5");
		label_consort_item_id_map.put("12", "6a");
		label_consort_item_id_map.put("13", "6b");
		label_consort_item_id_map.put("14", "7a");
		label_consort_item_id_map.put("15", "7b");
		label_consort_item_id_map.put("16", "8a");
		label_consort_item_id_map.put("17", "8b");
		label_consort_item_id_map.put("18", "9");
		label_consort_item_id_map.put("19", "10");
		label_consort_item_id_map.put("20", "11a");
		label_consort_item_id_map.put("21", "11b");
		label_consort_item_id_map.put("22", "12a");
		label_consort_item_id_map.put("23", "12b");
		label_consort_item_id_map.put("25", "13a");
		label_consort_item_id_map.put("26", "13b");
		label_consort_item_id_map.put("27", "14a");
		label_consort_item_id_map.put("28", "14b");
		label_consort_item_id_map.put("29", "15");
		label_consort_item_id_map.put("30", "16");
		label_consort_item_id_map.put("31", "17a");
		label_consort_item_id_map.put("32", "17b");
		label_consort_item_id_map.put("33", "18");
		label_consort_item_id_map.put("34", "19");
		label_consort_item_id_map.put("36", "20");
		label_consort_item_id_map.put("37", "21");
		label_consort_item_id_map.put("38", "22");
		label_consort_item_id_map.put("40", "23");
		label_consort_item_id_map.put("41", "24");
		label_consort_item_id_map.put("42", "25");
	}
	
	public static String[] convertToText(String se) {
		List<String> re = new ArrayList<String>();
		for(String s : se.split(" ")) {
			re.add(label_index_map.get(s));
		}
		return re.toArray(new String[re.size()]);
	}
	
	public static String[] convertToValues(String se) {
		List<String> re = new ArrayList<String>();
		for(String s : se.split(" ")) {
			re.add(label_consort_item_id_map.get(s));
		}
		return re.toArray(new String[re.size()]);
	}
	
	private static List<String> getLabels(String file) throws IOException  {
		List<String> lines = FileUtils.linesFromFile(file, "UTF-8");
		List<String> labels = new ArrayList<>();
		for (String l: lines) {
			String[] els = l.split("\\|");
			labels.add(els[0] + "|" + els[1]);
		}
		return labels;
	}
	
	public static ArrayList<String> keepItemOnly (String id, ArrayList<String> c1) {
		ArrayList<String> out = new ArrayList<>();
		for (String c: c1) {
				Set<String> s1 = new HashSet<String>(Arrays.asList(c.split(",")));
				if (s1.contains(id)) out.add("1");
				else out.add("0");
		}
		return out;
	}
	
	public static double hammingLoss(ArrayList<String> c1, ArrayList<String> c2) {
		double loss = 0.0;
		for (int i=0; i < c1.size(); i++) {
			String c = c1.get(i);
			String g = c2.get(i);
			Set<String> s1 = new HashSet<String>(Arrays.asList(c.split(",")));
			Set<String> s2 = new HashSet<String>(Arrays.asList(g.split(",")));
			Set<String> all = new HashSet<String>();
			all.addAll(s1);
			all.addAll(s2);
			int total = all.size();
			s1.retainAll(s2);
			all.removeAll(s1);
			loss += all.size() / total;
//			loss += CalcMASI.calcJ(s1, s2);
		}
		return (double) loss/c1.size();
	}

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

		String predict = args[0];
		String gold = args[1];
		
		List<String> predictions = getLabels(predict);
		List<String> golds = getLabels(gold);
		List<String> TPs = new ArrayList<>();
		List<String> FPs = new ArrayList<>();
		List<String> FNs = new ArrayList<>();
		for (String p: predictions) {
			if (golds.contains(p)) TPs.add(p);
			else FPs.add(p);
		}
		for (String g: golds) {
			if (TPs.contains(g) == false) FNs.add(g);
		}
			
		int TP =TPs.size();
		int FP = FPs.size();
		int FN = FNs.size();
		
		double precision = (double) TP / (double) (TP + FP);
		double recall = (double) TP / (double) (TP + FN);
		double f1 = (double) (2 * precision * recall) / (double) (precision + recall);
		
		System.out.println(precision + "|" + recall + "|" + f1);
		
	}
}
