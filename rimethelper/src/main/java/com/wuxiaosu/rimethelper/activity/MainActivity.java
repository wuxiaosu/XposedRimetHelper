package com.wuxiaosu.rimethelper.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wuxiaosu.rimethelper.BuildConfig;
import com.wuxiaosu.rimethelper.R;
import com.wuxiaosu.rimethelper.base.BaseActivity;
import com.wuxiaosu.widget.SettingLabelView;

import java.util.Arrays;

public class MainActivity extends BaseActivity {

    private SettingLabelView mSlvHideIcon;
    private EditText mEtLatitude;
    private EditText mEtLongitude;
    private ImageView mIvLocation;

    private SettingLabelView mSlvLocation;
    private LinearLayout mLlLocationTime;
    private EditText mEtLocationStartTime;

    private final String[] supportVersions =
            new String[]{"4.2.0", "4.2.1", "4.2.6", "4.2.8", "4.3.0", "4.3.1", "4.3.2", "4.3.3", "4.3.5", "4.3.7", "5.1.35"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getToolbar().setNavigationIcon(null);

        showModuleActiveInfo(false);
        initView();
    }

    private void initView() {
        mSlvHideIcon = findViewById(R.id.slv_hide_icon);
        mEtLatitude = findViewById(R.id.et_latitude);
        mEtLongitude = findViewById(R.id.et_longitude);
        mIvLocation = findViewById(R.id.iv_location);

        mSlvLocation = findViewById(R.id.slv_location);
        mLlLocationTime = findViewById(R.id.ll_location_time);
        mEtLocationStartTime = findViewById(R.id.et_location_start_time);

        SharedPreferences sharedPreferences =
                getSharedPreferences(SettingLabelView.DEFAULT_PREFERENCES_NAME, Context.MODE_WORLD_READABLE);

        bindPreferences(mEtLatitude, sharedPreferences, R.string.pre_key_latitude, null);
        bindPreferences(mEtLongitude, sharedPreferences, R.string.pre_key_longitude, null);
        bindPreferences(mEtLocationStartTime, sharedPreferences, R.string.pre_key_location_start_time, null);

        mSlvHideIcon.setCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideLauncherIcon(isChecked);
            }
        });

        mIvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AMapLiteActivity.actionStart(MainActivity.this,
                        mEtLatitude.getText().toString(), mEtLongitude.getText().toString());
            }
        });

        mLlLocationTime.post(new Runnable() {
            @Override
            public void run() {
                mLlLocationTime.setVisibility(mSlvLocation.isChecked() ? View.VISIBLE : View.GONE);
            }
        });
        mSlvLocation.setCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLlLocationTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void hideLauncherIcon(boolean isHide) {
        PackageManager packageManager = this.getPackageManager();
        int hide = isHide ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        packageManager.setComponentEnabledSetting(getAliasComponentName(), hide, PackageManager.DONT_KILL_APP);
    }

    private ComponentName getAliasComponentName() {
        return new ComponentName(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".activity.MainActivity_Alias");
    }

    private void bindPreferences(EditText editText, final SharedPreferences sharedPreferences, final int preStrResId, Object defaultValue) {
        String temp = sharedPreferences.getString(getString(preStrResId), (String) defaultValue);
        editText.setText(temp);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPreferences.edit().putString(getString(preStrResId), s.toString()).apply();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AMapLiteActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            mEtLatitude.setText(data.getStringExtra(AMapLiteActivity.LAT_KEY));
            mEtLongitude.setText(data.getStringExtra(AMapLiteActivity.LON_KEY));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    private void showInfo() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_about_content, null);
        TextView mTvVersionName = view.findViewById(R.id.tv_version_name);
        TextView mTvInfo = view.findViewById(R.id.tv_info);
        final TextView mTvUrl = view.findViewById(R.id.tv_url);
        mTvUrl.setText(Html.fromHtml("<a href=''>https://github.com/wuxiaosu/XposedRimetHelper</a>"));
        mTvUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendURLIntent(((TextView) v).getText().toString());
            }
        });
        mTvVersionName.setText(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mTvInfo.setText(getString(R.string.app_description)
                + ",理论上支持钉钉4.2.0以上所有版本，当前版本实测支持以下版本\n钉钉："
                + Arrays.toString(supportVersions)
                + "\n更多详情：");
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("关于")
                .setView(view)
                .create();
        alertDialog.show();
    }

    private void sendURLIntent(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri contentUrl = Uri.parse(url);
        intent.setData(contentUrl);
        startActivity(intent);
    }

    /**
     * 模块激活信息
     *
     * @param isModuleActive
     */
    private void showModuleActiveInfo(boolean isModuleActive) {
        if (!isModuleActive) {
            Toast.makeText(this, "模块未激活", Toast.LENGTH_SHORT).show();
        }
    }

}
