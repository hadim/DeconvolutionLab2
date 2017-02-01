package lab.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import lab.tools.NumFormat;

public class RegularizationPanel extends JPanel {

	private JSlider		slider	= new JSlider();
	private JTextField	txt		= new JTextField(10);
	private double		base	= Math.pow(10, 1. / 3.0);
	private double		logbase	= Math.log(base);
	
	public RegularizationPanel(double value) {
		setLayout(new BorderLayout());
		slider.setMinimum(-54);
		slider.setMaximum(30);
		slider.setPreferredSize(new Dimension(200, 40));
		int p = (int) Math.round(Math.log(Math.abs(value)) / logbase);
		slider.setValue(p);

		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		JLabel lbl0 = new JLabel("Off");
		JLabel lbl1 = new JLabel("Low");
		JLabel lbl2 = new JLabel("High");
		JLabel lbl4 = new JLabel("1E-6");
		JLabel lbl5 = new JLabel("1.0");
		java.awt.Font font = lbl1.getFont();
		java.awt.Font small = new java.awt.Font(font.getFamily(), font.getStyle(), font.getSize() - 3);
		lbl0.setFont(small);
		lbl1.setFont(small);
		lbl2.setFont(small);
		lbl4.setFont(small);
		lbl5.setFont(small);
		labels.put(-54, lbl0);
		labels.put(-36, lbl1);
		labels.put(-18, lbl4);
		labels.put(0, lbl5);
		labels.put(27, lbl2);
		slider.setMinorTickSpacing(3);
		// js.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(9);
		slider.setLabelTable(labels);
		add(new JLabel("<html>Reg. &lambda;</html>"), BorderLayout.WEST);
		add(slider, BorderLayout.CENTER);
		add(txt, BorderLayout.EAST);
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
	public JTextField getText() {
		return txt;
	}
	
	public void updateFromSlider() {
		double d = Math.pow(base, slider.getValue());
		d = Math.min(d, Math.pow(base, slider.getMaximum()));
		d = Math.max(d, Math.pow(base, slider.getMinimum()));
		txt.setText(NumFormat.nice(d));
	}

	public void updateFromText() {
		String typed = txt.getText();
		double value = NumFormat.parseNumber(typed, 1);
		int p = (int) Math.round(Math.log(Math.abs(value)) / logbase);
		slider.setValue(p);
	}

	public double getValue() {
		return Math.pow(base, slider.getValue());	
	}
}
