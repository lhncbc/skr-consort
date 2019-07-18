package gov.nih.nlm.consort.pmc;

import java.util.Set;

public class Labeling {
	private String docId;
	private String sentId;
	private String annotator;
	private String section;
	private Set<String> labels;
	
	
	public Labeling(String docId, String sentId, String annotator, Set<String> labels) {
		this.docId = docId;
		this.sentId = sentId;
		this.annotator = annotator;
		this.labels = labels;
	}
	
	
	public Labeling(String docId, String sentId, String annotator, String section, Set<String> labels) {
		this(docId,sentId,annotator,labels);
		this.section = section;
	}
	
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getSentId() {
		return sentId;
	}
	public void setSentId(String sentId) {
		this.sentId = sentId;
	}
	public String getAnnotator() {
		return annotator;
	}
	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}
	public Set<String> getLabels() {
		return labels;
	}
	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}
	
	
}
