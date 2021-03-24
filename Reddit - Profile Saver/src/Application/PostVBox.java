package Application;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import Application.Gif.Animation;
import Application.Gif.Gif;
import Application.Gif.GifAsMp4;
import Application.Gif.GifvToGifConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

public class PostVBox extends VBox
{
	private HBox mPostTags;
	private VBox mPostInfo;
	private Node mContent;
	private Hyperlink mTitle, mSubreddit, mAuthor;
	private Label mDate, mNum, mNsfw, mSaved, mGif;

	private final Post mPost;
	private Animation mAnimation;
	private GifAsMp4 mGifMp4;

	public PostVBox(Post post, int num)
	{
		super();

		if(post == null)
		{
			throw new NullPointerException("Null Post passed in.");
		}

		mAnimation = null;
		createPostInfo();

		mContent = new ImageView();
//		mThumbnail.fitWidthProperty().bind(widthProperty().multiply(0.75));
		((ImageView) mContent).setFitWidth(500);
		((ImageView) mContent).setSmooth(false);
		((ImageView) mContent).setPreserveRatio(true);
		mContent.setCursor(Cursor.HAND);

		mPost = post;
		updatePost(num);
		setBackground(new Background(new BackgroundFill(Color.web("#393939"), CornerRadii.EMPTY, Insets.EMPTY)));

		getChildren().addAll(mTitle, mContent, mPostInfo);
		setSpacing(5);
		setAlignment(Pos.TOP_CENTER);
		setPadding(new Insets(5, 5, 5, 5));
	}

	private void createPostInfo()
	{
		mPostTags = new HBox();
		mPostTags.setSpacing(10);

		mTitle = new Hyperlink("Title");
		mSubreddit = new Hyperlink("Subreddit");
		mAuthor = new Hyperlink("Author");

		mNum = new Label("Num");
		mDate = new Label("Date");
		mNsfw = new Label("NSFW");
		mSaved = new Label("Saved");
		mGif = new Label("Gif");

		mTitle.setPadding(Insets.EMPTY);
		mSubreddit.setPadding(Insets.EMPTY);
		mAuthor.setPadding(Insets.EMPTY);

		mTitle.setFont(new Font("Arial", 32));
		mSubreddit.setFont(new Font("Arial", 18));
		mAuthor.setFont(new Font("Arial", 18));
		mNum.setFont(new Font("Arial", 12));
		mDate.setFont(new Font("Arial", 12));
		mNsfw.setFont(new Font("Arial", 25));
		mSaved.setFont(new Font("Arial", 25));
		mGif.setFont(new Font("Arial", 25));

		mTitle.setTextFill(Color.WHITE);
		mSubreddit.setTextFill(Color.WHITE);
		mAuthor.setTextFill(Color.WHITE);
		mNum.setTextFill(Color.WHITE);
		mDate.setTextFill(Color.WHITE);
		mNsfw.setTextFill(Color.RED);
		mSaved.setTextFill(Color.LIME);
		mGif.setTextFill(Color.LIGHTBLUE);

//		mTitle.setPrefWidth(800);
//		mSubreddit.setPrefWidth(657);
//		mAuthor.setPrefWidth(657);
//		mDate.setPrefWidth(657);
//		mPostTags.setPrefWidth(657);

//		mTitle.setPrefHeight(37);
//		mSubreddit.setPrefHeight(25);
//		mAuthor.setPrefHeight(25);
//		mDate.setPrefHeight(21);

		mPostInfo = new VBox(mSubreddit, mAuthor, mNum, mDate, mPostTags);
		mPostInfo.setSpacing(5);
	}

	private void updatePost(int num)
	{
		updateHyperlink(mTitle, mPost.getTitle(), mPost.getPostUrl());
		updateHyperlink(mSubreddit, mPost.getSubreddit(), "https://www.reddit.com/r/" + mPost.getSubreddit() + "/");
		updateHyperlink(mAuthor, mPost.getAuthor(), "https://www.reddit.com/user/" + mPost.getAuthor() + "/");

		mDate.setText(mPost.getDate());
		mNum.setText(num + "");

		mPostTags.getChildren().clear();
		if(mPost.isNsfw())
		{
			mPostTags.getChildren().add(mNsfw);
		}

		if(mPost.isSaved())
		{
			mPostTags.getChildren().add(mSaved);
		}

		if(mPost.isGif())
		{
			mPostTags.getChildren().add(mGif);
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
			int index = item.isGallery() ? 1 : 0;

			String contentUrl = item.getContentUrls()[index];
			
			if(contentUrl.contains(".gifv"))
			{
				VBox contentBox = new VBox();
				contentBox.setAlignment(Pos.CENTER);
				
				ImageView thumbnail = new ImageView(item.getThumbnailUrl());
				thumbnail.setFitWidth(500);
				thumbnail.setPreserveRatio(true);
				thumbnail.setCursor(Cursor.HAND);
				
				HBox btnBox = new HBox();
				btnBox.setAlignment(Pos.CENTER);
				
				Button playBtn = new Button("Play");
				btnBox.getChildren().add(playBtn);
				playBtn.setOnAction((event) ->
				{
					if(mAnimation.isPlaying())
					{
						if(contentBox.getChildren().contains(mAnimation.getImageView()))
						{
							mAnimation.pause();
						}
						
						else
						{
							mGifMp4.playOrPause();
						}
						
						playBtn.setText("Play");
					}
					
					else
					{
						if(contentBox.getChildren().contains(mAnimation.getImageView()))
						{
							mAnimation.play();
						}
						
						else
						{
							mGifMp4.playOrPause();
						}

						playBtn.setText("Pause");
					}
				});
				
				Button mp4LoadBtn = new Button("Mp4 Load");
				btnBox.getChildren().add(mp4LoadBtn);
				mp4LoadBtn.setOnAction((event) ->
				{
					mGifMp4 = new GifAsMp4(contentUrl.replace("gifv", "mp4"));

					if(mAnimation != null)
					{
						contentBox.getChildren().remove(mAnimation.getImageView());
					}
					contentBox.getChildren().remove(thumbnail);
					contentBox.getChildren().add(mGifMp4);
				});
				
				Button gifLoad = new Button("Alt Gif Load");
				btnBox.getChildren().add(gifLoad);
				gifLoad.setOnAction((event) ->
				{
					if(mAnimation == null)
					{
						GifvToGifConverter converter = new GifvToGifConverter();
						mAnimation = converter.convert(contentUrl.replace("gifv", "mp4"));
						
						if(mAnimation == null)
						{
							return;
						}
						
						mAnimation.getImageView().setFitWidth(500);
						mAnimation.getImageView().setPreserveRatio(true);
						mAnimation.getImageView().setCursor(Cursor.HAND);

						if(mGifMp4 != null)
						{
							contentBox.getChildren().remove(mGifMp4);
						}
						contentBox.getChildren().remove(thumbnail);
						contentBox.getChildren().add(mAnimation.getImageView());
					}
				});

				contentBox.getChildren().add(btnBox);
				contentBox.getChildren().add(thumbnail);

				mContent = contentBox;
				return;
			}
			
			else if(contentUrl.contains(".gif"))
			{
				VBox contentBox = new VBox();
				contentBox.setAlignment(Pos.CENTER);
				
				mAnimation = new Gif(contentUrl);

				mAnimation.getImageView().setFitWidth(500);
				mAnimation.getImageView().setPreserveRatio(true);
				mAnimation.getImageView().setCursor(Cursor.HAND);
				
				Button playBtn = new Button("Play");
				playBtn.setOnAction((event) ->
				{
					if(mAnimation.isPlaying())
					{
						mAnimation.pause();
						playBtn.setText("Play");
					}
					
					else
					{
						mAnimation.play();
						playBtn.setText("Pause");
					}
				});

				contentBox.getChildren().add(playBtn);
				contentBox.getChildren().add(mAnimation.getImageView());

				mContent = contentBox;
				
				return;
			}

			((ImageView) mContent).setImage(new Image(contentUrl));
			mContent.setOnMouseClicked((event) ->
			{
				if(event.getButton() == MouseButton.PRIMARY)
				{
					Startup.loadUrl(item.getContentUrls()[0]);
				}
			});

			MenuItem openBrowserMenu = new MenuItem("Open in Browser");
			openBrowserMenu.setOnAction((event) -> openUrl(item.getContentUrls()[0]));
			ContextMenu contextMenu = new ContextMenu(openBrowserMenu);

			mContent.setOnContextMenuRequested((event) -> contextMenu.show(mContent, event.getScreenX(), event.getScreenY()));
		}

		catch(IllegalArgumentException e)
		{
//			System.out.println("Invalid Thumbnail Url");
			((ImageView) mContent).setImage(null);
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
