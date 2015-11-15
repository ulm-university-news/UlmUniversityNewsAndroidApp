package ulm.university.news.app.controller;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ulm.university.news.app.R;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT;
    private String tabTitles[];
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        tabTitles = new String[]{
                context.getString(R.string.fragment_channel_s_title),
                context.getString(R.string.fragment_group_title),
                context.getString(R.string.fragment_settings_title)};
        PAGE_COUNT = tabTitles.length;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChannelFragment.newInstance();
            case 1:
                return GroupFragment.newInstance();
            case 2:
                return SettingsFragment.newInstance();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
