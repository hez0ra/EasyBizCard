package volosyuk.easybizcard.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import volosyuk.easybizcard.BusinessCardDetailActivity;
import volosyuk.easybizcard.R;
import volosyuk.easybizcard.models.BusinessCard;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private List<BusinessCard> cardList;

    public CardAdapter(Context context, List<BusinessCard> cardList) {
        this.context = context;
        this.cardList = cardList;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_business_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        BusinessCard card = cardList.get(position);
        holder.title.setText(card.getTitle());
        holder.description.setText(card.getDescription());

        // Загружаем изображение с помощью Glide
        Glide.with(context)
                .load(card.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageView);

        // Обработчик клика
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BusinessCardDetailActivity.class);
            intent.putExtra("businessCard", card);  // Передаем объект визитки
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView imageView;

        public CardViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.card_title);
            description = itemView.findViewById(R.id.card_description);
            imageView = itemView.findViewById(R.id.card_image);
        }
    }
}
