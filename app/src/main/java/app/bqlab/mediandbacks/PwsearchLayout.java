package app.bqlab.mediandbacks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class PwsearchLayout extends LinearLayout {

    DatabaseReference mDatabase;
    ArrayList<DataSnapshot> infoList;

    public PwsearchLayout(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_pwsearch, this);
        setupDatabase();
        init();
    }

    private void init() {
        findViewById(R.id.pwsearch_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        findViewById(R.id.pwsearch_send).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCorrectId(((EditText) findViewById(R.id.pwsearch_email)).getText().toString())) {
                    Toast.makeText(getContext(), "비밀번호가 전송되었습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "가입되지 않은 아이디입니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void close() {
        final FrameLayout parent = ((FrameLayout) getParent());
        ChildrenEnable.set(true, parent);
        parent.removeView(PwsearchLayout.this);
    }

    private boolean isCorrectId(String id) {
        for (DataSnapshot info : infoList) {
            String currentId = String.valueOf(info.child("id").getValue());
            if (id.equals(currentId)) {
                return true;
            }
        }
        return false;
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
}
