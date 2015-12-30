package ulm.university.news.app.controller;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import ulm.university.news.app.R;

/**
 * TODO
 *
 * @author Matthias Mak
 */
public class MainFragmentPager extends FragmentPagerAdapter {
    final int PAGE_COUNT;
    private String tabTitles[];
    private Context context;

    public MainFragmentPager(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        tabTitles = new String[]{
                context.getString(R.string.activity_main_channel_tab),
                context.getString(R.string.activity_main_group_tap)};
        PAGE_COUNT = tabTitles.length;
    }

    private Fragment mCurrentFragment;

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            mCurrentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
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
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position.
        return tabTitles[position];
    }
}
