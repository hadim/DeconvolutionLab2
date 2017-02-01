package deconvolution.algorithm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lab.component.GridPanel;
import lab.component.RegularizationPanel;
import lab.component.SpinnerRangeDouble;
import lab.component.SpinnerRangeInteger;
import lab.tools.NumFormat;
import deconvolution.Command;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;

public class ICTMPanel extends AbstractAlgorithmPanel implements KeyListener, ActionListener, ChangeListener {
	
	private SpinnerRangeInteger	spnIter	= new SpinnerRangeInteger(10, 1, 99999, 1, "###");
	private SpinnerRangeDouble	spnStep	= new SpinnerRangeDouble(1, 0, 2, 0.1, "#.#");
	private RegularizationPanel reg; 

	private ICTM algo = new ICTM(10, 0.1, 1);
	
	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();			
		spnIter.setPreferredSize(Constants.dimParameters);
		spnStep.setPreferredSize(Constants.dimParameters);
		reg = new RegularizationPanel(params[2]);
				
		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\"><b>Iterations</b></span></html>");
		pn.place(1, 1, "<html><span \"nowrap\"><i>N</i></span></html>");
		pn.place(1, 2, spnIter);
		pn.place(1, 3, "<html><span \"nowrap\">Step <i>&gamma;</i></span></html>");
		pn.place(1, 4, spnStep);
		
		pn.place(2, 0, 5, 1, reg);
		
		Config.register("Algorithm." + algo.getShortname(), "iterations", spnIter, params[0]);
		Config.register("Algorithm." + algo.getShortname(), "step", spnStep, params[1]);
		Config.register("Algorithm." + algo.getShortname(), "reg", reg.getText(), "0.1");
		reg.getText().addKeyListener(this);
		reg.getSlider().addChangeListener(this);
		spnIter.addChangeListener(this);
		spnStep.addChangeListener(this);
		return pn;
	}


	@Override
	public String getCommand() {
		double gamma = spnStep.get();	
		double lambda = reg.getValue();
		return spnIter.get() + " " + NumFormat.nice(gamma) + " " + NumFormat.nice(lambda);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Command.command();
	} 
	
	@Override
	public void stateChanged(ChangeEvent e) {
		reg.getText().removeKeyListener(this);
		reg.updateFromSlider();
		Command.command();
		reg.getText().addKeyListener(this);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		reg.getSlider().removeChangeListener(this);
		reg.updateFromText();
		Command.command();
		reg.getSlider().addChangeListener(this);
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
