package Application.Gif;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Animation extends Transition // Source: https://stackoverflow.com/a/28185996
{
	private ImageView mImageView;
	private int mSize, mPrevFrame;
	private Image[] mFrames;
	
	private boolean mIsPlaying;
	
	protected Animation()
	{
		// For overwriting purposes
	}

	public Animation(Image[] frames, double duration)
	{
		initialize(frames, duration);
	}

	protected void initialize(Image[] frames, double duration)
	{
		if(frames == null)
		{
			throw new NullPointerException("frames parameter is null.");
		}

		else if(frames.length == 0)
		{
			throw new IllegalArgumentException("frames is empty.");
		}

		mImageView = new ImageView(frames[0]);
		mFrames = frames;
		mSize = mFrames.length;

		setCycleCount(Animation.INDEFINITE);
		setCycleDuration(Duration.millis(duration));
		setInterpolator(Interpolator.LINEAR);
		
		mIsPlaying = false;
	}

	@Override
	protected void interpolate(double frac)
	{
		int index = Math.min((int) Math.floor(frac * mSize), mSize - 1);

		if(index != mPrevFrame)
		{
			mImageView.setImage(mFrames[index]);
			mPrevFrame = index;
		}
	}
	
	@Override
	public void play()
	{
		super.play();
		mIsPlaying = !mIsPlaying;
	}

	public ImageView getImageView()
	{
		return mImageView;
	}

	public boolean isPlaying()
	{
		return mIsPlaying;
	}
}
