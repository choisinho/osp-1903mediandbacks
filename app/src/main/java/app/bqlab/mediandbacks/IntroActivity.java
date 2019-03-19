package app.bqlab.mediandbacks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        checkUserService();
    }

    private void checkUserService() {
        if (ServiceCheck.isRunning(this, UserService.class.getName())) {
            if (getSharedPreferences("setting", MODE_PRIVATE).getBoolean("FIRST_RUN", true)) {
                startActivity(new Intent(this, InitialActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, StartActivity.class));
            finish();
        }
    }
}
