package gov.nih.nlm.consort.pmc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PMCSentenceExtraction {
	private static Logger log = Logger.getLogger(PMCSentenceExtraction.class.getName());	
	
	static Map<String, String> label_index_map;
	static Map<String, String> label_consort_item_id_map;
	
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

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		initMap();
		String dir = args[0]; // the path to the directory which contains all the reconciled files
		String out = args[1]; // the path to the output file which will contain all sentences and labels information
		String id_out = args[2]; // the path to output file which will contain reconciled files IDs
		BufferedWriter bw = new BufferedWriter(new FileWriter(out));
		BufferedWriter bw_id = new BufferedWriter(new FileWriter(id_out));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		factory.setFeature("http://xml.org/sax/features/namespaces", false);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		DocumentBuilder dBuilder = factory.newDocumentBuilder();
		String docID = null;
		int labeled = 0, unlabeled = 0;
		for (File f: new File(dir).listFiles()) {
			if (f.getName().endsWith(".xml") == false) continue;
			Document doc = dBuilder.parse(f);
			NodeList nl2 = doc.getElementsByTagName("section");
			Element el;
			String title;
			int offsetLow = 0, offsetHigh = 0, j = 0;
			while(offsetLow == 0 || offsetHigh == 0) {
				el = (Element) nl2.item(j);
				title = el.getAttribute("title").toLowerCase();
				if(title.contains("method")) {
					String offsets = el.getAttribute("textSpan");
					String[] offs = offsets.split("-");
					offsetLow = Integer.valueOf(offs[0]);
					offsetHigh = Integer.valueOf(offs[1]);
				}
				j++;
			}
			NodeList nl = doc.getElementsByTagName("sentence");
			docID = ((Element) doc.getElementsByTagName("document").item(0)).getAttribute("id");
			log.info(docID);
			bw_id.write(docID + "\n");
			String se=null;
			String sentID;
			String[] label_text,label_values;
			for(int i = 0; i < nl.getLength(); i++) {
				el = (Element) nl.item(i);
				String text = el.getElementsByTagName("text").item(0).getTextContent();
				if(text.split(" ").length >= 5) {
					se = el.getAttribute("selection");
					sentID = el.getAttribute("id");
					String[] charOffs = el.getAttribute("charOffset").split("-");
					label_text = convertToText(se);
					label_values = convertToValues(se);
					if(offsetLow <= Integer.valueOf(charOffs[0]) && offsetHigh >= Integer.valueOf(charOffs[1])) {
						if(se != null && se != "") {
							bw.write(docID + "|" + sentID + "|" + "|" + String.join(",", label_text) + "|" + "|" + text);
							bw.write("\n");
							labeled++;
						}else {
							bw.write(docID + "|" + sentID + "|" + "|None|" + "|" + text);
							bw.write("\n");
							unlabeled++;
						}
					}
				}
			}
			log.fine("Number of sentences: " + nl.getLength());
		}
		log.info("Labeled sentences: " + labeled + " Unlabeled sentence: " + unlabeled);
		bw.close();
		bw_id.close();
		
	}
}
