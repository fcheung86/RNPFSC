package com.example.rnpfsc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class ResultActivity extends Activity {

	Button shareButton;

	public static final String WIN = "You WIN!!! :D";
	public static final String LOSE = "You lose... =\\";
	public static final String TIE = "Tie!!!";
	public static final String ERROR = "Error!!!";
	String yourSelection = null;
	String theirSelection = null;
	String postResults = null;

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final int REAUTH_ACTIVITY_CODE = 100;
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;

	static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
	Session.StatusCallback statusCallback1 = new SessionStatusCallback1();
	GraphUser testuser;
	String name = null;
	int permissionFlag = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		
		shareButton = (Button) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				publishStory();
			}
		});

		Intent intent = getIntent();
		yourSelection = intent.getStringExtra(MainActivity.SEND_MESSAGE);
		name = intent.getStringExtra(MainActivity.USER_NAME);

		/* Session session = Session.getActiveSession();
		if (session.isClosed()) {
			setResults();
		} else {
			GetFbAccess();
		}*/
		
		setResults();
	}

	public void onSessionStateChange(SessionState state, Exception exception) {
		if (pendingPublishReauthorization && state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
			pendingPublishReauthorization = false;
			publishStory();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_result, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onPause() {
		super.onPause();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback1);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback1);
	}

	private void setImage(ImageView imageView, String selection) {
		if (selection.equalsIgnoreCase("rock")) {
			imageView.setImageResource(R.drawable.rock_base);

		} else if (selection.equalsIgnoreCase("paper")) {
			imageView.setImageResource(R.drawable.paper_base);

		} else if (selection.equalsIgnoreCase("scissors")) {
			imageView.setImageResource(R.drawable.scissors_base);

		}
	}

	private String calculateWinner(String you, String them) {
		if (you.equalsIgnoreCase(them)) {
			postResults = "tied";
			return TIE;
		}

		if (you.equalsIgnoreCase("rock")) {
			if (them.equalsIgnoreCase("scissors")) {
				postResults = "won";
				return WIN;
			} else if (them.equalsIgnoreCase("paper")) {
				postResults = "lost";
				return LOSE;
			} else {
				return ERROR;
			}

		} else if (you.equalsIgnoreCase("paper")) {
			if (them.equalsIgnoreCase("scissors")) {
				postResults = "lost";
				return LOSE;
			} else if (them.equalsIgnoreCase("rock")) {
				postResults = "won";
				return WIN;
			} else {
				return ERROR;
			}

		} else if (you.equalsIgnoreCase("scissors")) {
			if (them.equalsIgnoreCase("rock")) {
				postResults = "lost";
				return LOSE;
			} else if (them.equalsIgnoreCase("paper")) {
				postResults = "won";
				return WIN;
			} else {
				return ERROR;
			}

		} else {
			return ERROR;
		}
	}

	/* public void GetFbAccess() {
		Session session = Session.getActiveSession();

		if (session.isOpened()) {
			Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {
					if (user != null) {
						testuser = user;
						name = user.getName();
						setResults();
					}
				}
			});

			Request.executeBatchAsync(request);
			
		} else {
			testuser = null;
		}

		Session.openActiveSession(this, true, statusCallback1);
	} */

	public void setResults() {
		Session session = Session.getActiveSession();
		TextView result = (TextView) findViewById(R.id.result);

		// TODO read the other person's NFC instead
		int randomSelection = new Random().nextInt(3);

		switch (randomSelection) {
		case 0:
			theirSelection = "Rock";
			break;
		case 1:
			theirSelection = "Paper";
			break;
		case 2:
			theirSelection = "Scissors";
			break;
		default:
			theirSelection = "ERROR";
			break;
		}

		if (name != null) {
			result.setText(name + ", " + calculateWinner(yourSelection, theirSelection));
		} else {
			result.setText(calculateWinner(yourSelection, theirSelection));
		}

		ImageView youImg = (ImageView) findViewById(R.id.you);
		setImage(youImg, yourSelection);

		ImageView themImg = (ImageView) findViewById(R.id.them);
		setImage(themImg, theirSelection);

		if (session.isOpened()) {
			shareButton.setVisibility(View.VISIBLE);
		}
	}

	private void publishStory() {
		Session session = Session.getActiveSession();
		
		if (session != null) {

			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.ReauthorizeRequest reauthRequest = new Session.ReauthorizeRequest(this, PERMISSIONS).setRequestCode(REAUTH_ACTIVITY_CODE);
				session.reauthorizeForPublish(reauthRequest);
				permissionFlag = 1;
				return;
			}

			Bundle postParams = new Bundle();
			postParams.putString("name", "RNPFSC");
			postParams.putString("caption", name + " " + postResults + " against computer at RNPFSC");
			postParams.putString("description", name + "'s " + yourSelection + " " + postResults +  " against computer's " + theirSelection);
			postParams.putString("link", "https://developers.facebook.com/android");
			postParams.putString("picture", "https://dl.dropbox.com/s/0ik06x1upecxrb3/ic_launcher.png");

			Request request = new Request(session, "me/feed", postParams, HttpMethod.POST);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REAUTH_ACTIVITY_CODE) {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	private class SessionStatusCallback1 implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if (permissionFlag == 1){
				onSessionStateChange(state,exception);
			}	
		}
	}
}
