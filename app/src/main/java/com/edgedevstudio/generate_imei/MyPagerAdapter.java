package com.edgedevstudio.generate_imei;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.edgedevstudio.generate_imei.Tabs.Tab1;
import com.edgedevstudio.generate_imei.Tabs.Tab2;
import com.edgedevstudio.generate_imei.Tabs.Tab3;

import java.lang.ref.WeakReference;

/**
 * Created by OPEYEMI OLORUNLEKE on 12/3/2017.
 */

public class MyPagerAdapter extends FragmentPagerAdapter {
    private final SparseArray<WeakReference<Fragment>> instantiatedFragments = new SparseArray<>();
    private int no_of_tabs = 3;

    public MyPagerAdapter(FragmentManager fm, int no_of_tabs) {
        super(fm);
        this.no_of_tabs = no_of_tabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Tab1();
            case 1:
                return new Tab2();
            case 2:
                return new Tab3();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return no_of_tabs;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
        instantiatedFragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        instantiatedFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Nullable
    public Fragment getFragment(final int position) {
        final WeakReference<Fragment> wr = instantiatedFragments.get(position);
        if (wr != null) {
            return wr.get();
        } else {
            return null;
        }
    }
}