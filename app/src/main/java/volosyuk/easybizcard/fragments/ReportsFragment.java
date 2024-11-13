package volosyuk.easybizcard.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import volosyuk.easybizcard.BusinessCardDetailActivity;
import volosyuk.easybizcard.R;
import volosyuk.easybizcard.adapters.ReportAdapter;
import volosyuk.easybizcard.models.Report;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.ReportRepository;


public class ReportsFragment extends Fragment implements ReportAdapter.OnCardClickListener {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<Report> reportList;
    private ReportRepository reportRepository;
    private BusinessCardRepository businessCardRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.fragment_reports_recycler);

        reportRepository = new ReportRepository();
        businessCardRepository = new BusinessCardRepository(FirebaseFirestore.getInstance());

        // Инициализация данных (в реальном приложении данные могут загружаться из БД или сети)
        reportRepository.getAllReports().thenAccept(result -> {
            reportList = result;
            // Устанавливаем адаптер
            adapter = new ReportAdapter(reportList, requireContext(), this);
            recyclerView.setAdapter(adapter);
        });
    }


    @Override
    public void onCardClick(String cardId) {
        businessCardRepository.searchBusinessCardById(cardId).thenAccept(result -> {
            Intent intent = new Intent(requireContext(), BusinessCardDetailActivity.class);
            intent.putExtra(BusinessCardDetailActivity.EXTRA_CARD, result);
            startActivity(intent);
        });
    }
}