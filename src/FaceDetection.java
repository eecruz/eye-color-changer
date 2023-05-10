/*
 * 	NAME: 			EMILIO CRUZ
 * 	APPLICATION:	EYE COLOR CHANGER
 * 	ASSIGNMENT: 	FINAL PROJECT (SER305)
 * 	DATE: 			MAY 2023 
 */

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

// run facial detection on specified image
public class FaceDetection 
{
	private Mat imageMat;
	private ArrayList<EyeSpecs> eyeSpecs;
	
	public FaceDetection(BufferedImage image)
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		eyeSpecs = new ArrayList<EyeSpecs>();
		imageMat = bufferedImageToMat(image);
		
		// load references for face and eyes
		CascadeClassifier faceDetector = new CascadeClassifier("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");
		CascadeClassifier eyeDetector = new CascadeClassifier("C:\\opencv\\sources\\data\\haarcascades\\haarcascade_eye.xml");
		
		MatOfRect detectedFaces = new MatOfRect();
		MatOfRect detectedEyes = new MatOfRect();
		
		// run facial detection on image as Matrix
		faceDetector.detectMultiScale(imageMat, detectedFaces);
		eyeDetector.detectMultiScale(imageMat, detectedEyes);
		
		// if faces detected
		if(detectedFaces.toArray().length != 0)
		{
			for (Rect face : detectedFaces.toArray())
			{
				Imgproc.putText(imageMat, "Face", new Point(face.x,face.y-5), 1, 2, new Scalar(0,0,255));								
				Imgproc.rectangle(imageMat, new Point(face.x, face.y), new Point(face.x + face.width, face.y + face.height),
						new Scalar(0, 100, 0),3);
				
				for(Rect eye : detectedEyes.toArray())
				{
					if (eye.x > face.x && eye.x < face.x + face.width &&
						eye.y > face.y && eye.y < face.y + face.height)
					{
						Imgproc.putText(imageMat, "Eye", new Point(eye.x,eye.y-5), 1, 2, new Scalar(0,0,255));				
						Imgproc.rectangle(imageMat, new Point(eye.x, eye.y), new Point(eye.x + eye.width, eye.y + eye.height),
								new Scalar(200, 200, 100),2);
						
						Point center = new Point(eye.x + (eye.width/2), eye.y + (eye.height/2));
						eyeSpecs.add(new EyeSpecs(center, (int) (center.x - eye.width/3.5), (int) (center.x + eye.width/3.5), 
									(int) center.y - eye.height/4, (int) center.y + eye.height/4));
					}
				}
			}
		}
		
		else // eyes but no face
		{
			for(Rect eye : detectedEyes.toArray())
			{

				Imgproc.putText(imageMat, "Eye", new Point(eye.x,eye.y-5), 1, 2, new Scalar(0,0,255));				
				Imgproc.rectangle(imageMat, new Point(eye.x, eye.y), new Point(eye.x + eye.width, eye.y + eye.height),
						new Scalar(200, 200, 100),2);
				
				Point center = new Point(eye.x + (eye.width/2), eye.y + (eye.height/2));
				eyeSpecs.add(new EyeSpecs(center, (int) (center.x - eye.width/3.5), (int) (center.x + eye.width/3.5), 
						(int) center.y - eye.height/4, (int) center.y + eye.height/4));

			}
		}

	}
	
	// return list of specs for each detected eye
	public ArrayList<EyeSpecs> getEyeSpecs()
	{
		return eyeSpecs;
	}
	
	// return image with faces and eyes boxed
	public BufferedImage getImage()
	{
		BufferedImage image = null;
		MatOfByte byteMat = new MatOfByte();
		Imgcodecs.imencode(".jpg", imageMat, byteMat);
		
		byte[] byteArray = byteMat.toArray();
		
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			image = ImageIO.read(in);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return image;
	}
	
	// convert BufferedImage to Matrix
	public static Mat bufferedImageToMat(BufferedImage image)
	{
		BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
		
		byte[] data = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();  
		Mat mat = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), CvType.CV_8UC3);
		mat.put(0, 0, data);

		return mat;
	}
}
