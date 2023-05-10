/*
 * 	NAME: 			EMILIO CRUZ
 * 	APPLICATION:	EYE COLOR CHANGER
 * 	ASSIGNMENT: 	FINAL PROJECT (SER305)
 * 	DATE: 			MAY 2023 
 */

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

// main class, opens ImageChooser when button is clicked
public class EyeColorChanger extends JFrame 
{
	public EyeColorChanger() 
	{
		super("Eye Color Chooser");
		this.setPreferredSize(new Dimension(400, 175));
		this.setSize(400, 175);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);

		String text = "<html><div align=\"center\">Welcome to Eye Color Chooser! " 
				+ "Click the button below to select an image and get started.</html>";

		// create title label
		JLabel label = new JLabel(text);
		label.setVerticalAlignment(JLabel.TOP);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setFont(new Font("Verdana Bold", Font.BOLD, 21));

		// create button
		JButton openButton = new JButton("Select Image");
		openButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				ImageChooser imageChooser = new ImageChooser();
			}
		});

		// create a panel for button
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(openButton);

		// add panels to frame
		this.add(label, "North");
		this.add(buttonPanel, "South");
	}

	// main method
	public static void main(String[] args) 
	{    
		new EyeColorChanger();
	}

}