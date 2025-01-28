package volosyuk.easybizcard.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import volosyuk.easybizcard.R;
import volosyuk.easybizcard.models.SocialNetwork;

public class SocialNetworkAdapter extends RecyclerView.Adapter<SocialNetworkAdapter.ViewHolder> {

    private final Context context;
    private final List<SocialNetwork> socialNetworks;
    private final OnSocialNetworkClickListener clickListener;

    public interface OnSocialNetworkClickListener {
        void onSocialNetworkClick(SocialNetwork socialNetwork);
    }

    public SocialNetworkAdapter(Context context, List<SocialNetwork> socialNetworks, OnSocialNetworkClickListener clickListener) {
        this.context = context;
        this.socialNetworks = socialNetworks;
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_social_network, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SocialNetwork socialNetwork = socialNetworks.get(position);
        holder.icon.setImageResource(socialNetwork.getIconResId());
        holder.name.setText(socialNetwork.getName());

        // Обработка клика
        holder.addButton.setOnClickListener(v -> clickListener.onSocialNetworkClick(socialNetwork));
    }

    @Override
    public int getItemCount() {
        return socialNetworks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        Button addButton;

        public ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.social_icon);
            name = itemView.findViewById(R.id.social_name);
            addButton = itemView.findViewById(R.id.add_button);
        }
    }
}
