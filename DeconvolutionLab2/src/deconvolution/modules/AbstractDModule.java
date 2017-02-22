package deconvolution.modules;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import deconvolutionlab.Lab;

public abstract class AbstractDModule {
	
	protected Deconvolution deconvolution;
	protected JSplitPane split;
	
	public AbstractDModule(Deconvolution deconvolution) {
		this.deconvolution = deconvolution;
	}
	
	public JSplitPane getPane() {
		return split;
	}
	
	public void show(String name) {
		JFrame frame = new JFrame(name);
		update();
		frame.getContentPane().add(split);
		frame.pack();
		Lab.setVisible(frame);
	}
	
	public abstract void update();
	public abstract String getName();
}
