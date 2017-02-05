package lab.component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

public class PanelImage extends JPanel {

	private Image	image;
	private int		w	= -1;
	private int		h	= -1;

	public PanelImage(String filename) {
		super();
		image = ImageLoader.get(filename);
	}

	public PanelImage(String filename, int w, int h) {
		super();
		image = ImageLoader.get(filename);
		this.w = w;
		this.h = h;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			if (w < 0)
				g.drawImage(image, getWidth(), 0, getWidth(), getHeight(), null);
			else {
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.drawImage(image, (getWidth()-w)/2, 0, w, h, null);
			}
		}
		else {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

}
