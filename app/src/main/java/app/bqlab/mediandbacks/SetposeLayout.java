package app.bqlab.mediandbacks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SetposeLayout extends LinearLayout {

    public SetposeLayout(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_setpose, this);
        init();
    }

    private void init() {
        findViewById(R.id.setpose_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    void close() {
        final FrameLayout parent = ((FrameLayout) getParent());
        ChildrenEnable.set(true, parent);
        parent.removeView(SetposeLayout.this);
    }

    Button getButton() {
        return findViewById(R.id.setpose_done);
    }
}
