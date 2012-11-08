package dexels.apachecon.billboard;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = { Billboard.class }, xmlns = "http://www.osgi.org/xmlns/scr/v1.1.0")
public class Billboard {

	private JFrame frame;
	private JLabel label;

	@Activate
	public void activate() {
		try {
			frame = new JFrame("Billboard");
			label = new JLabel("aaa");
			label.setFont(new Font("sans", Font.PLAIN, 40));
			// label.setHorizontalAlignment(1);
			frame.setSize(500, 300);
			frame.setLocation(100, 100);
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.setVisible(true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Deactivate
	public void deactivate() {
		frame.dispose();

	}

	public void show(final String text) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				label.setText(text);
			}
		});
	}

	public void sleep(final long millis) {
		try {
			Thread.sleep(millis);
			System.err.println("slept");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
