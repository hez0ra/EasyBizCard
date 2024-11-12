package volosyuk.easybizcard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import volosyuk.easybizcard.R;
import volosyuk.easybizcard.adapters.ReportAdapter;
import volosyuk.easybizcard.models.Report;
import volosyuk.easybizcard.utils.ReportRepository;

public class ReportsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Report> reportList;
    private ReportAdapter adapter;
    private ReportRepository reportRepository;

    public ReportsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reportRepository = new ReportRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        recyclerView = view.findViewById(R.id.reports_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportRepository.getAllReports().thenAccept(result -> {
            reportList = result;
            adapter = new ReportAdapter(reportList, getContext());
            recyclerView.setAdapter(adapter);
        });

        return view;
    }
}