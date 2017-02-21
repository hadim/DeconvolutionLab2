package lab.component;

import java.awt.Insets;

import javax.swing.JButton;

public class BorderToggledButton extends JButton {

	private String text = "";
	public BorderToggledButton(String text) {
		super(text);
		this.text = text;
		setMargin(new Insets(1, 1, 1, 1));
	}

	public void setSelected(boolean selected) {
		if (selected)
			setText("<html><b>" + text + "</b></html>");
		else
			setText(text);
	}
}
