package app.bqlab.mediandbacks;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    //variables
    String userId;
    String userPw;
    String userPwConfirm;
    String userName;
    String userSex;
    String userBirthday;
    String userRegisterDay;
    String userKey;
    //objects
    ArrayList<String> registeredIds;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
        setupDatabase();
    }

    private void init() {
        //check
        InternetCheck.showDialogAfterCheck(this);
        //initialize
        userSex = "남자";
        registeredIds = new ArrayList<>();
        //setup
        findViewById(R.id.register_sex_male).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSex = "남자";
                findViewById(R.id.register_sex_male).setBackground(getResources().getDrawable(R.drawable.app_button_red));
                ((Button) findViewById(R.id.register_sex_male)).setTextColor(getResources().getColor(R.color.colorWhite));
                findViewById(R.id.register_sex_female).setBackground(getResources().getDrawable(R.drawable.app_button_white));
                ((Button) findViewById(R.id.register_sex_female)).setTextColor(getResources().getColor(R.color.colorGrayDark));
            }
        });
        findViewById(R.id.register_sex_female).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSex = "여자";
                findViewById(R.id.register_sex_female).setBackground(getResources().getDrawable(R.drawable.app_button_red));
                ((Button) findViewById(R.id.register_sex_female)).setTextColor(getResources().getColor(R.color.colorWhite));
                findViewById(R.id.register_sex_male).setBackground(getResources().getDrawable(R.drawable.app_button_white));
                ((Button) findViewById(R.id.register_sex_male)).setTextColor(getResources().getColor(R.color.colorGrayDark));
            }
        });
        findViewById(R.id.register_birthday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePicker picker = new DatePicker(RegisterActivity.this);
                picker.setCalendarViewShown(false);
                new AlertDialog.Builder(RegisterActivity.this)
                        .setView(picker)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                                userBirthday = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(calendar.getTime());
                                ((TextView) findViewById(R.id.register_birthday_day)).setText(userBirthday);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
        findViewById(R.id.register_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = ((EditText) findViewById(R.id.register_id)).getText().toString();
                userName = ((EditText) findViewById(R.id.register_name)).getText().toString();
                userPw = ((EditText) findViewById(R.id.register_pw)).getText().toString();
                userPwConfirm = ((EditText) findViewById(R.id.register_pwConfirm)).getText().toString();
                userRegisterDay = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(new Date());
                userKey = String.valueOf(userId.hashCode());
                if (isFormCorrect()) {
                    //info
                    mDatabase.child(userKey).child("info").child("id").setValue(userId);
                    mDatabase.child(userKey).child("info").child("pw").setValue(userPw.hashCode());
                    mDatabase.child(userKey).child("info").child("name").setValue(userName);
                    mDatabase.child(userKey).child("info").child("sex").setValue(userSex);
                    mDatabase.child(userKey).child("info").child("birthday").setValue(userBirthday);
                    mDatabase.child(userKey).child("info").child("register_date").setValue(userRegisterDay);
                    //data
                    mDatabase.child(userKey).child("data").child("realtime").setValue(0);
                    mDatabase.child(userKey).child("data").child(UserService.today).child("bad").setValue(0);
                    mDatabase.child(userKey).child("data").child(UserService.today).child("good").setValue(0);
                    mDatabase.child(userKey).child("data").child(UserService.today).child("total").setValue(0);
                    mDatabase.child(userKey).child("data").child(UserService.today).child("vibrate").setValue(0);
                    //setting
                    mDatabase.child(userKey).child("setting").child("good_pose").setValue(0);
                    mDatabase.child(userKey).child("setting").child("bad_pose").setValue(0);
                    mDatabase.child(userKey).child("setting").child("notify_delay").setValue(0);
                    mDatabase.child(userKey).child("setting").child("week_goal").setValue("");
                    //finish
                    Toast.makeText(RegisterActivity.this, "회원 가입이 완료되었습니다.", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                    i.putExtra("id", userId);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    private boolean isFormCorrect() {
        if (!EmailCheck.isCorrect(userId)) {
            Toast.makeText(RegisterActivity.this, "이메일을 다시 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        } else if (userPw.length() < 8) {
            Toast.makeText(RegisterActivity.this, "비밀번호가 8자 이하입니다.", Toast.LENGTH_LONG).show();
            return false;
        } else if (!userPw.equals(userPwConfirm)) {
            Toast.makeText(RegisterActivity.this, "비밀번호를 다시 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        } else if (!((CheckBox) findViewById(R.id.register_agree)).isChecked()) {
            Toast.makeText(RegisterActivity.this, "회원 약관을 동의해야 합니다.", Toast.LENGTH_LONG).show();
            return false;
        } else if (userId.isEmpty() || userName.isEmpty() || userPw.isEmpty() || userPwConfirm.isEmpty() || userSex.isEmpty() || userBirthday.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "빈칸이 있는지 다시 확인해주세요.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            for (String id : registeredIds) {
                if (id.equals(String.valueOf(userId.hashCode()))) {
                    Toast.makeText(RegisterActivity.this, "이미 아이디가 존재합니다.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }

    private void setupDatabase() {
        //initialize
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                registeredIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    registeredIds.add(snapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
