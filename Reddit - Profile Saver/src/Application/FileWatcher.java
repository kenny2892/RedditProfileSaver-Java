package Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javafx.beans.property.StringProperty;

public class FileWatcher implements Runnable
{
	// Created with the help of this post: https://stackoverflow.com/a/20799857
	private final File mToWatch;
	private final StringProperty mToStore;
	private final Runnable mOnCompletion;
	private WatchService mWatchService;
	private int mOffset;

	public FileWatcher(File toWatch, StringProperty toStore, Runnable onCompletion) throws Exception
	{
		if(toWatch == null)
		{
			throw new NullPointerException("Passed in file was Null");
		}

		else if(!toWatch.exists())
		{
			throw new Exception("Passed in file does not exist");
		}

		else if(toStore == null)
		{
			throw new NullPointerException("Passed in toStore was Null");
		}

		else if(onCompletion == null)
		{
			throw new NullPointerException("Passed in onCompletion was Null");
		}

		mToWatch = toWatch;
		mToStore = toStore;
		mOnCompletion = onCompletion;
		mOffset = 0;
	}

	@Override
	public void run()
	{
		try
		{
			Path pathToWatch = mToWatch.toPath().getParent();
			mWatchService = pathToWatch.getFileSystem().newWatchService();
			pathToWatch.register(mWatchService, StandardWatchEventKinds.ENTRY_MODIFY);

			while(true)
			{
				WatchKey watchKey = mWatchService.take();
				if(!watchKey.reset())
				{
					break;
				}

				else if(!watchKey.pollEvents().isEmpty())
				{
					if(!retrieveLines())
					{
						break;
					}
				}

				Thread.sleep(1000);
			}
		}

		catch(ClosedWatchServiceException ex)
		{
			// Stop was called
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}

		mOnCompletion.run();
	}

	public void stop()
	{
		if(mWatchService != null)
		{
			try
			{
				mWatchService.close();
			}

			catch(IOException e)
			{
				e.printStackTrace();
			}

			mWatchService = null;
		}
	}

	private boolean retrieveLines()
	{
		try
		{
			BufferedReader resultsReader = new BufferedReader(new FileReader(mToWatch));
			resultsReader.skip(mOffset);

			while(true)
			{
				String line = resultsReader.readLine();

				if(line != null && line.compareTo("Completed") != 0)
				{
					String newContent = mToStore.get() == null ? line + "\n" : mToStore.get() + line + "\n";
					mToStore.setValue(newContent);
					mOffset += line.length() + 2;
				}

				else if(line != null && line.compareTo("Completed") == 0)
				{
					resultsReader.close();
					return false;
				}

				else
				{
					break;
				}
			}

			resultsReader.close();

			return true;
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}
}
