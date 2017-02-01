package deconvolution.algorithm;

import javax.swing.JPanel;

import lab.component.GridPanel;

public class NonStabilizedDivisionPanel extends AbstractAlgorithmPanel {

	private NonStabilizedDivision algo = new NonStabilizedDivision();
	
	@Override
	public JPanel getPanelParameters() {
		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\">No parameters</span></html>");
		return pn;
	}
	
	@Override
	public String getCommand() {
		return "";
	}
	
	@Override
	public String getName() {
		return algo.getName();
	}

	@Override
	public String getShortname() {
		return algo.getShortname();
	}
	
	@Override
	public String getDocumentation() {
		String s = "";
		s += "<h1>" + getName() + "</h1>";
		s += "<p>Iterative: " + algo.isIterative() + "</p>";
		s += "<p>Step controllable: " + algo.isStepControllable() + "</p>";
		s += "<p>Regularization: " + algo.isRegularized() + "</p>";
		s += "<p>Wavelet-base: " + algo.isWaveletsBased() + "</p>";
		s += "<p>Shortname: " + getShortname() + "</p>";
		return s;
	}


}
