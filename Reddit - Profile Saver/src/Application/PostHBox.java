package Application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class PostHBox extends HBox
{
	private HBox mPostTags;
	private VBox mPostInfo;
	private ImageView mThumbnail;
	private Hyperlink mTitle, mSubreddit, mAuthor;
	private Label mDate, mNsfw, mSaved;

	private final Post mPost;

	public PostHBox(Post post)
	{
		super();

		if(post == null)
		{
			throw new NullPointerException("Null Post passed in.");
		}

		createPostInfo();

		mThumbnail = new ImageView();
		mThumbnail.setFitHeight(140);
		mThumbnail.setFitWidth(140);
		mThumbnail.setPreserveRatio(true);
		mThumbnail.setCursor(Cursor.HAND);

		mPost = post;
		updatePost();
		setBackground(new Background(new BackgroundFill(Color.web("#393939"), CornerRadii.EMPTY, Insets.EMPTY)));
		setMinHeight(140);

		getChildren().addAll(mThumbnail, mPostInfo);
		setSpacing(5);
	}

	private void createPostInfo()
	{
		mPostTags = new HBox();
		mPostTags.setSpacing(10);

		mTitle = new Hyperlink("Title");
		mSubreddit = new Hyperlink("Subreddit");
		mAuthor = new Hyperlink("Author");

		mDate = new Label("Date");
		mNsfw = new Label("NSFW");
		mSaved = new Label("Saved");

		mTitle.setPadding(Insets.EMPTY);
		mSubreddit.setPadding(Insets.EMPTY);
		mAuthor.setPadding(Insets.EMPTY);

		mTitle.setFont(new Font("Arial", 32));
		mSubreddit.setFont(new Font("Arial", 18));
		mAuthor.setFont(new Font("Arial", 18));
		mDate.setFont(new Font("Arial", 12));
		mNsfw.setFont(new Font("Arial", 25));
		mSaved.setFont(new Font("Arial", 25));

		mTitle.setTextFill(Color.WHITE);
		mSubreddit.setTextFill(Color.WHITE);
		mAuthor.setTextFill(Color.WHITE);
		mDate.setTextFill(Color.WHITE);
		mNsfw.setTextFill(Color.RED);
		mSaved.setTextFill(Color.LIME);

		mTitle.setPrefWidth(800);
		mSubreddit.setPrefWidth(657);
		mAuthor.setPrefWidth(657);
		mDate.setPrefWidth(657);
		mPostTags.setPrefWidth(657);

		mTitle.setPrefHeight(37);
		mSubreddit.setPrefHeight(25);
		mAuthor.setPrefHeight(25);
		mDate.setPrefHeight(21);

		mPostInfo = new VBox(mTitle, mSubreddit, mAuthor, mDate, mPostTags);
	}

	private void updatePost()
	{
		updateHyperlink(mTitle, mPost.getTitle(), mPost.getPostUrl());
		updateHyperlink(mSubreddit, mPost.getSubreddit(), "https://www.reddit.com/r/" + mPost.getSubreddit() + "/");
		updateHyperlink(mAuthor, mPost.getAuthor(), "https://www.reddit.com/user/" + mPost.getAuthor() + "/");

		mDate.setText(mPost.getDate());

		mPostTags.getChildren().clear();
		if(mPost.isNsfw())
		{
			mPostTags.getChildren().add(mNsfw);
		}

		if(mPost.isSaved())
		{
			mPostTags.getChildren().add(mSaved);
		}

		if(mPost.hasThumbnail())
		{
			updateThumbnail(mPost);
		}
	}

	private void updateHyperlink(Hyperlink hyperlink, String txt, String url)
	{
		hyperlink.setText(txt);
		Tooltip tip = new Tooltip(url);
		tip.setFont(new Font("Arial", 12));
		hyperlink.setTooltip(tip);

		hyperlink.setOnAction((event) -> Startup.loadUrl(url));

		MenuItem openBrowserMenu = new MenuItem("Open in Browser");
		openBrowserMenu.setOnAction((event) -> openUrl(url));
		hyperlink.setContextMenu(new ContextMenu(openBrowserMenu));
	}

	private void updateThumbnail(Post item)
	{
		try
		{
			mThumbnail.setImage(new Image(item.getThumbnailUrl()));
			mThumbnail.setOnMouseClicked((event) ->
			{
				if(event.getButton() == MouseButton.PRIMARY)
				{
					Startup.loadUrl(item.getContentUrls()[0]);
				}
			});

			MenuItem openBrowserMenu = new MenuItem("Open in Browser");
			openBrowserMenu.setOnAction((event) -> openUrl(item.getContentUrls()[0]));
			ContextMenu contextMenu = new ContextMenu(openBrowserMenu);

			mThumbnail.setOnContextMenuRequested((event) -> contextMenu.show(mThumbnail, event.getScreenX(), event.getScreenY()));
		}

		catch(IllegalArgumentException e)
		{
//			System.out.println("Invalid Thumbnail Url");
			mThumbnail.setImage(null);
		}
	}

	private void openUrl(String url)
	{
		try
		{
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

			if(desktop != null)
			{
				desktop.browse(new URL(url).toURI());
			}
		}

		catch(IOException | URISyntaxException e)
		{
			e.printStackTrace();
		}
	}
}
