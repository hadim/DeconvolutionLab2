package deconvolution.modules;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JSplitPane;
import javax.swing.text.BadLocationException;

import bilib.component.HTMLPane;
import bilib.table.CustomizedTable;
import deconvolution.Deconvolution;

public class RecapDModule extends AbstractDModule implements KeyListener {

	private HTMLPane		pnCommand;
	private CustomizedTable table;

	public RecapDModule(Deconvolution deconvolution) {
		super(deconvolution);
		// Panel command
		pnCommand = new HTMLPane("Monaco", "#10FF10", "100020", 100, 100);
		pnCommand.append("p", deconvolution.getCommand());
		pnCommand.setEditable(true);
		pnCommand.addKeyListener(this);
		table = new CustomizedTable(new String[] { "Features", "Values" }, false);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, table.getPane(200, 200), pnCommand.getPane());
	}
	
	public void update() {
		if (table == null)
			return;
		table.removeRows();
		for (String[] feature : deconvolution.recap())
			table.append(feature);

		split.setDividerLocation(0.5);
		split.repaint();
	}

	public String getCommand() {
		return pnCommand.getText();
	}
	
	@Override
	public String getName() {
		return "Recap";
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		try {
			int len = pnCommand.getDocument().getLength();
			String command = pnCommand.getDocument().getText(0, len);
			deconvolution.setCommand(command);
			table.removeRows();
			for (String[] feature : deconvolution.recap())
				table.append(feature);
		}
		catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}



}