package volosyuk.easybizcard.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import volosyuk.easybizcard.BusinessCardViewActivity;
import volosyuk.easybizcard.R;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.utils.UserRepository;

public class MyCardsAdapter extends RecyclerView.Adapter<MyCardsAdapter.MyCardsViewHolder> {

    private Context context;
    private List<BusinessCard> businessCards;

    public MyCardsAdapter(Context context, List<BusinessCard> businessCards) {
        this.context = context;
        this.businessCards = businessCards;
    }

    @Override
    public MyCardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Инфляция макета для элемента списка
        View view = LayoutInflater.from(context).inflate(R.layout.item_business_card, parent, false);
        return new MyCardsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyCardsViewHolder holder, int position) {
        FirebaseAuth mAuth =  FirebaseAuth.getInstance();
        UserRepository userRepository = new UserRepository(FirebaseFirestore.getInstance(), mAuth);
        BusinessCard card = businessCards.get(position);

        userRepository.isActiveUserAdmin().thenAccept(result -> {
            if(result){
                holder.cardStatus.setVisibility(View.VISIBLE);
            }
        });

        if (Objects.equals(card.getUser_id(), mAuth.getCurrentUser().getUid())){
            holder.cardStatus.setVisibility(View.VISIBLE);
        }

        // Заполнение данных для каждого элемента
        holder.cardName.setText(card.getTitle());
        switch (card.getStatus()){
            case PENDING:
                holder.cardStatus.setText("На рассмотрении");
                break;
            case APPROVED:
                holder.cardStatus.setText("Одобрено");
                break;
            case REJECTED:
                holder.cardStatus.setText("Отклонено");
                break;
            default:
                break;
        }

        // Форматируем дату: "дд МММ гггг HH:mm"
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        holder.cardDate.setText(sdf.format(card.getCreated_at()));

        userRepository.getUserData().thenAccept(result -> {
            holder.cardUserEmail.setText(result.getString("email"));
        });

        // Добавляем обработчик клика на элемент
        holder.itemView.setOnClickListener(v -> {
            // Создаем Intent для открытия BusinessCardViewActivity
            Intent intent = new Intent(context, BusinessCardViewActivity.class);

            // Добавляем id визитки в extras
            intent.putExtra(BusinessCardViewActivity.EXTRA_ID, card.getId());

            // Запускаем активность
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return businessCards.size();  // Количество элементов в списке
    }

    // Вьюхолдер для каждого элемента списка
    public static class MyCardsViewHolder extends RecyclerView.ViewHolder {

        TextView cardName, cardStatus, cardDate, cardUserEmail;

        public MyCardsViewHolder(View itemView) {
            super(itemView);
            cardName = itemView.findViewById(R.id.card_name);
            cardStatus = itemView.findViewById(R.id.card_status);
            cardDate = itemView.findViewById(R.id.card_date);
            cardUserEmail = itemView.findViewById(R.id.card_user_email);
        }
    }
}
