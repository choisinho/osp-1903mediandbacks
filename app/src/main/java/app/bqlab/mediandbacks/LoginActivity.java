package app.bqlab.mediandbacks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    DatabaseReference mDatabase;
    ArrayList<DataSnapshot> infoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupDatabase();
        init();
        loadRegisterdId();
    }

    private void init() {
        //check
        InternetChecker.showDialogAfterCheck(this);
        //setup
        findViewById(R.id.login_find).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPwsearchLayout();
            }
        });
        findViewById(R.id.login_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
        String id = ((TextView) findViewById(R.id.login_id)).getText().toString();
        String pw = ((TextView) findViewById(R.id.login_pw)).getText().toString();
        if (id.isEmpty() || pw.isEmpty()) {
            new AlertDialog.Builder(LoginActivity.this)
                    .setMessage("아이디와 비밀번호를 다시 확인하세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        } else if (!isCorrectIdpw(id, pw)) {
            new AlertDialog.Builder(LoginActivity.this)
                    .setMessage("아이디와 비밀번호를 다시 확인하세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        } else {
            if (ServiceCheck.isRunning(this, UserService.class.getName())) {
                Toast.makeText(this, "이미 로그인된 아이디입니다. 잠시후 다시 로그틴을 해주세요.", Toast.LENGTH_LONG).show();
                stopService(new Intent(LoginActivity.this, UserService.class));
                finishAffinity();
            } else {
                if (getSharedPreferences("setting", MODE_PRIVATE).getBoolean("NEED_INITIAL_SETTING", true)) {
                    startActivity(new Intent(this, InitialActivity.class));
                    UserService.buzzable = false;
                    UserService.userId = id;
                    finish();
                } else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    UserService.userId = id;
                    finish();
                }
            }
        }
    }

    private boolean isCorrectIdpw(String id, String pw) {
        DataSnapshot loginInfo = null;
        for (DataSnapshot info : infoList) {
            String currentId = String.valueOf(info.child("id").getValue());
            if (id.equals(currentId)) {
                loginInfo = info;
                break;
            }
        }
        if (loginInfo == null)
            return false;
        else
            return String.valueOf(loginInfo.child("pw").getValue()).equals(pw);
    }

    private void setupDatabase() {
        infoList = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                infoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    infoList.add(snapshot.child("info"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadRegisterdId() {
        if (getIntent().getStringExtra("id") != null)
            ((EditText) findViewById(R.id.login_id)).setText(getIntent().getStringExtra("id"));
    }

    private void showPwsearchLayout() {
        FrameLayout login = findViewById(R.id.login);
        ChildrenEnable.set(false, login);
        login.addView(new PwsearchLayout(LoginActivity.this));
    }
}
