package deconvolution.algorithm;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.GridPanel;
import lab.component.SpinnerRangeInteger;
import deconvolution.Command;
import deconvolutionlab.Config;

public class RichardsonLucyPanel extends AbstractAlgorithmPanel implements ChangeListener {
	
	private SpinnerRangeInteger	spnIter	= new SpinnerRangeInteger(10, 1, 99999, 1);

	private RichardsonLucy algo = new RichardsonLucy(10);
	
	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();
		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\"><b>Iterations</b></span></html>");
		pn.place(1, 1, "<html><span \"nowrap\"><i>N</i></span></html>");
		pn.place(1, 2, spnIter);
		Config.register("Algorithm." + algo.getShortname(), "iterations", spnIter, params[0]);
		spnIter.addChangeListener(this);
		return pn;
	}

	@Override
	public String getCommand() {
		return "" + spnIter.get();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		Command.command();
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
