package deconvolution.modules;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;

public abstract class AbstractDModule {
	
	protected Deconvolution deconvolution;
	protected JSplitPane split;
	private AsynchronousTimer signalTimer;
	private Timer			timer		= new Timer();
	private int count = 0;
	
	private Monitors monitors;
	private String message = "";
	
	public AbstractDModule(Deconvolution deconvolution) {
		this.deconvolution = deconvolution;
		this.monitors = deconvolution.monitors;
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
	
	public void stopAsynchronousTimer() {
		if (monitors != null)
			monitors.progress(message, 100);
		if (signalTimer != null) {
			signalTimer.cancel();
			signalTimer = null;
		}
	}

	public void startAsynchronousTimer(String message, long refreshTime) {
		if (signalTimer != null) {
			signalTimer.cancel();
			signalTimer = null;
		}
		this.message = message;
		signalTimer = new AsynchronousTimer(message);
		timer.schedule(signalTimer, 0, refreshTime);
	}

	public void signal(String message) {
		if (monitors != null)
			monitors.progress(message, count+=2);
	}

	private class AsynchronousTimer extends TimerTask {
		private String message;
		public AsynchronousTimer(String message) {			
			this.message = message;
		}
		@Override
		public void run() {			
			signal(message);
		}
	}
	
	@Override
	public void finalize() {
		stopAsynchronousTimer();
	}

}
