package Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

// To stop the python command from opening the Windows Store: https://stackoverflow.com/questions/58754860/cmd-opens-window-store-when-i-type-python
public class Startup extends Application
{
	private static Stage mStage;
	private static Controller mController;

	private static ArrayList<PostParserThread> mParserThreads;
	private static ArrayList<Node> mPrevPosts;
	private static ArrayList<Node> mCurrPosts;
	private static ArrayList<Node> mNextPosts;
	private static ArrayList<Post> mAllPosts, mPostsToUse;
	private static int mCurrPageNum, mTotalPageNum;
	private static String mSubredditFilter, mKeywordFilter, mAuthorFilter;
	private static boolean mIsSaved, mIsShuffle;
	private static ContentRating mRating;
	private static boolean mStopAll;

	private static Thread mPrevPostThread;
	private static boolean mPrevPostIsOn;
	private static Thread mNextPostThread;
	private static boolean mNextPostIsOn;
	private static boolean mStopRetrievingPosts;

	private static final int POSTS_PER_PAGE = 1000;

	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(Startup.class.getResource("/Application/View.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);

			mController = loader.getController();

//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setTitle("Profile Saver for Reddit");
//			primaryStage.getIcons().add(new Image(getClass().getResource("/Resources/Images/unknown_pack.png").toExternalForm()));
			primaryStage.setMinWidth(1296);
			primaryStage.setMinHeight(759);
			primaryStage.show();

//			System.out.println(primaryStage.getWidth() + " - " + primaryStage.getHeight());
			mStage = primaryStage;
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void stop()
	{
		mStopAll = true;
		
		for(PostParserThread thread : mParserThreads)
		{
			thread.interrupt();
		}
		
		if(mNextPostIsOn && mNextPostThread != null)
		{
			try
			{
				mNextPostThread.join();
			}

			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		if(mPrevPostIsOn && mPrevPostThread != null)
		{
			try
			{
				mPrevPostThread.join();
			}

			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		mController.stopTimer();
	}

	public static void main(String[] args) throws Exception
	{
		mParserThreads = new ArrayList<PostParserThread>();
		mPrevPosts = new ArrayList<Node>();
		mCurrPosts = new ArrayList<Node>();
		mNextPosts = new ArrayList<Node>();

		mAllPosts = new ArrayList<Post>();
		mPostsToUse = new ArrayList<Post>();

		mSubredditFilter = "";
		mKeywordFilter = "";
		mAuthorFilter = "";
		mCurrPageNum = 0;
		mTotalPageNum = 1;
		mRating = ContentRating.All;

		mStopAll = false;
		mNextPostIsOn = false;
		mPrevPostIsOn = false;
		mStopRetrievingPosts = false;

		launch(args);
	}

	public static void selectFile()
	{
		File dataDir = new File(System.getProperty("user.dir") + "/Data/");
		FileChooser chooser = new FileChooser();
		chooser.setInitialDirectory(dataDir);
		chooser.setTitle("Choose A Upvote Storage File");
		chooser.getExtensionFilters().add(new ExtensionFilter("Json", "*.json"));

		File upvoteFile = chooser.showOpenDialog(mStage);

		if(upvoteFile != null)
		{
			Thread loadUpvotes = new Thread(() ->
			{
				loadUpvoteFile(upvoteFile);
				mController.finishedSelectingFile(true);
			});

			loadUpvotes.start();
		}

		else
		{
			mController.finishedSelectingFile(false);
		}
	}

	private static void loadUpvoteFile(File upvoteFile)
	{
		if(upvoteFile.exists())
		{
			try
			{
				String fileStr = "";
				fileStr = readFile(upvoteFile);

				PostsQueue queue = new PostsQueue();
				JSONObject json = new JSONObject(fileStr);
				@SuppressWarnings("unchecked")
				Iterator<String> keys = json.keys();

				while(keys.hasNext())
				{
					JSONObject entry = json.getJSONObject(keys.next());
					queue.enqueue(entry);
				}

				parsePosts(queue);
			}

			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void parsePosts(PostsQueue queue)
	{
		mParserThreads.add(new PostParserThread(queue));
		mParserThreads.add(new PostParserThread(queue));
		mParserThreads.add(new PostParserThread(queue));
		mParserThreads.add(new PostParserThread(queue));

		for(PostParserThread thread : mParserThreads)
		{
			thread.start();
		}

		for(PostParserThread thread : mParserThreads)
		{
			try
			{
				thread.join();
			}

			catch(InterruptedException e)
			{
				System.out.println("\nINTERRUPTED\n");
				break;
			}
		}

		sortPosts(mAllPosts);
		mTotalPageNum = (mAllPosts.size() / 1000) + 1;
	}

	public synchronized static void addPost(Post toAdd)
	{
		mAllPosts.add(toAdd);
		mController.displayTxt("Post #" + mAllPosts.size() + " was loaded.");
	}

	private static String readFile(File upvoteFile)
	{
		StringBuilder sb = new StringBuilder();
		String content = "";

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(upvoteFile));

			String line;
			while((line = br.readLine()) != null)
			{
				sb.append(line + "\n");
			}

			br.close();

			content = sb.toString();
		}

		catch(Exception e)
		{
			e.printStackTrace();
		}

		return content;
	}

	public static ArrayList<Node> getPosts()
	{
		return mCurrPosts;
	}

	public static int getCurrPageNum()
	{
		return mCurrPageNum;
	}

	public static int getTotalPageNum()
	{
		return mTotalPageNum;
	}

	public static void loadUrl(String url)
	{
		mController.loadFreshUrl(url);
	}

	public static void applyFilter(String subreddit, String keyword, String author, int rating, boolean isSaved, boolean isShuffle)
	{
		mSubredditFilter = subreddit == null ? "" : subreddit;
		mKeywordFilter = keyword == null ? "" : keyword;
		mAuthorFilter = author == null ? "" : author;
		mIsSaved = isSaved;
		
		mStopRetrievingPosts = true;
		waitForPostThreads();
		mStopRetrievingPosts = false;

		switch(rating)
		{
			case 0:
				mRating = ContentRating.NSFW;
				break;

			case 1:
				mRating = ContentRating.All;
				break;

			default:
				mRating = ContentRating.SFW;
				break;
		}
		
		findValidPosts();

		if(!isShuffle && mIsShuffle)
		{
			sortPosts(mPostsToUse);
		}

		mIsShuffle = isShuffle;

		if(mIsShuffle)
		{
			shuffle();
		}
	}

	public static void findValidPosts()
	{
		mPostsToUse.clear();

		for(Post post : mAllPosts)
		{
			if(doesPostMatchFilter(post))
			{
				mPostsToUse.add(post);
			}
		}
	}

	private static void shuffle()
	{
		Collections.shuffle(mPostsToUse);
	}

	private static void sortPosts(ArrayList<Post> posts)
	{
		posts.sort(new Comparator<Post>()
		{
			@Override
			public int compare(Post o1, Post o2)
			{
				return -1 * o1.compareTo(o2);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Node> goToPage(int pageNum)
	{
		if(pageNum < 1 || pageNum > mTotalPageNum)
		{
			return null;
		}
		
		waitForPostThreads();

		int ogPageNum = mCurrPageNum;
		mCurrPageNum = pageNum;
		calculateTotalPageNum();
		System.out.println("\nPAGE: " + mCurrPageNum);

		if(mCurrPageNum == 1 && mPrevPosts.isEmpty()) // First Time Loading
		{
			System.out.println("Load First Page");
			mCurrPosts.clear();
			addPosts(mCurrPosts, (mCurrPageNum - 1) * POSTS_PER_PAGE);
			retrieveNextPosts();
		}
		
		else if(mCurrPageNum - 1 == ogPageNum) // Next Page
		{
			System.out.println("Load Next Page");
			mPrevPosts = (ArrayList<Node>) mCurrPosts.clone();
			mCurrPosts = (ArrayList<Node>) mNextPosts.clone();
			retrieveNextPosts();
		}
		
		else if(mCurrPageNum + 1 == ogPageNum) // Prev Page
		{
			System.out.println("Load Prev Page");
			mNextPosts = (ArrayList<Node>) mCurrPosts.clone();
			mCurrPosts = (ArrayList<Node>) mPrevPosts.clone();
			retrievePrevPosts();
		}
		
		else // Manual Page Selection
		{
			System.out.println("Load Manually Chosen Page");
			mCurrPosts.clear();
			addPosts(mCurrPosts, (mCurrPageNum - 1) * POSTS_PER_PAGE);
			retrieveNextPosts();
			retrievePrevPosts();
		}

		return mCurrPosts;
	}

	private static void waitForPostThreads()
	{
		if(mNextPostIsOn)
		{
			try
			{
				System.out.println("Waiting on Next Page Loading");
				mNextPostThread.join();
			}

			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
		
		if(mPrevPostIsOn)
		{
			try
			{
				System.out.println("Waiting on Prev Page Loading");
				mPrevPostThread.join();
			}

			catch(Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
	}

	private static void retrievePrevPosts()
	{
		if(mCurrPageNum < 1)
		{
			return;
		}

		mPrevPosts.clear();

		mPrevPostThread = new Thread(() ->
		{
			addPosts(mPrevPosts, (mCurrPageNum - 2) * POSTS_PER_PAGE);
			System.out.println("PREV: Got Page " + (mCurrPageNum - 1) + " posts.");
			mPrevPostIsOn = false;
		});

		mPrevPostThread.start();
		mPrevPostIsOn = true;
	}

	private static void retrieveNextPosts()
	{
		mNextPosts.clear();
		
		mNextPostThread = new Thread(() ->
		{
			addPosts(mNextPosts, mCurrPageNum * POSTS_PER_PAGE);
			System.out.println("NEXT: Got Page " + (mCurrPageNum + 1) + " posts.");
			mNextPostIsOn = false;
		});

		mNextPostThread.start();
		mNextPostIsOn = true;
	}

	private static void calculateTotalPageNum()
	{
		int validPostCount = 0;

		for(Post post : mPostsToUse)
		{
			if(doesPostMatchFilter(post))
			{
				validPostCount++;
			}
		}

		mTotalPageNum = (validPostCount / POSTS_PER_PAGE) + 1;
	}

	private static void addPosts(ArrayList<Node> postAra, int postIndex)
	{
		int addedPosts = 0;
		System.out.println("POST START INDEX: " + postIndex + " - POST ENDING INDEX: " + (postIndex + POSTS_PER_PAGE));

		while(addedPosts < POSTS_PER_PAGE)
		{
			if(mStopAll || mStopRetrievingPosts)
			{
				break;
			}

			else if(postIndex >= mPostsToUse.size() || postIndex < 0)
			{
				break;
			}

			Post curr = mPostsToUse.get(postIndex);
			postAra.add(new PostHBox(curr));

			postIndex++;
			addedPosts++;
			mController.displayTxt("Post #" + addedPosts + " was added.");
		}
	}

	private static boolean doesPostMatchFilter(Post post)
	{
		if(mSubredditFilter.compareTo("") != 0 && !post.getSubreddit().toLowerCase().contains(mSubredditFilter.toLowerCase()))
		{
			return false;
		}

		if(mKeywordFilter.compareTo("") != 0 && !post.getTitle().toLowerCase().contains(mKeywordFilter.toLowerCase()))
		{
			return false;
		}

		if(mAuthorFilter.compareTo("") != 0 && mAuthorFilter.toLowerCase().compareTo(post.getAuthor().toLowerCase()) != 0)
		{
			return false;
		}

		switch(mRating)
		{
			case NSFW:
				if(!post.isNsfw())
				{
					return false;
				}
				break;

			case All:

				break;

			default:
				if(post.isNsfw())
				{
					return false;
				}
				break;
		}

		if(mIsSaved && !post.isSaved())
		{
			return false;
		}

		return true;
	}

	public static void resetPosts()
	{
		mCurrPosts.clear();
		mCurrPageNum = 1;
	}
	
	public static boolean isStopped()
	{
		return mStopAll;
	}
}
