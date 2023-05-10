/*
 * 	NAME: 			EMILIO CRUZ
 * 	APPLICATION:	EYE COLOR CHANGER
 * 	ASSIGNMENT: 	FINAL PROJECT (SER305)
 * 	DATE: 			MAY 2023 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.opencv.core.Point;
import java.util.ArrayList;

// show initial file chooser and display selected image in a JFrame
public class ImageChooser extends JFrame
{
	private File selectedFile;
	private JLabel leftImageLabel, rightImageLabel;
	private Image scaledImage;
	private Color selectedColor;
	private double redScale, greenScale, blueScale;
	private FaceDetection faceDetection;

	public ImageChooser()
	{	
		super("View Image");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		// display file chooser and retrieve selection
		JFileChooser fileChooser = new JFileChooser();
		int option = fileChooser.showOpenDialog(this);

		// if user clicks "open"
		if (option == JFileChooser.APPROVE_OPTION) 
		{		
			// show ImageChooser JFrame
			this.setVisible(true);
			
			selectedFile = fileChooser.getSelectedFile();

			// create an image icon from the selected file
			ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
			System.out.println(selectedFile.getAbsolutePath());
			Image image = icon.getImage();

			// calculate scale factor for resizing
			int scaleFactor = image.getWidth(this) / 300;
			int newWidth;
			int newHeight;

			// prevent divide by 0 if image isn't correctly read
			if(scaleFactor != 0)
			{
				newWidth = image.getWidth(this) / scaleFactor;
				newHeight = image.getHeight(this) / scaleFactor;
			}
			else 
			{
				newWidth = image.getWidth(this);
				newHeight = image.getHeight(this);
			}

			// scale the image
			scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

			// create labels to display images side by side
			leftImageLabel = new JLabel(new ImageIcon(scaledImage));
			rightImageLabel = new JLabel(new ImageIcon(scaledImage));
			
			// print rgb and location when pixel in left image is clicked
	        leftImageLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	                // Get the x and y coordinates of the mouse click
	                int x = e.getX();
	                int y = e.getY();

	                BufferedImage bufferedImage = convertToBufferedImage(scaledImage);
	                Color color = new Color(bufferedImage.getRGB(x, y));
	                
	                System.out.println("x: " + x + ", y: " + y);
	                System.out.println("RGB value: " + color.getRed() + ", " +color.getGreen()+ ", " + color.getBlue());
	            }});

			leftImageLabel.setSize(new Dimension(newWidth, newHeight));
			rightImageLabel.setSize(new Dimension(newWidth, newHeight));

			// create panel for images
			JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			imagePanel.add(leftImageLabel);
			imagePanel.add(rightImageLabel);

			// create panel for buttons
			JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JButton colorButton = new JButton("Pick a color!");
			JButton saveButton = new JButton("Save!");
			buttonPanel.add(colorButton);
			buttonPanel.add(saveButton);

			initFaceDetection();

			// open color chooser and call color change method
			colorButton.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JColorChooser colorChooser = new JColorChooser();
					selectedColor = JColorChooser.showDialog(ImageChooser.this, "Select a color", Color.WHITE);
					changeEyeColors();
					System.out.println(faceDetection.getEyeSpecs());
				}
			});

			// open file chooser and write final image to specified save file
			saveButton.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JFileChooser fileSaver = new JFileChooser();
					int selection = fileSaver.showSaveDialog(ImageChooser.this);

					// if user clicks "save"
					if (selection == JFileChooser.APPROVE_OPTION) 
					{
						// Get the selected file
						File file = fileSaver.getSelectedFile();

						// Get the image to be saved (e.g. from a BufferedImage object)
						BufferedImage saveImage = convertToBufferedImage(
								((ImageIcon)(rightImageLabel.getIcon())).getImage());

						// Save the image to the selected file
						try {
							ImageIO.write(saveImage, "png", file);
							System.out.println("Save Successful!");
							System.out.println(file.getAbsolutePath());
						} 
						catch (IOException ex) {
							ex.printStackTrace();
							System.err.println("UNKNOWN ERROR WHILE SAVING");
						}
					}
				}
			});

			this.add(imagePanel, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			this.pack();
		}
	}

	// run facial detection on image
	public void initFaceDetection()
	{
		faceDetection = new FaceDetection(convertToBufferedImage(scaledImage));
	}

	// change colors of entire image
	public void changeImageColors() 
	{
		setColorScaleFactors(selectedColor);

		BufferedImage oldImage = convertToBufferedImage(scaledImage);
		BufferedImage newImage = oldImage;

		// iterate over all the pixels in the image and change their color to the selected color
		for (int y = 0; y < oldImage.getHeight(); y++) 
		{
			for (int x = 0; x < oldImage.getWidth(); x++) 
			{
				Color pixelColor = new Color(oldImage.getRGB(x, y));
				Color newColor = getScaledColor(pixelColor);
				newImage.setRGB(x, y, newColor.getRGB());
			}
		}

		// update label with new image
		rightImageLabel.setIcon(new ImageIcon(newImage));
	}

	// change colors for only eyes
	public void changeEyeColors()
	{
		setColorScaleFactors(selectedColor);

		BufferedImage oldImage = convertToBufferedImage(scaledImage);
		BufferedImage newImage = oldImage;
		
		ArrayList<EyeSpecs> eyeSpecs = faceDetection.getEyeSpecs();
		
		// call floodfill for each detected eye
		for(EyeSpecs specs : eyeSpecs)
		{
			boolean[][] visited = new boolean[newImage.getHeight()][newImage.getWidth()];
			floodFill(newImage, visited, (int) specs.eyeCenter.x, (int) specs.eyeCenter.y, specs, true);
		}
		
		rightImageLabel.setIcon(new ImageIcon(newImage));
	}

	// "bucket" fill recursive algorithm for coloring eyes
	public void floodFill(BufferedImage image, boolean[][] visited, int x, int y, EyeSpecs specs, boolean initialCall)
	{
		Color pixelColor = new Color(image.getRGB(x, y));
		Color newColor = getScaledColor(pixelColor);
		
		// if pixel has already been visited
		if(visited[y][x]) 
			return;
		
		// if pixel is outside of eye range
		if(x < specs.leftBound || x > specs.rightBound || y < specs.topBound || y > specs.botBound)
			return;
		
		// if pixel is in one of the "corners" of eye box
		if ((Math.abs(y - specs.botBound) < 10 || Math.abs(y - specs.topBound) < 10) && 
			(Math.abs(x - specs.leftBound) < 15 || Math.abs(x - specs.rightBound) < 15))
			return;
		
		// try again if initial recursive call fails
		if(isBlack(pixelColor) && initialCall)
		{
			visited[y][x] = true;
			System.out.println("Failed inital call for eye");
			floodFill(image, visited, x, y+2, specs, true);
		}
		else if(isBlack(pixelColor))
		{
			visited[y][x] = true;
			return;
		}
		
		// try again if initial recursive call fails
		if(isWhite(pixelColor) && initialCall) 
		{
			visited[y][x] = true;
			System.out.println("Failed inital call for eye");
			floodFill(image, visited, x, y+2, specs, true);
		}
		else if(isWhite(pixelColor))
		{
			visited[y][x] = true;
			return;
		}

		image.setRGB(x, y, newColor.getRGB());
		visited[y][x] = true;

		// calls for adjacent pixels
		floodFill(image, visited, x+1, y, specs, false);
		floodFill(image, visited, x-1, y, specs, false);
		floodFill(image, visited, x, y+1, specs, false);
		floodFill(image, visited, x, y-1, specs, false);
	}

	// determines if rgb values are similar and color is dark
	public boolean isBlack(Color color)
	{
		boolean grayscale = (Math.abs(color.getRed() - color.getBlue()) < 20 &&
				Math.abs(color.getBlue() - color.getGreen()) < 20 &&
				Math.abs(color.getRed() - color.getGreen()) < 20);
		
		boolean isBlack = color.getRed() < 40 && color.getBlue() < 40 && color.getGreen() < 40;
		
		return grayscale && isBlack;
	}
	
	// determines if rgb values are similar and color is light
	public boolean isWhite(Color color)
	{
		boolean grayscale = (Math.abs(color.getRed() - color.getBlue()) < 20 &&
				Math.abs(color.getBlue() - color.getGreen()) < 20 &&
				Math.abs(color.getRed() - color.getGreen()) < 20);
		
		boolean isWhite = color.getRed() > 150 && color.getBlue() > 150 && color.getGreen() > 150;
		
		return grayscale && isWhite;
	}

	// creates new color based on scale factors and original color
	public Color getScaledColor(Color oldColor)
	{
		int red, green, blue;

		red = (int) (oldColor.getRed() * redScale);
		green = (int) (oldColor.getGreen() * greenScale);
		blue = (int) (oldColor.getBlue() * blueScale);

		return new Color(red, green, blue);
	}

	// determines main color (R, G, B) and scales the others as a ratio
	public void setColorScaleFactors(Color color)
	{
		if(color.getRed() >= color.getBlue() && color.getRed() >= color.getGreen())
		{
			redScale = 1;
			greenScale = (double) color.getGreen() / color.getRed();
			blueScale = (double) color.getBlue() / color.getRed();
		}

		else if(color.getGreen() >= color.getRed() && color.getGreen() >= color.getBlue())
		{
			redScale = (double) color.getRed() / color.getGreen();
			greenScale = 1;
			blueScale = (double) color.getBlue() / color.getGreen();
		}

		else 
		{
			redScale = (double) color.getRed() / color.getBlue();
			greenScale = (double) color.getGreen() / color.getBlue();
			blueScale = 1;
		}
	}

	// convert Image to BufferedImage
	public static BufferedImage convertToBufferedImage(Image img)
	{
		if (img instanceof BufferedImage)
			return (BufferedImage) img;

		BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		Graphics2D b = bufferedImage.createGraphics();
		b.drawImage(img, 0, 0, null);
		b.dispose();

		return bufferedImage;
	}
}