package volosyuk.easybizcard.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import volosyuk.easybizcard.BusinessCardViewActivity;
import volosyuk.easybizcard.R;
import volosyuk.easybizcard.models.BusinessCard;

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
        BusinessCard card = businessCards.get(position);

        // Заполнение данных для каждого элемента
        holder.cardId.setText(card.getId());
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

        // Отображаем user_id
        holder.cardUserId.setText(card.getUser_id());  // Отображаем user_id

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

        TextView cardId, cardStatus, cardDate, cardUserId;

        public MyCardsViewHolder(View itemView) {
            super(itemView);
            cardId = itemView.findViewById(R.id.card_id);
            cardStatus = itemView.findViewById(R.id.card_status);
            cardDate = itemView.findViewById(R.id.card_date);
            cardUserId = itemView.findViewById(R.id.card_user_id);  // Поле для отображения user_id
        }
    }
}
