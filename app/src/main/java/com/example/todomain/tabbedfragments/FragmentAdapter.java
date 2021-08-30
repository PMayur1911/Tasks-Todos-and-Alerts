package com.example.todomain.tabbedfragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class FragmentAdapter extends FragmentStateAdapter {

    ArrayList<Fragment> fragmentList;

    public FragmentAdapter(@NonNull FragmentManager fragmentManager
            , @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragmentList = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment){
        fragmentList.add(fragment);
    }

    public Fragment getFragment(int position){return fragmentList.get(position);}
}
