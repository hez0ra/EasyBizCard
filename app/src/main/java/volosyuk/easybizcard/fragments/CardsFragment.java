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
import volosyuk.easybizcard.adapters.CardAdapter;
import volosyuk.easybizcard.adapters.ReportAdapter;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.models.Report;
import volosyuk.easybizcard.utils.BusinessCardRepository;
import volosyuk.easybizcard.utils.ReportRepository;


public class CardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private BusinessCardRepository businessCardRepository;
    private List<BusinessCard> cardsList;
    private CardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.fragment_cards_recycler);
        businessCardRepository = new BusinessCardRepository(FirebaseFirestore.getInstance());

        // Инициализация данных (в реальном приложении данные могут загружаться из БД или сети)
        businessCardRepository.getAllBusinessCards().thenAccept(result -> {
            cardsList = result;
            // Устанавливаем адаптер
            adapter = new CardAdapter(requireContext(), cardsList);
            recyclerView.setAdapter(adapter);
        });
    }
}