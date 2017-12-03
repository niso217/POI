package com.benezra.nir.poi.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.benezra.nir.poi.Fragment.ProgressDialogFragment;
import com.benezra.nir.poi.Helper.SharePref;
import com.benezra.nir.poi.Login.ResetPasswordActivity;
import com.benezra.nir.poi.Login.SignupActivity;
import com.benezra.nir.poi.Interface.LoginCallBackInterface;
import com.benezra.nir.poi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.benezra.nir.poi.Interface.Constants.ID_TOKEN;
import static com.benezra.nir.poi.Interface.Constants.NOTIFY_TOKEN;

public class SignInActivity extends AppCompatActivity implements LoginCallBackInterface, View.OnClickListener {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;
    AnimationDrawable animationDrawable;
    ScrollView scrollView;
    private FirebaseDatabase mFirebaseInstance;
    private static final String TAG = SignInActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance();

        if (mAuth.getCurrentUser() != null)
            saveUserToFireBase();
        else {
            // set the view now
            setContentView(R.layout.activity_login);


            inputEmail = (EditText) findViewById(R.id.email);
            inputPassword = (EditText) findViewById(R.id.password);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            btnSignup = (Button) findViewById(R.id.btn_signup);
            btnLogin = (Button) findViewById(R.id.btn_login);
            btnReset = (Button) findViewById(R.id.btn_reset_password);

            scrollView = (ScrollView) findViewById(R.id.scrollview);
            animationDrawable = (AnimationDrawable) scrollView.getBackground();
            animationDrawable.setEnterFadeDuration(2000);
            animationDrawable.setExitFadeDuration(2000);

            btnSignup.setOnClickListener(this);


            btnReset.setOnClickListener(this);


            btnLogin.setOnClickListener(this);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                startLogin();
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a chat_message_item to the user. If sign in succeeds
                                // the mAuth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {
                                        Toast.makeText(SignInActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    saveUserToFireBase();
                                }
                            }
                        });
                break;
            case R.id.btn_signup:
                startActivity(new Intent(SignInActivity.this, SignupActivity.class));
                break;
            case R.id.btn_reset_password:
                startActivity(new Intent(SignInActivity.this, ResetPasswordActivity.class));
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning())
            animationDrawable.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning())
            animationDrawable.stop();
    }

    public void showProgress(String title, String message) {
        ProgressDialogFragment mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment == null) {
            Log.d(TAG, "opening origress dialog");
            mProgressDialogFragment = ProgressDialogFragment.newInstance(
                    title, message, ProgressDialog.STYLE_SPINNER);
            mProgressDialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.class.getName());
        }
    }

    public void hideProgressMessage() {
        ProgressDialogFragment mProgressDialogFragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.class.getName());
        if (mProgressDialogFragment != null)
            mProgressDialogFragment.dismiss();

    }

    public void saveUserToFireBase() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();
                            String niftyToken = FirebaseInstanceId.getInstance().getToken().toString();

                            Log.d(TAG, ID_TOKEN + idToken);
                            Log.d(TAG, NOTIFY_TOKEN + niftyToken);

                            SharePref.getInstance(SignInActivity.this).putString(ID_TOKEN, idToken);
                            SharePref.getInstance(SignInActivity.this).putString(NOTIFY_TOKEN, niftyToken);

                            // Send token to your backend via HTTPS
                            mFirebaseInstance.getReference().child("users").child(user.getUid()).child("notify_token").setValue(FirebaseInstanceId.getInstance().getToken().toString());
                            mFirebaseInstance.getReference().child("users").child(user.getUid()).child("user_token").setValue(task.getResult().getToken());
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("name").setValue(user.getDisplayName());
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("email").setValue(user.getEmail());
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("avatar").setValue(user.getPhotoUrl().toString());
                            mFirebaseInstance.getReference("users").child(user.getUid()).child("notify_radius").setValue(SharePref.getInstance(SignInActivity.this).getDefaultRadiusgetDefaultRadius());

                            Intent intent = new Intent(SignInActivity.this, TutorialActivity.class);
                            startActivity(intent);
                            finish();

                            // ...
                        } else {
                            // Handle error -> task.getException();
                        }
                    }
                });

    }


    @Override
    public void login(boolean status) {
        hideProgressMessage();
        if (!status)
            Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
        else
            saveUserToFireBase();

    }

    @Override
    public void startLogin() {
        showProgress(getString(R.string.loading), getString(R.string.please_wait));
    }


}


