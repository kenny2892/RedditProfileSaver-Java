package Application;

import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONObject;

public class PostsQueue
{
	Queue<JSONObject> mPosts;
	
	public PostsQueue()
	{
		mPosts = new LinkedList<JSONObject>(); 
	}

	public synchronized void enqueue(JSONObject post)
	{
		if(post == null)
		{
			throw new NullPointerException("Passed in \"post\" argument was null.");
		}
		
		mPosts.add(post);
	}

	public synchronized JSONObject dequeue()
	{
		if(mPosts.isEmpty())
		{
			return null;
		}

		return mPosts.remove();
	}
}
