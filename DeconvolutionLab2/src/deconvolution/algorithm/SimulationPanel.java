package deconvolution.algorithm;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

import lab.component.GridPanel;
import deconvolution.Command;
import deconvolutionlab.Config;

public class SimulationPanel extends AbstractAlgorithmPanel implements KeyListener {
	
	private JTextField	txtMean;
	private JTextField	txtStdev;
	private JTextField	txtPoisson;

	private Simulation algo = new Simulation(0, 0, 0);
	
	@Override
	public JPanel getPanelParameters() {
		double[] params = algo.getDefaultParameters();
		txtMean = new JTextField(""+ params[0], 5);
		txtStdev = new JTextField(""+ params[1], 5);
		txtPoisson = new JTextField(""+ params[2], 5);
				
		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html>Gaussian (Mean)</html>");
		pn.place(1, 2, txtMean);
		pn.place(2, 0, "<html>Gaussian (Stdev)</html>");
		pn.place(2, 2, txtStdev);
		pn.place(3, 0, "<html>Poisson</html>");
		pn.place(3, 2, txtPoisson);
		txtMean.addKeyListener(this);
		txtStdev.addKeyListener(this);
		txtPoisson.addKeyListener(this);
		Config.register("Algorithm." + algo.getShortname(), "gaussian.mean", txtMean, params[0]);
		Config.register("Algorithm." + algo.getShortname(), "gaussian.stdev", txtStdev, params[1]);
		Config.register("Algorithm." + algo.getShortname(), "poisson", txtPoisson, params[2]);
		return pn;
	}
	
	@Override
	public String getCommand() {
		return txtMean.getText() +  "  " + txtStdev.getText() + " " + txtPoisson.getText();
	}

	@Override
    public void keyTyped(KeyEvent e) {
	    Command.command();
    }

	@Override
    public void keyPressed(KeyEvent e) {
	    Command.command();
    }

	@Override
    public void keyReleased(KeyEvent e) {
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
		s += "<p>This algorithm is only used for simulation. It convolves the input image with the PSF and adds some noise.</p>";
		return s;
	}


}
