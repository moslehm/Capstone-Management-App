package ca.macewan.capstone.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ca.macewan.capstone.ProfAccepted;
import ca.macewan.capstone.ProfAll;
import ca.macewan.capstone.ProfInvited;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new ProfInvited();
        }
        return new ProfAll();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
