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
public class BallotFragmentPager extends FragmentPagerAdapter {
    final int PAGE_COUNT;
    private String tabTitles[];
    private int groupId;
    private int ballotId;

    public BallotFragmentPager(FragmentManager fm, Context context, int groupId, int ballotId) {
        super(fm);
        this.groupId = groupId;
        this.ballotId = ballotId;
        tabTitles = new String[]{
                context.getString(R.string.activity_ballot_tab_vote),
                context.getString(R.string.activity_ballot_tab_result),
                context.getString(R.string.activity_ballot_tab_information)
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
                return OptionFragment.newInstance(groupId, ballotId);
            case 1:
                return BallotResultFragment.newInstance(groupId, ballotId);
            case 2:
                return BallotDetailFragment.newInstance(groupId, ballotId);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position.
        return tabTitles[position];
    }
}
