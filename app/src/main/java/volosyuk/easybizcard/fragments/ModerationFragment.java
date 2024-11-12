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
import volosyuk.easybizcard.adapters.ReportAdapter;
import volosyuk.easybizcard.utils.ReportRepository;

public class ModerationFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportRepository reportRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        recyclerView = view.findViewById(R.id.reports_recyclerView);

        reportRepository = new ReportRepository();
        reportRepository.getAllReports().thenAccept(result -> {
            recyclerView.setAdapter(new ReportAdapter(result, requireContext()));
        });

        return view;
    }

}
