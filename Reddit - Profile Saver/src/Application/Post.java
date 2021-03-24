package Application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Post implements Comparable<Post>
{
	private String mTitle, mAuthor, mSubreddit, mPostUrl, mThumbnailUrl;
	private String[] mContentUrls;
	private long mDateInSec;
	private boolean mIsSaved, mIsNsfw;
	private int mNumber;
	
	public Post(String title, String author, String subreddit, String[] contentUrl, String postUrl, String thumbnailUrl, long dateInSec, boolean isSaved, boolean isNsfw, int number)
	{
		if(isNull(title) || isNull(author) || isNull(subreddit) || isNull(postUrl) || isNull(thumbnailUrl))
		{
			throw new NullPointerException("A passed in String was null.");
		}
		
		else if(contentUrl == null)
		{
			throw new NullPointerException("Passed in Content Urls were null.");
		}
		
		mTitle = title;
		mAuthor = author;
		mSubreddit = subreddit;
		mContentUrls = contentUrl;
		mPostUrl = postUrl;
		mThumbnailUrl = thumbnailUrl;
		mDateInSec = dateInSec;
		mIsSaved = isSaved;
		mIsNsfw = isNsfw;
		mNumber = number;
	}
	
	private boolean isNull(String str)
	{
		return str == null;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public String getAuthor()
	{
		return mAuthor;
	}

	public String getSubreddit()
	{
		return mSubreddit;
	}

	public String[] getContentUrls()
	{
		return mContentUrls;
	}

	public String getPostUrl()
	{
		return mPostUrl;
	}

	public String getThumbnailUrl()
	{
		if(mThumbnailUrl.compareTo("default") == 0 && mContentUrls.length > 0)
		{
			return mContentUrls[0];
		}
		
		return mThumbnailUrl;
	}

	public String getDate()
	{
		Date date = new Date(mDateInSec * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm a", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("PST"));
		return sdf.format(date);
	}
	
	public boolean isGif()
	{
		return mContentUrls[0].contains(".gif");
	}
	
	public boolean isGallery()
	{
		return mContentUrls.length > 1;
	}

	public boolean isSaved()
	{
		return mIsSaved;
	}

	public boolean isNsfw()
	{
		return mIsNsfw;
	}
	
	public int getNumber()
	{
		return mNumber;
	}
	
	public boolean hasThumbnail()
	{
		return mThumbnailUrl.compareTo("self") != 0;
	}

	@Override
	public int compareTo(Post that)
	{
		return mNumber - that.getNumber();
	}
}
