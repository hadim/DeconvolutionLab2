package deconvolution.modules;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import deconvolution.Deconvolution;

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
		frame.setVisible(true);
	}
	
	public abstract void update();
	public abstract String getName();
}
