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
import lab.tools.NumFormat;
import deconvolution.Command;
import deconvolutionlab.Config;

public class TikhonovRegularizationInverseFilterPanel extends AbstractAlgorithmPanel implements ActionListener, ChangeListener, KeyListener {
	
	private RegularizationPanel reg; 
	private TikhonovRegularizationInverseFilter algo = new TikhonovRegularizationInverseFilter(0.1);
	
	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();			
		reg = new RegularizationPanel(params[0]);
		GridPanel pn = new GridPanel(false);
		pn.place(0, 0, reg);
				
		Config.register("Algorithm." + algo.getShortname(), "reg", reg.getText(), "0.1");
		reg.getText().addKeyListener(this);
		reg.getSlider().addChangeListener(this);
		return pn;

	}
	
	@Override
	public String getCommand() {
		return NumFormat.nice(reg.getValue());
	}
		
	@Override
	public void stateChanged(ChangeEvent e) {
		reg.getText().removeKeyListener(this);
		reg.updateFromSlider();
		Command.command();
		reg.getText().addKeyListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Command.command();
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
