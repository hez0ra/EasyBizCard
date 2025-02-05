package volosyuk.easybizcard.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import volosyuk.easybizcard.R;
import volosyuk.easybizcard.adapters.MyCardsAdapter;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.utils.BusinessCardRepository;



public class CardsFragment extends Fragment {

    private RecyclerView recyclerView;
    private BusinessCardRepository businessCardRepository;
    private List<BusinessCard> cardsList;
    private MyCardsAdapter adapter;
    private boolean showPending = true;
    private boolean showApproved = true;
    private boolean showRejected = true;

    public CardsFragment(){

    }

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
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);

        btnFilter.setOnClickListener(v -> showFilterMenu(v));

        updateInformation();
    }


    @Override
    public void onResume() {
        super.onResume();
        updateInformation();
    }
    private void showFilterMenu(View view) {
        // Применяем кастомный стиль
        Context wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        Menu menu = popupMenu.getMenu();

        // Добавляем пункты меню
        menu.add(Menu.NONE, 1, Menu.NONE, BusinessCard.STATUS_LABELS.get(BusinessCard.Status.PENDING))
                .setCheckable(true)
                .setChecked(showPending);
        menu.add(Menu.NONE, 2, Menu.NONE, BusinessCard.STATUS_LABELS.get(BusinessCard.Status.APPROVED))
                .setCheckable(true)
                .setChecked(showApproved);
        menu.add(Menu.NONE, 3, Menu.NONE, BusinessCard.STATUS_LABELS.get(BusinessCard.Status.REJECTED))
                .setCheckable(true)
                .setChecked(showRejected);

        // Обработка кликов
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    showPending = !showPending;
                    break;
                case 2:
                    showApproved = !showApproved;
                    break;
                case 3:
                    showRejected = !showRejected;
                    break;
            }
            updateInformation();
            return false;
        });

        // Показываем меню
        popupMenu.show();
    }

    private void updateInformation() {
        businessCardRepository.getAllBusinessCards().thenAccept(result -> {
            cardsList = result;

            // Фильтрация по выбранным состояниям
            cardsList = cardsList.stream()
                    .filter(card -> (showPending && card.getStatus() == BusinessCard.Status.PENDING) ||
                            (showApproved && card.getStatus() == BusinessCard.Status.APPROVED) ||
                            (showRejected && card.getStatus() == BusinessCard.Status.REJECTED))
                    .collect(Collectors.toList());

            adapter = new MyCardsAdapter(requireContext(), cardsList);
            recyclerView.setAdapter(adapter);
        });
    }
}