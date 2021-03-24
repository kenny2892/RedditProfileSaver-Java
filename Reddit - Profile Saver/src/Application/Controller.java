package Application;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

public class Controller
{
	@FXML private VBox mPostsBox, mPageNumBox;
	@FXML private ScrollPane mPostScroll;
	@FXML private ScrollBar mPostScrollBar;
	@FXML private WebView mWebView;
	@FXML private Button mCloseWebBtn;
	@FXML private Slider mRatingSlider, mZoomSlider;
	@FXML private VBox mSettingsBox, mWebSetBox;
	@FXML private TextField mSubredditTxtField, mKeywordTxtField, mAuthorTxtField, mWebUrl, mPageNumField;
	@FXML private CheckBox mSavedCheckbox, mShuffleCheckbox;
	@FXML private Pane mMainPane;
	@FXML private Rectangle mLoadingBg;
	@FXML private Label mLoadingLbl;
	@FXML private TextArea mLoadingTxtArea;
	@FXML private ToggleButton mTimerBtn;
	@FXML private Text mTimerTxt, mCurrPageNum, mTotalPageNum;

	private final double SPEED = 6;

	private ArrayList<String> mUrlHistory;
	private int mCurrUrlIndex;

	private boolean mIsTimerOn;
	private int mTimerMaxSeconds;
	private int mSecondsRemaining;
	private Timer mCountDown;
	private TimerTask mTimerTask;
	private boolean mTurnOff;

	private BooleanProperty mShowLoading;
	private StringProperty mLoadingResults;

	public void initialize()
	{
//		Bindings.bindContentBidirectional(Startup.getPosts(), mPostsBox.getChildren());
		mPostsBox.setOnScroll(new EventHandler<ScrollEvent>()
		{
			@Override
			public void handle(ScrollEvent event)
			{
				double deltaY = event.getDeltaY() * SPEED; // *6 to make the scrolling a bit faster
				double height = mPostScroll.getContent().getBoundsInLocal().getHeight();
				double vvalue = mPostScroll.getVvalue();
				mPostScroll.setVvalue(vvalue + -deltaY / height); // deltaY/width to make the scrolling equally fast regardless of the actual
			}
		});

		mSettingsBox.setVisible(false);

		mPostScroll.prefHeightProperty().bind(mMainPane.heightProperty());
		mPostScroll.prefWidthProperty().bind(mMainPane.widthProperty());

		mWebView.prefHeightProperty().bind(mMainPane.heightProperty());
		mWebView.prefWidthProperty().bind(mMainPane.widthProperty());

		mWebView.setVisible(false);
		mWebSetBox.setVisible(false);

		mZoomSlider.valueChangingProperty().addListener((observable, oldValue, newValue) -> zoomWebBrowser());

		mIsTimerOn = false;
		mTimerMaxSeconds = 1800;
		mSecondsRemaining = mTimerMaxSeconds;
		timerOff();
		updateTimer();
		mTurnOff = false;

		mCurrUrlIndex = 0;
		mUrlHistory = new ArrayList<String>();

		mShowLoading = new SimpleBooleanProperty(false);

		mLoadingBg.heightProperty().bind(mMainPane.heightProperty());
		mLoadingBg.widthProperty().bind(mMainPane.widthProperty());
		mLoadingBg.visibleProperty().bind(mShowLoading);

		mLoadingLbl.prefWidthProperty().bind(mMainPane.widthProperty());
		mLoadingLbl.visibleProperty().bind(mShowLoading);

		mLoadingResults = new SimpleStringProperty("");
		mLoadingTxtArea.textProperty().bind(mLoadingResults);
		mLoadingTxtArea.prefHeightProperty().bind(mMainPane.heightProperty().subtract((17 * 7) - 7));
		mLoadingTxtArea.prefWidthProperty().bind(mMainPane.widthProperty().subtract(20));
		mLoadingTxtArea.visibleProperty().bind(mShowLoading);

		mLoadingResults.addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				mLoadingTxtArea.selectPositionCaret(mLoadingResults.get().length());
				mLoadingTxtArea.deselect();
			}
		});

		mPageNumBox.setVisible(false);
	}

	private void startTimer()
	{
		stopTimer();

		mTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if(mTurnOff)
				{
					cancel();
				}

				mSecondsRemaining--;
				updateTimer();

				if(mSecondsRemaining <= 0)
				{
					retrieveUpvotes();
					cancel();
				}
			}
		};

		mCountDown = new Timer();
		mCountDown.schedule(mTimerTask, 0, 1000);
	}

	private void updateTimer()
	{
		int sec = mSecondsRemaining % 60;
		int hr = mSecondsRemaining / 60;
		int min = hr % 60;
		hr /= 60;

		mTimerTxt.setText(String.format("%d:%02d:%02d", hr, min, sec));
	}

	public void stopTimer()
	{
		if(mTimerTask != null)
		{
			mTimerTask.cancel();
			mTimerTask = null;
		}

		if(mCountDown != null)
		{
			mCountDown.cancel();
			mCountDown = null;
		}
	}

	@FXML
	private void selectFile()
	{
		mShowLoading.set(true);
		Startup.selectFile();
	}
	
	public void displayTxt(String txt)
	{
		Platform.runLater(() ->
		{
			mLoadingResults.set(mLoadingResults.get() + "\n" + txt);
		});
	}
	
	public void finishedSelectingFile(boolean wasSuccess)
	{
		Platform.runLater(() ->
		{
			if(wasSuccess)
			{
				Startup.findValidPosts();
				goToPage(1, () ->
				{
					mSettingsBox.setVisible(true);
					mPageNumBox.setVisible(true);
					mShowLoading.set(false);
					mLoadingResults.set("");
				});
			}
			
			else
			{
				mShowLoading.set(false);
				mLoadingResults.set("");
			}
		});
	}

	@FXML
	private void retrieveUpvotes()
	{
		mShowLoading.set(true);

		Thread runScript = new Thread(() ->
		{
			try
			{
				mLoadingResults.setValue("");

				String scriptPath = System.getProperty("user.dir") + "/Data/Reddit.py";

				String resultsPath = scriptPath.replace("Reddit.py", "Results.txt");
				File resultsFile = new File(resultsPath);

				FileWriter resultsWriter = new FileWriter(resultsFile, false);
				resultsWriter.write("");
				resultsWriter.close();

				ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
				pb.start();

				FileWatcher watcher = new FileWatcher(resultsFile, mLoadingResults, () ->
				{
					Platform.runLater(() ->
					{
						applyFilters();
						timerOff();
						mSecondsRemaining = mTimerMaxSeconds;
						updateTimer();

						mShowLoading.set(false);
						mLoadingResults.set("");
					});
				});

				watcher.run();
			}

			catch(Exception e)
			{
				System.out.println("Error occurred when trying to run Python script.");
				e.printStackTrace();
			}
		});

		runScript.start();
	}

	private void updatePagesFooter()
	{
		mCurrPageNum.setText(Startup.getCurrPageNum() + "");
		mTotalPageNum.setText(Startup.getTotalPageNum() + "");
		mPageNumField.setText(mCurrPageNum.getText());
	}

	@FXML
	private void previousPage()
	{
		mShowLoading.set(true);

		Thread prevPage = new Thread(() ->
		{
			try
			{
				Thread.sleep(100); // Have to pause to ensure the Loading Screen gets displayed
			}

			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			Platform.runLater(() ->
			{
				goToPage(Startup.getCurrPageNum() - 1, () -> mShowLoading.set(false));
			});
		});

		prevPage.start();
	}

	@FXML
	private void nextPage()
	{
		mShowLoading.set(true);

		Thread nextPage = new Thread(() ->
		{
			try
			{
				Thread.sleep(100); // Have to pause to ensure the Loading Screen gets displayed
			}

			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			Platform.runLater(() ->
			{
				goToPage(Startup.getCurrPageNum() + 1, () -> mShowLoading.set(false));
			});
		});

		nextPage.start();
	}

	@FXML
	private void customFieldPage()
	{
		mShowLoading.set(true);

		Thread customField = new Thread(() ->
		{
			try
			{
				Thread.sleep(100); // Have to pause to ensure the Loading Screen gets displayed
			}

			catch(InterruptedException e1)
			{
				e1.printStackTrace();
			}

			Platform.runLater(() ->
			{
				int pageNum = Startup.getCurrPageNum();

				try
				{
					pageNum = Integer.parseInt(mPageNumField.getText());
				}

				catch(Exception e)
				{
					System.out.println("Invalid Page Num");
					mPageNumField.setText(Startup.getCurrPageNum() + "");
					return;
				}

				goToPage(pageNum, () -> mShowLoading.set(false));
			});
		});

		customField.start();
	}

	private void goToPage(int pageNum, Runnable runWhenDone)
	{
		mPostsBox.getChildren().remove(mPageNumBox);

		if(mPostScrollBar == null)
		{
			for(Node node : mPostScroll.lookupAll(".scroll-bar"))
			{
				if(node instanceof ScrollBar)
				{
					mPostScrollBar = (ScrollBar) node;
					break;
				}
			}
		}

		if(mPostScrollBar != null)
		{
			mPostScrollBar.setValue(0);
		}

		Thread getPostsThread = new Thread(() ->
		{
			ArrayList<Node> toAdd = Startup.goToPage(pageNum);
			
			Platform.runLater(() ->
			{
				if(toAdd != null)
				{
					mPostsBox.getChildren().clear();
					mPostsBox.getChildren().addAll(toAdd);
				}
				
				mPostsBox.getChildren().add(mPageNumBox);
				updatePagesFooter();
				mLoadingResults.set("");
				
				runWhenDone.run();
			});
		});
		
		getPostsThread.start();
	}

	@FXML
	private void applyAndReload()
	{
		mShowLoading.set(true);
		Startup.resetPosts();

		Thread applyReload = new Thread(() ->
		{
			try
			{
				Thread.sleep(100); // Have to pause to ensure the Loading Screen gets displayed
			}

			catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			Platform.runLater(() ->
			{
				applyFilters();
				goToPage(1, () ->
				{
					if(mPostScrollBar != null)
					{
						mPostScrollBar.setValue(0);
					}

					mShowLoading.set(false);
				});
			});
		});

		applyReload.start();
	}

	private void applyFilters()
	{
		Startup.applyFilter(mSubredditTxtField.getText(), mKeywordTxtField.getText(), mAuthorTxtField.getText(), (int) mRatingSlider.getValue(), mSavedCheckbox.isSelected(), mShuffleCheckbox.isSelected());
	}

	@FXML
	private void zoomWebBrowser()
	{
		double zoom = mZoomSlider.getValue();
		mWebView.setZoom(zoom);
	}

	private void loadUrl(String url)
	{
		mWebView.setVisible(true);
		mWebView.getEngine().load(url);
		mWebUrl.setText(url);

		if(mCurrUrlIndex == 0)
		{
			mUrlHistory.add(url);
		}

		else
		{
			mCurrUrlIndex++;
			mUrlHistory.add(mCurrUrlIndex, url);
		}
	}

	public void loadFreshUrl(String url)
	{
		loadUrl(url);

		mSettingsBox.setVisible(false);
		mWebSetBox.setVisible(true);
		mZoomSlider.setValue(1);
		zoomWebBrowser();
	}

	@FXML
	private void loadChangedUrl()
	{
		loadUrl(mWebUrl.getText());
	}

	@FXML
	private void closeWeb()
	{
		mSettingsBox.setVisible(true);
		mWebView.setVisible(false);
		mWebSetBox.setVisible(false);
		mWebView.getEngine().load("http://google.com");
	}

	@FXML
	private void loadPreviousUrl()
	{
		if(mCurrUrlIndex - 1 >= 0)
		{
			mCurrUrlIndex--;
			loadUrl(mUrlHistory.get(mCurrUrlIndex));
		}
	}

	@FXML
	private void loadNextUrl()
	{
		if(mUrlHistory.size() < mCurrUrlIndex + 1)
		{
			mCurrUrlIndex++;
			loadUrl(mUrlHistory.get(mCurrUrlIndex));
		}
	}

	@FXML
	private void timerToggle()
	{
		mIsTimerOn = !mIsTimerOn;

		if(mIsTimerOn)
		{
			timerOn();
		}

		else
		{
			timerOff();
		}

		updateTimer();
	}

	private void timerOn()
	{
		mTimerBtn.getStyleClass().removeAll("on-button", "off-button");
		mTimerBtn.getStyleClass().add("on-button");
		mTimerBtn.setText("On");
		startTimer();
	}

	private void timerOff()
	{
		mTimerBtn.getStyleClass().removeAll("on-button", "off-button");
		mTimerBtn.getStyleClass().add("off-button");
		mTimerBtn.setText("Off");
		stopTimer();
	}
}
