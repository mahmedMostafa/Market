package com.example.market.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.market.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    //return the corresponding fragment to the tab layout
   /* @Override
    public Fragment getItem(int i) {
        Fragment currentFragment = null;
        switch (i) {
            case 0:
                currentFragment = new HomeFragment();
                break;
            case 1:
                currentFragment = new FavoritesFragment();
                break;
            case 2:
                currentFragment = new CartFragment();
                break;
            default:
                currentFragment = new CartFragment();
        }
        return currentFragment;
    }

    //we return the number of tabs (starting from 1)
    @Override
    public int getCount() {
        return 3;
    }*/


    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
    //setting the icons and the titles for the tabs (this method could have been in mainActivity but i thought it would be better to be here)
    //1_we inflate the custom_tab text view we created by using LayoutInflater
    //2_we use setText method to assign the title for the tab & setCompoundDrawablesWithIntrinsicBounds to make the icon above the title
    //3_and finally we call getTabAt with the corresponding index to set the custom view we created
    public static void setupTabsIcons(TabLayout layout, Context context){
        //first tab
        TextView tabOne = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        tabOne.setText(R.string.fragment_home);
        //tabOne.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_home, 0, 0);
        layout.getTabAt(0).setCustomView(tabOne);
        //second tab
        TextView tabTwo = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        tabTwo.setText(R.string.fragment_favorites);
        //tabTwo.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite, 0, 0);
        layout.getTabAt(1).setCustomView(tabTwo);
        //third tab
        TextView tabThree = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        tabThree.setText(R.string.fragment_cart);
        //tabThree.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_drafts, 0, 0);
        layout.getTabAt(2).setCustomView(tabThree);

        layout.getTabAt(0).setIcon(R.drawable.ic_home);
        layout.getTabAt(1).setIcon(R.drawable.ic_favorite);
        layout.getTabAt(2).setIcon(R.drawable.ic_drafts);
    }
}
