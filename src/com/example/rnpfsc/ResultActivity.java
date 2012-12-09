package com.example.rnpfsc;

import java.util.Random;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends Activity {

	public static final String WIN = "You WIN!!! :D";
	public static final String LOSE = "You lose... =\\";
	public static final String TIE = "Tie!!!";
	public static final String ERROR = "Error!!!";
	String yourSelection = null;
	
	static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
	Session.StatusCallback statusCallback1 = new SessionStatusCallback1();
	GraphUser testuser;
	String name = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
				
		Intent intent = getIntent();
		yourSelection = intent.getStringExtra(MainActivity.SEND_MESSAGE);
		
		Session session = Session.getActiveSession();
		if (session.isClosed()){
			setResults();
		}
		else {
			GetFbAccess();
		}
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
			return TIE;
		}

		if (you.equalsIgnoreCase("rock")) {
			if (them.equalsIgnoreCase("scissors")) {
				return WIN;
			} else if (them.equalsIgnoreCase("paper")) {
				return LOSE;
			} else {
				return ERROR;
			}

		} else if (you.equalsIgnoreCase("paper")) {
			if (them.equalsIgnoreCase("scissors")) {
				return LOSE;
			} else if (them.equalsIgnoreCase("rock")) {
				return WIN;
			} else {
				return ERROR;
			}

		} else if (you.equalsIgnoreCase("scissors")) {
			if (them.equalsIgnoreCase("rock")) {
				return LOSE;
			} else if (them.equalsIgnoreCase("paper")) {
				return WIN;
			} else {
				return ERROR;
			}

		} else {
			return ERROR;
		}
	}
	
	public void GetFbAccess()
	{
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
			//Request.executeBatchAndWait(request);
		}
		else {
			testuser = null;
		}
		
		Session.openActiveSession(this, true, statusCallback1);
	}
	
	public void setResults(){
		TextView result = (TextView) findViewById(R.id.result);
		
		// TODO read the other person's NFC instead
		int randomSelection = new Random().nextInt(3);
		String theirSelection;
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

		if(testuser != null){
			result.setText(testuser.getFirstName() + ", " + calculateWinner(yourSelection, theirSelection));
		}
		else {
			result.setText(calculateWinner(yourSelection, theirSelection));
		}
		

		ImageView youImg = (ImageView) findViewById(R.id.you);
		setImage(youImg, yourSelection);

		ImageView themImg = (ImageView) findViewById(R.id.them);
		setImage(themImg, theirSelection);
	}
	
	private class SessionStatusCallback1 implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			//setResults();
		}
	}
}
