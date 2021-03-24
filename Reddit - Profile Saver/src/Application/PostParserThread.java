package Application;

import org.json.JSONException;
import org.json.JSONObject;

public class PostParserThread extends Thread
{
	private PostsQueue mQueue;
	
	public PostParserThread(PostsQueue queue)
	{
		if(queue == null)
		{
			throw new NullPointerException("Passed in \"postQueue\" argument was null.");
		}
		
		mQueue = queue;
	}

	@Override
	public void run()
	{
		while(true)
		{
			JSONObject postEntry = mQueue.dequeue();

			if(postEntry == null)
			{
				break;
			}

			try
			{
				String postName = postEntry.getString("Post Name");
				String author = postEntry.getString("Author");
				String subreddit = postEntry.getString("Subreddit");
				double dateInSec = postEntry.getDouble("Created (in Seconds)");
				String urlContent = postEntry.getString("Url to Content");
				
				String[] contentUrls = urlContent.split(" ");
				
				String urlPost = postEntry.getString("Url to Post");
				String urlThumbnail = postEntry.getString("Url to Thumbnail");
				boolean isSaved = postEntry.getBoolean("Is Saved");
				boolean isNsfw = postEntry.getBoolean("Is NSFW");
				int number = postEntry.getInt("Number");

				Startup.addPost(new Post(postName, author, subreddit, contentUrls, urlPost, urlThumbnail, (long) dateInSec, isSaved, isNsfw, number));
			}

			catch(JSONException e)
			{
				e.printStackTrace();
				break;
			}
			
			catch(Exception e)
			{
				break;
			}
		}
	}
}
