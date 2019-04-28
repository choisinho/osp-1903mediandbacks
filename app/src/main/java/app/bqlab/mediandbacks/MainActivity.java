package app.bqlab.mediandbacks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //constants
    final int LAYOUT_MAIN_DASHBOARD = 0;
    final int LAYOUT_MAIN_ANALYSIS = 1;
    final int LAYOUT_MAIN_STRETCH = 2;
    final int LAYOUT_MAIN_SETTING = 3;
    final int LAYOUT_MAIN_SETTING_CONNECT = 4;
    final int LAYOUT_MAIN_SETTING_NOTIFY = 5;
    final int LAYOUT_MAIN_SETTING_PROFILE = 6;
    final int LAYOUT_MAIN_SETTING_VERSION = 7;
    final int LAYOUT_MAIN_SETTING_NOTICE = 8;
    //variables
    int layoutIndex;
    String totalTimeText, goodTimeText, badTimeText;
    //objects
    Bluetooth mBluetooth;
    DatabaseReference mDatabase;
    //layouts
    FrameLayout main;
    LinearLayout mainBar;
    PieChart mainChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InternetCheck.showDialogAfterCheck(this);
        showRefreshDialog();
        init();
        showLayoutByIndex(layoutIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case Bluetooth.REQUEST_ENABLE_BLUETOOTH:
                    break;
                case Bluetooth.REQUEST_DISCOVERABLE:
                    break;

            }
        } else {
            switch (requestCode) {
                case Bluetooth.REQUEST_ENABLE_BLUETOOTH:
                    mBluetooth.setup();
                    break;
                case Bluetooth.REQUEST_DISCOVERABLE:
                    mBluetooth.autoConnect();
                    break;

            }
        }
    }

    @Override
    public void onBackPressed() {
        switch (layoutIndex) {
            case LAYOUT_MAIN_DASHBOARD:
                finishAffinity();
                break;
            case LAYOUT_MAIN_ANALYSIS:
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
                break;
            case LAYOUT_MAIN_STRETCH:
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
                break;
            case LAYOUT_MAIN_SETTING:
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
                break;
            case LAYOUT_MAIN_SETTING_CONNECT:
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
                break;
            case LAYOUT_MAIN_SETTING_NOTICE:
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
                break;
            case LAYOUT_MAIN_SETTING_NOTIFY:
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
                break;
            case LAYOUT_MAIN_SETTING_VERSION:
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
                break;
            case LAYOUT_MAIN_SETTING_PROFILE:
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
                break;
        }
    }

    private void init() {
        //check service running
        if (!ServiceCheck.isRunning(this, UserService.class.getName())) {
            Log.d("MainActivity", "Userservice not running");
            new AlertDialog.Builder(this)
                    .setMessage("사용자의 정보를 불러올 수 없습니다. 다시 로그인하시길 바랍니다.")
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            mBluetooth.getSetting().disconnect();
                            finish();
                        }
                    }).show();
        }
        //initialize
        mBluetooth = new Bluetooth(this, this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        main = findViewById(R.id.main);
        mainBar = findViewById(R.id.main_bar);
        mainChart = findViewById(R.id.main_dashboard_chart);
        layoutIndex = getIntent().getIntExtra("refreshIndex", 0);
        //setup
        ((SwipeRefreshLayout) findViewById(R.id.main_refresh_layout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        //call setup methods
        setMainBar();
        setMainDashboard();
        setMainAnalisys();
        setMainStretch();
        setMainSetting();
        setMainSettingConnect();
        setMainSettingNotice();
        setMainSettingNotify();
        setMainSettingProfile();
        setMainSettingVersion();
    }

    private void setMainBar() {
        findViewById(R.id.main_bar_dashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
            }
        });
        findViewById(R.id.main_bar_analysis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_bar_stretch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_STRETCH);
            }
        });
        findViewById(R.id.main_bar_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
            }
        });
        findViewById(R.id.main_dashboard_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_dashboard_vibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_dashboard_analysis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_dashboard_stretch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_STRETCH);
            }
        });
        findViewById(R.id.main_analysis_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
            }
        });
    }

    private void setMainDashboard() {
        //setup layouts
        findViewById(R.id.main_dashboard_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_dashboard_vibrate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_dashboard_analysis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_ANALYSIS);
            }
        });
        findViewById(R.id.main_dashboard_stretch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_STRETCH);
            }
        });
        //setup layout
        if (UserService.dataTotal != 0 && (UserService.dataBad != 0 || UserService.dataGood != 0)) {
            ArrayList<PieEntry> values = new ArrayList<>();
            mainChart.setUsePercentValues(true);
            mainChart.getDescription().setEnabled(false);
            mainChart.setTouchEnabled(false);
            mainChart.setTransparentCircleRadius(0f);
            mainChart.setExtraOffsets(0, 0, 0, 0);
            mainChart.setDrawSliceText(false);
            mainChart.setDrawHoleEnabled(true);
            mainChart.setHoleRadius(90f);
            mainChart.setHoleColor(getResources().getColor(R.color.colorWhite));
            mainChart.getLegend().setEnabled(false);
            values.add(new PieEntry(UserService.dataBad, "bad"));
            values.add(new PieEntry(UserService.dataGood, "good"));
            PieDataSet dataSet = new PieDataSet(values, "Data");
            dataSet.setSliceSpace(0f);
            dataSet.setColors(getResources().getColor(R.color.colorRedForChart), getResources().getColor(R.color.colorBlueForChart));
            PieData pieData = new PieData(dataSet);
            pieData.setValueTextSize(0f);
            mainChart.setData(pieData);
            String text = "나쁜 자세 " + String.valueOf((int) ((double) UserService.dataBad / (double) UserService.dataTotal * 100)) + "%";
            if (UserService.dataBad < UserService.dataGood) {
                ((TextView) findViewById(R.id.main_dashboard_chart_grade)).setTextColor(getResources().getColor(R.color.colorBlueForChart));
                ((TextView) findViewById(R.id.main_dashboard_chart_state)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.main_dashboard_chart_grade)).setText("GOOD");
                ((TextView) findViewById(R.id.main_dashboard_chart_state)).setText(text);
            } else {
                ((TextView) findViewById(R.id.main_dashboard_chart_grade)).setTextColor(getResources().getColor(R.color.colorRedForChart));
                ((TextView) findViewById(R.id.main_dashboard_chart_state)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.main_dashboard_chart_grade)).setText("BAD");
                ((TextView) findViewById(R.id.main_dashboard_chart_state)).setText(text);
            }
            String vibrateText = String.valueOf(UserService.dataVibrate) + "회";
            ((TextView) findViewById(R.id.main_dashboard_vibrate_content)).setText(vibrateText);
            ((TextView) findViewById(R.id.main_dashboard_vibrate_content)).setText(vibrateText);
        } else {
            ArrayList<PieEntry> values = new ArrayList<>();
            mainChart.setUsePercentValues(true);
            mainChart.getDescription().setEnabled(false);
            mainChart.setTouchEnabled(false);
            mainChart.setTransparentCircleRadius(0f);
            mainChart.setExtraOffsets(0, 0, 0, 0);
            mainChart.setDrawSliceText(false);
            mainChart.setDrawHoleEnabled(true);
            mainChart.setHoleRadius(90f);
            mainChart.setHoleColor(getResources().getColor(R.color.colorWhite));
            mainChart.getLegend().setEnabled(false);
            values.add(new PieEntry(1f, "No data"));
            PieDataSet dataSet = new PieDataSet(values, "Data");
            dataSet.setSliceSpace(0f);
            dataSet.setColors(getResources().getColor(R.color.colorWhiteDark));
            PieData pieData = new PieData(dataSet);
            pieData.setValueTextSize(0f);
            mainChart.setData(pieData);
            ((TextView) findViewById(R.id.main_dashboard_chart_grade)).setTextColor(getResources().getColor(R.color.colorWhiteDark));
            findViewById(R.id.main_dashboard_chart_state).setVisibility(View.GONE);
        }
        if (UserService.dataGood > 3600)
            goodTimeText = String.valueOf(UserService.dataGood / 3600) + "시간 " + String.valueOf((UserService.dataGood % 3600) / 60) + "분";
        else
            goodTimeText = String.valueOf((UserService.dataGood % 3600) / 60) + "분";
        if (UserService.dataBad > 3600)
            badTimeText = String.valueOf(UserService.dataBad / 3600) + "시간 " + String.valueOf((UserService.dataBad % 3600) / 60) + "분";
        else
            badTimeText = String.valueOf((UserService.dataBad % 3600) / 60) + "분";
        if (UserService.dataTotal > 3600)
            totalTimeText = String.valueOf(UserService.dataTotal / 3600) + "시간 " + String.valueOf((UserService.dataTotal % 3600) / 60) + "분";
        else
            totalTimeText = String.valueOf((UserService.dataTotal % 3600) / 60) + "분";
        String vibrateTimeText = String.valueOf(UserService.dataVibrate) + "회";
        ((TextView) findViewById(R.id.main_analysis_time_content)).setText(String.valueOf(totalTimeText));
        ((TextView) findViewById(R.id.main_analysis_vibrate_content)).setText(vibrateTimeText);
        ((TextView) findViewById(R.id.main_analysis_good_content)).setText(goodTimeText);
        ((TextView) findViewById(R.id.main_analysis_bad_content)).setText(badTimeText);
        ((TextView) findViewById(R.id.main_dashboard_total_content)).setText(totalTimeText);
    }

    private void setMainAnalisys() {
        findViewById(R.id.main_analysis_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
            }
        });
        String today = UserService.today.substring(0, 4) + "-" + UserService.today.substring(4, 6) + "-" + UserService.today.substring(6, 8);
        String period = today + " ~ " + today;
        ((TextView) findViewById(R.id.main_analysis_top_date)).setText(period);
        if (UserService.data > UserService.goodPose + 15) {
            ((TextView) findViewById(R.id.main_analysis_top_state)).setText("전만");
            ((TextView) findViewById(R.id.main_analysis_top_details)).setText("어깨를 좀 더\n펴 보세요.");
        } else if (UserService.data < UserService.goodPose - 7) {
            ((TextView) findViewById(R.id.main_analysis_top_state)).setText("후만");
            ((TextView) findViewById(R.id.main_analysis_top_details)).setText("어깨를 좀 더\n숙여보세요.");
        } else {
            ((TextView) findViewById(R.id.main_analysis_top_state)).setText("정상");
            ((TextView) findViewById(R.id.main_analysis_top_details)).setText("정상입니다.\n꾸준히 관리하세요.");
        }
    }

    private void setMainStretch() {
        findViewById(R.id.main_stretch_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_DASHBOARD);
            }
        });
    }

    private void setMainSetting() {
        if (UserService.deviceConnected) {
            ((TextView) findViewById(R.id.main_setting_top_connect_state)).setText("연결 됨");
            findViewById(R.id.main_setting_connect_top_circle).setBackground(getResources().getDrawable(R.drawable.app_blue_circle));
        } else {
            ((TextView) findViewById(R.id.main_setting_top_connect_state)).setText("연결 안됨");
            findViewById(R.id.main_setting_connect_top_circle).setBackground(getResources().getDrawable(R.drawable.app_white_circle));
        }
        findViewById(R.id.main_setting_top_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING_CONNECT);
            }
        });
        findViewById(R.id.main_setting_set_notify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING_NOTIFY);
            }
        });
        findViewById(R.id.main_setting_set_posture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserService.deviceConnected) {
                    Intent i = new Intent(MainActivity.this, InitialActivity.class);
                    i.putExtra("mainToInitial", true);
                    startActivity(i);
                } else
                    Toast.makeText(MainActivity.this, "장치와 연결되어 있지 않습니다.", Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.main_setting_my_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING_PROFILE);
            }
        });
        findViewById(R.id.main_setting_my_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING_VERSION);
            }
        });
        findViewById(R.id.main_setting_my_notice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING_NOTICE);
            }
        });
    }

    private void setMainSettingConnect() {
        if (UserService.deviceConnected) {
            ((TextView) findViewById(R.id.main_setting_connect_state)).setText("연결 됨");
            ((TextView) findViewById(R.id.main_setting_connect_state)).setTextColor(getColor(R.color.colorWhiteNavy));
            findViewById(R.id.main_setting_connect_button).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.main_setting_connect_state)).setText("연결 안됨");
            ((TextView) findViewById(R.id.main_setting_connect_state)).setTextColor(getColor(R.color.colorRedPrimary));
            findViewById(R.id.main_setting_connect_button).setVisibility(View.VISIBLE);
            findViewById(R.id.main_setting_connect_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectDevice();
                }
            });
        }
        findViewById(R.id.main_setting_connect_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
            }
        });
    }

    private void setMainSettingNotify() {
        findViewById(R.id.main_setting_notify_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
            }
        });
        findViewById(R.id.main_setting_notify_notify_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayAdapter<String> items = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                items.addAll("즉시", "5초", "10초");
                new AlertDialog.Builder(MainActivity.this)
                        .setAdapter(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((TextView) findViewById(R.id.main_setting_notify_notify_time)).setText(new String[]{"즉시>", "5초>", "10초>"}[which]);
                                mDatabase.child(UserService.userKey).child("setting").child("notify_delay").setValue(which * 5);
                            }
                        }).show();

            }
        });
        String goodpose = String.valueOf(UserService.goodPose) + "도";
        String badpose = String.valueOf(UserService.badPose) + "도";
        ((TextView) findViewById(R.id.main_setting_notify_notify_good)).setText(goodpose);
        ((TextView) findViewById(R.id.main_setting_notify_notify_bad)).setText(badpose);
    }

    private void setMainSettingProfile() {
        ((TextView) findViewById(R.id.main_setting_profile_email)).setText(UserService.userId);
        ((TextView) findViewById(R.id.main_setting_profile_name)).setText(UserService.userName);
        ((TextView) findViewById(R.id.main_setting_profile_sex)).setText(UserService.userSex);
        ((TextView) findViewById(R.id.main_setting_profile_birthday)).setText(UserService.userBirthday);
        ((TextView) findViewById(R.id.main_setting_profile_register_date)).setText(UserService.userRegisterDate);
        findViewById(R.id.main_setting_profile_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
            }
        });
        findViewById(R.id.main_setting_profile_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("지금 로그아웃 하시겠습니까?")
                        .setMessage("로그아웃시 지금 설정한 결과는 삭제됩니다.")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopService(new Intent(MainActivity.this, UserService.class));
                                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                i.putExtra("id", UserService.userId);
                                startActivity(i);
                                finish();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });
        findViewById(R.id.main_setting_profile_withdrawal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("정말 회원탈퇴 하시겠습니까?")
                        .setMessage("회원탈퇴를 하시면 메디앤백스를 사용할 수 없으며 재가입은 1주일 후에 가능합니다.")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDatabase.child(UserService.userKey).removeValue();
                                stopService(new Intent(MainActivity.this, UserService.class));
                                startActivity(new Intent(MainActivity.this, StartActivity.class));
                                Toast.makeText(MainActivity.this, "탈퇴하였습니다.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });
    }

    private void setMainSettingVersion() {
        try {
            ((TextView) findViewById(R.id.main_setting_version_current)).setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            ((TextView) findViewById(R.id.main_setting_version_latest)).setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            ((TextView) findViewById(R.id.main_setting_version_using)).setText("최신버전을 사용 중 입니다.\n");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        findViewById(R.id.main_setting_version_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
            }
        });
        findViewById(R.id.main_setting_version_upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("스토어에서 정보를 불러올 수 없습니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }

    private void setMainSettingNotice() {
        findViewById(R.id.main_setting_notice_top_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayoutByIndex(LAYOUT_MAIN_SETTING);
            }
        });
    }

    private void showLayoutByIndex(int idx) {
        layoutIndex = idx;
        InternetCheck.showDialogAfterCheck(this);
        FrameLayout main = findViewById(R.id.main);
        LinearLayout mainBar = findViewById(R.id.main_bar);
        for (int i = 0; i < main.getChildCount(); i++)
            main.getChildAt(i).setVisibility(View.GONE);
        main.getChildAt(idx).setVisibility(View.VISIBLE);
        if (idx < LAYOUT_MAIN_SETTING_CONNECT) {
            switch (idx) {
                case LAYOUT_MAIN_DASHBOARD:
                    ((LinearLayout) mainBar.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_dashboard_p));
                    ((LinearLayout) mainBar.getChildAt(1)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_analysis_np));
                    ((LinearLayout) mainBar.getChildAt(2)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_stretch_np));
                    ((LinearLayout) mainBar.getChildAt(3)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_setting_np));
                    break;
                case LAYOUT_MAIN_ANALYSIS:
                    ((LinearLayout) mainBar.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_dashboard_np));
                    ((LinearLayout) mainBar.getChildAt(1)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_analysis_p));
                    ((LinearLayout) mainBar.getChildAt(2)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_stretch_np));
                    ((LinearLayout) mainBar.getChildAt(3)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_setting_np));
                    break;
                case LAYOUT_MAIN_STRETCH:
                    ((LinearLayout) mainBar.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_dashboard_np));
                    ((LinearLayout) mainBar.getChildAt(1)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_analysis_np));
                    ((LinearLayout) mainBar.getChildAt(2)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_stretch_p));
                    ((LinearLayout) mainBar.getChildAt(3)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_setting_np));
                    break;
                case LAYOUT_MAIN_SETTING:
                    ((LinearLayout) mainBar.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_dashboard_np));
                    ((LinearLayout) mainBar.getChildAt(1)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_analysis_np));
                    ((LinearLayout) mainBar.getChildAt(2)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_stretch_np));
                    ((LinearLayout) mainBar.getChildAt(3)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_setting_p));
                    break;
            }
        } else {
            ((LinearLayout) mainBar.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_dashboard_np));
            ((LinearLayout) mainBar.getChildAt(1)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_analysis_np));
            ((LinearLayout) mainBar.getChildAt(2)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_stretch_np));
            ((LinearLayout) mainBar.getChildAt(3)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.main_bar_setting_p));
        }
    }

    private void showRefreshDialog() {
        if (getSharedPreferences("setting", MODE_PRIVATE).getBoolean("SHOW_REFRESH_DAILOG", true)) {
            new AlertDialog.Builder(this)
                    .setTitle("그래프가 잘 보이지 않나요?")
                    .setMessage("새로고침을 원하시면 손가락을 화면에 대고 쓸어내려보세요.")
                    .setPositiveButton("새로고침", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refresh();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNeutralButton("다시는 나타나지 않음", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getSharedPreferences("setting", MODE_PRIVATE).edit().putBoolean("SHOW_REFRESH_DAILOG", false).apply();
                        }
                    })
                    .show();
        }
    }

    private void refresh() {
        finish();
        Intent i = getIntent();
        i.putExtra("refreshIndex", layoutIndex);
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
        ((SwipeRefreshLayout) findViewById(R.id.main_refresh_layout)).setRefreshing(false);
    }

    private void connectDevice() {
        if (UserService.deviceConnected) {
            Log.d("MainActivity", "Already connected");
        } else {
            Log.d("MainActivity", "Start to connect device");
            final ProgressLayout p = new ProgressLayout(this);
            main.addView(p);
            ChildrenEnable.set(false, main);
            ChildrenEnable.set(false, mainBar);
            mBluetooth.getSetting().setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
                @Override
                public void onDataReceived(byte[] data, String message) {
                    if (UserService.dataTotal == 0)
                        UserService.dataTotal = 1;
                    UserService.deviceConnected = true;
                    UserService.data = Integer.valueOf(message) - 90;
                    mDatabase.child(UserService.userKey).child("data").child("realtime").setValue(UserService.data);
                    Log.d("MainActivity", "Realtime data: " + String.valueOf(UserService.data));
                }
            });
            mBluetooth.getSetting().setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
                @Override
                public void onDeviceConnected(String name, String address) {
                    main.removeView(p);
                    ChildrenEnable.set(true, main);
                    ChildrenEnable.set(true, mainBar);
                    Log.d("MainActivity", "Connected to device");
                    refresh();
                }

                @Override
                public void onDeviceDisconnected() {
                    UserService.deviceConnected = false;
                    Log.d("MainActivity", "Disconnected to device");
                    Toast.makeText(MainActivity.this, "장치와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onDeviceConnectionFailed() {
                    main.removeView(p);
                    ChildrenEnable.set(true, main);
                    UserService.deviceConnected = false;
                    Log.d("MainActivity", "Failed to connect device");
                    Toast.makeText(MainActivity.this, "장치와 연결할 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            });
            mBluetooth.checkup();
        }

    }
}
