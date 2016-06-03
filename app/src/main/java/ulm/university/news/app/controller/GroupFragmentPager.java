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
public class GroupFragmentPager extends FragmentPagerAdapter {
    final int PAGE_COUNT;
    private String tabTitles[];
    private int groupId;

    public GroupFragmentPager(FragmentManager fm, Context context, int groupId) {
        super(fm);
        this.groupId = groupId;
        tabTitles = new String[]{
                context.getString(R.string.activity_group_tab_conversations),
                context.getString(R.string.activity_group_tab_ballots),
                context.getString(R.string.activity_group_tab_information)
        };
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
                return ConversationFragment.newInstance(groupId);
            case 1:
                return BallotFragment.newInstance(groupId);
            case 2:
                return GroupDetailFragment.newInstance(groupId);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position.
        return tabTitles[position];
    }
}
