package com.example.market.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.market.R;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class AccountActivity extends AppCompatActivity {

    public static final String KEY_TAB = "key_tab";

    private FirebaseFirestore firebaseFirestore;
    private TextView accountTextView;
    private TextView welcomeTextView;
    private Button loginButton;
    private TextView loginTextView;
    private CollectionReference userRef;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener listener;
    private boolean isLogedin = false;
    private RelativeLayout recentlyViewedItemsLayout;
    private RelativeLayout savedItemsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_account);
        firebaseFirestore = FirebaseFirestore.getInstance();
        userRef = firebaseFirestore.collection("users/");
        recentlyViewedItemsLayout = findViewById(R.id.recently_viewed_items);
        savedItemsLayout = findViewById(R.id.saved_items_layout);
        recentlyViewedItemsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this,RecentlyViewedActivity.class));
                finish();
            }
        });
        savedItemsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountActivity.this,MainActivity.class);
                intent.putExtra(KEY_TAB,1);
                startActivity(intent);
            }
        });
        initializeUI();
        checkUser();
        Toolbar toolbar = findViewById(R.id.account_tool_bar);
        toolbar.setTitle("Account");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuth != null) {
            mAuth.addAuthStateListener(listener);
        }
    }

    private void checkUser() {
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    isLogedin = true;
                    loginButton.setVisibility(View.INVISIBLE);
                    loginTextView.setText("LOGOUT");
                    accountTextView.setText(firebaseAuth.getCurrentUser().getProviderData().get(1).getEmail());
                    DocumentReference documentReference = userRef.document(firebaseAuth.getCurrentUser().getUid());
                    documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if(e != null){
                                Log.d("", "Error is :" + e.getMessage());
                                Toast.makeText(AccountActivity.this, "Error is :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                welcomeTextView.setText("Welcome " + documentSnapshot.get("first_name"));
                                //welcomeTextView.setText("Welcome " + firebaseAuth.getCurrentUser().getDisplayName());
                                welcomeTextView.setTextColor(ContextCompat.getColor(AccountActivity.this,R.color.colorAccent));
                            }
                        }
                    });
                }
            }
        };
    }

    private void initializeUI() {
        welcomeTextView = findViewById(R.id.welcome_text_view);
        accountTextView = findViewById(R.id.account_text_view);
        loginButton = findViewById(R.id.login_button);
        loginTextView = findViewById(R.id.login_text_view);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this, LoginActivity.class));
            }
        });
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogedin) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(AccountActivity.this,R.style.AlertDialogCustom)
                            .setTitle("Logout Confirmation")
                            .setMessage("Are you sure you want to exit?")
                            //Specifying a removeListener allows you to take an action before dismissing the dialog
                            // The dialog is automatically dismissed when a dialog button is clicked
                            .setNegativeButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mAuth.signOut();
                                    LoginManager.getInstance().logOut();
                                    loginTextView.setText("LOGIN");
                                    loginButton.setVisibility(View.VISIBLE);
                                    isLogedin = false;
                                    welcomeTextView.setText("Welcome!");
                                    accountTextView.setText("Enter your account");
                                }
                            })// A null removeListener allows the button to dismiss the dialog and take no further action
                            .setPositiveButton("NO", null);

                    dialog.show()
                            .getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AccountActivity.this, R.color.alertDialogTextColor));
                } else {
                    startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                }
            }
        });
    }
}
