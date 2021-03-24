package Application.Gif;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class Gif extends Animation// Source: https://stackoverflow.com/a/28185996
{
	public Gif(String fileName)
	{
		GifDecoder gifDecoder = new GifDecoder();
		gifDecoder.read(fileName);

		double duration = (gifDecoder.getDelay(0) + 1) * gifDecoder.getFrameCount();
		
		Image[] frames = new Image[gifDecoder.getFrameCount()];
		for(int i = 0; i < gifDecoder.getFrameCount(); i++)
		{
			WritableImage writeImg = null;
			BufferedImage buffImg = gifDecoder.getFrame(i);
			frames[i] = SwingFXUtils.toFXImage(buffImg, writeImg);
		}
		
		super.initialize(frames, duration);
	}
}
