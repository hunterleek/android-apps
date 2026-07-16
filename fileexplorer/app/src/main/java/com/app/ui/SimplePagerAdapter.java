package com.app.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SimplePagerAdapter extends FragmentStateAdapter {
    private final int count;
    public SimplePagerAdapter(FragmentActivity fa, int count) {
        super(fa);
        this.count = count;
    }
    @NonNull @Override public Fragment createFragment(int position) {
        Fragment f = new Fragment();
        Bundle args = new Bundle();
        args.putInt("pos", position);
        f.setArguments(args);
        return f;
    }
    @Override public int getItemCount() { return count; }
}
