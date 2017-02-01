package deconvolution.algorithm;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.GridPanel;
import lab.component.SpinnerRangeDouble;
import lab.component.SpinnerRangeInteger;
import lab.tools.NumFormat;
import deconvolution.Command;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;

public class LandweberPanel extends AbstractAlgorithmPanel implements ChangeListener{

	private SpinnerRangeInteger	spnIter	= new SpinnerRangeInteger(10, 1, 99999, 1);
	private SpinnerRangeDouble	spnStep	= new SpinnerRangeDouble(1, 0, 2, 0.1);
	private Landweber algo = new Landweber(10, 1);

	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();
		
		spnIter.setPreferredSize(Constants.dimParameters);
		spnStep.setPreferredSize(Constants.dimParameters);

		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\"><b>Iterations</b></span></html>");
		pn.place(1, 1, "<html><span \"nowrap\"><i>N</i></span></html>");
		pn.place(1, 2, spnIter);
		pn.place(1, 3, "<html><span \"nowrap\">Step <i>&gamma;</i></span></html>");
		pn.place(1, 4, spnStep);
		
		pn.place(2, 0, "<html><span \"nowrap\"><b>Regularization</b></span></html>");
		pn.place(2, 1, 4, 1, "<html><span \"nowrap\">No regularization</i></span></html>");
		
		Config.register("Algorithm." + algo.getShortname(), "iterations", spnIter, params[0]);
		Config.register("Algorithm." + algo.getShortname(), "step", spnStep, params[1]);
		spnIter.addChangeListener(this);
		spnStep.addChangeListener(this);
		return pn;
	}

	@Override
	public String getCommand() {
		int iter = spnIter.get();
		double gamma = spnStep.get();	
		return iter + " " + NumFormat.nice(gamma);
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
