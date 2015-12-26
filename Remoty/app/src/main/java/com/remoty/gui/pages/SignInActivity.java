package com.remoty.gui.pages;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.remoty.R;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

	public final static String SIGN_IN = "SIGN_IN";
	public final static int RC_SIGN_IN = 9001;

	GoogleApiClient mGoogleApiClient;

	ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

//		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//		fab.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//						.setAction("Action", null).show();
//			}
//		});

		// Configure sign-in to request the user's ID, email address, and basic
		// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.build();

		// Build a GoogleApiClient with access to the Google Sign-In API and the
		// options specified by gso.
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				buttonSignIn(v);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		// Silent sign in
		OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

		if (pendingResult.isDone()) {
			// There's immediate result available.
			handleSignInResult(pendingResult.get());
		}
		else {
			// There's no immediate result ready, displays some progress indicator and waits for the
			// async callback.
			showProgressIndicator();

			pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
				@Override
				public void onResult(GoogleSignInResult result) {

					handleSignInResult(result);
					hideProgressIndicator();
				}
			});
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		Log.d(SIGN_IN, "onConnectionFailed()");
	}

	public void buttonSignIn(View v) {

		Log.d(SIGN_IN, "buttonSignIn()");

		signIn();
	}

	private void signIn() {

		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {

			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
	}

	private void handleSignInResult(GoogleSignInResult result) {

		Log.d(SIGN_IN, "handleSignInResult(): " + result.isSuccess());

		if (result.isSuccess()) {

			GoogleSignInAccount acct = result.getSignInAccount();

			String email = acct.getEmail();
			String name = acct.getDisplayName();
			Uri photo = acct.getPhotoUrl();
			String id = acct.getId();
			String idToken = acct.getIdToken();
			String serverAuthCode = acct.getServerAuthCode();

			Log.d(SIGN_IN, "handleSignInResult(): " + email + ", " + name + ", " + photo);
			Log.d(SIGN_IN, "handleSignInResult(): " + "id: " + id);
			Log.d(SIGN_IN, "handleSignInResult(): " + "idToken: " + idToken);
			Log.d(SIGN_IN, "handleSignInResult(): " + "serverAuthCode: " + serverAuthCode);
		}
	}

	private void showProgressIndicator() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Loading...");
			mProgressDialog.setIndeterminate(true);
		}

		mProgressDialog.show();
	}

	private void hideProgressIndicator() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.hide();
		}
	}
}
