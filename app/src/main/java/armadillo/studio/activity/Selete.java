/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.ArcMotion;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import armadillo.studio.R;
import armadillo.studio.adapter.FragmentAdapter;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.enums.SeleteFileType;
import armadillo.studio.common.transformer.ZoomOutPageTransformer;
import armadillo.studio.ui.selete.file.FileFragment;
import armadillo.studio.ui.selete.soft.SoftFragment;
import butterknife.BindView;

public class Selete extends BaseActivity<SeleteFileType> {
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private SeleteFileType seleteFileType;
    private final int REQUEST_CODE = 1001;

    @Override
    protected int BindXML() {
        return R.layout.activity_file_select;
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    private void initViewPager(SeleteFileType seleteFileType) {
        if (seleteFileType == SeleteFileType.JKS)
            toolbar.setTitle(R.string.selete_jks_file);
        TabLayout mTabLayout = findViewById(R.id.tabs);
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.activity_selete_file));
        if (seleteFileType == SeleteFileType.APK)
            titles.add(getString(R.string.activity_selete_soft));
        for (int i = 0; i < titles.size(); i++)
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i)));
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FileFragment(seleteFileType));
        if (seleteFileType == SeleteFileType.APK)
            fragments.add(new SoftFragment());
        FragmentAdapter mFragmentAdapteradapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapteradapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(mFragmentAdapteradapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (searchView.isSearchOpen())
                    searchView.closeSearch();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean flag = false;
        if (requestCode == 200) {
            if (grantResults.length > 0) {
                List<String> deniedPermissions = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    int result = grantResults[i];
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        String permission = permissions[i];
                        deniedPermissions.add(permission);
                    }
                    if (!deniedPermissions.isEmpty()) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                    } else
                        flag = true;
                }
            }
        }
        if (flag)
            initViewPager(seleteFileType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            } else
                initViewPager(seleteFileType);
        }
    }

    @Override
    public void BindData(SeleteFileType data) {
        if (data == null)
            onBackPressed();
        this.seleteFileType = data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            } else
                initViewPager(seleteFileType);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                initViewPager(seleteFileType);
            else
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        } else
            initViewPager(seleteFileType);
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        onBackPressed();
    }
}
