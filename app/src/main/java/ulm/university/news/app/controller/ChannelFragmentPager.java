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
public class ChannelFragmentPager extends FragmentPagerAdapter {
    final int PAGE_COUNT;
    private String tabTitles[];
    private int channelId;

    public ChannelFragmentPager(FragmentManager fm, Context context, int channelId) {
        super(fm);
        this.channelId = channelId;
        tabTitles = new String[]{
                context.getString(R.string.activity_channel_announcement_tab),
                context.getString(R.string.activity_channel_channel_details_tab)};
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
                return AnnouncementFragment.newInstance(channelId);
            case 1:
                return ChannelDetailFragment.newInstance(channelId);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position.
        return tabTitles[position];
    }
}
