package Application.Gif;

import javafx.scene.Cursor;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class GifAsMp4 extends MediaView
{
	private MediaPlayer mMediaPlayer;
	private boolean mIsPlaying;
	
	public GifAsMp4(String mp4Url)
	{
		mMediaPlayer = new MediaPlayer(new Media(mp4Url));					
		setMediaPlayer(mMediaPlayer);

		setFitWidth(500);
		setPreserveRatio(true);
		setCursor(Cursor.HAND);
		
		mIsPlaying = false;
	}

	public void playOrPause()
	{
		if(mIsPlaying)
		{
			mMediaPlayer.play();
		}
		
		else
		{
			mMediaPlayer.pause();
		}

		mIsPlaying = !mIsPlaying;
	}
}
