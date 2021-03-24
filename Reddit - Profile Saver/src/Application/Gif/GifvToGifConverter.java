package Application.Gif;

import java.util.ArrayList;

import org.bytedeco.javacv.FFmpegFrameGrabber;

import Application.Startup;
import javafx.scene.image.Image;

public class GifvToGifConverter
{
	private Image[] mFrames;
	
	public Animation convert(String mp4Url)
	{
		FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(mp4Url);

		try
		{
			frameGrabber.start();
		}

		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		double frameRate = frameGrabber.getFrameRate();		
		double frameCount = frameGrabber.getLengthInFrames();
		mFrames = new Image[(int) frameCount];
		
		long startTime = System.nanoTime();
		
		int threadCount = 10;
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for(int i = 0; i < threadCount; i++)
		{
			int startIndex = (((int) frameCount / threadCount) * i);
			int endIndex = (((int) frameCount / threadCount) * (i + 1));
			
			if(i == threadCount - 1)
			{
				endIndex = (int) frameCount;
			}
			
			Thread thread = new GifvToGifThread(this, mp4Url, startIndex, endIndex);
			threads.add(thread);
			
			thread.start();
		}
		
		try
		{
			for(Thread thread : threads)
			{
				thread.join();
			}
		}

		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}

		long endTime = System.nanoTime();
		double durationSec = (endTime - startTime) / 1000000000.0;
		System.out.println("TIME TAKEN: " + (durationSec / 60.0));
		
		if(Startup.isStopped())
		{
			return null;
		}
		
		return new Animation(mFrames, (frameCount / frameRate) * 1000);
	}

	public void addFrame(int index, Image image)
	{
		mFrames[index] = image;
//		System.out.println(index);
	}
}
