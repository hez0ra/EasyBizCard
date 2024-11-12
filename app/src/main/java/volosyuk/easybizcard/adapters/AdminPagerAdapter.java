package volosyuk.easybizcard.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import volosyuk.easybizcard.fragments.ReportsFragment;
import volosyuk.easybizcard.fragments.ModerationFragment;
import volosyuk.easybizcard.fragments.UserManagementFragment;

public class AdminPagerAdapter extends FragmentStateAdapter {

    public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ModerationFragment();
            case 1:
                return new ReportsFragment();
            case 2:
                return new UserManagementFragment();
            default:
                return new ModerationFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Количество вкладок
    }
}
