package volosyuk.easybizcard.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import volosyuk.easybizcard.R;

public class ModerationFragment extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moderation, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);

        // Настройка RecyclerView для отображения визиток
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        // Логика загрузки визиток и отображения в RecyclerView
    }

}
