package Application.Gif;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import Application.Startup;
import javafx.embed.swing.SwingFXUtils;

public class GifvToGifThread extends Thread
{
	private GifvToGifConverter mHub;
	private String mUrl;
	private int mStartFrame, mEndFrame;
	
	public GifvToGifThread(GifvToGifConverter hub, String mp4Url, int startFrame, int endFrame)
	{
		mHub = hub;
		mUrl = mp4Url;
		mStartFrame = startFrame;
		mEndFrame = endFrame;
	}
	
	@Override
	public void run()
	{
		Java2DFrameConverter converter = new Java2DFrameConverter();
		FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(mUrl);

		try
		{
			frameGrabber.start();
		}

		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		try
		{
			for(int i = mStartFrame; i < mEndFrame; i++)
			{
				if(Startup.isStopped())
				{
					return;
				}
				
				frameGrabber.setFrameNumber(i);
				Frame frame = frameGrabber.grab();
				BufferedImage buffImg = converter.convert(frame);
				
				mHub.addFrame(i, SwingFXUtils.toFXImage(buffImg, null));
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
