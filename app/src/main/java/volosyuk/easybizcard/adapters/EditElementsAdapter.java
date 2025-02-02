package volosyuk.easybizcard.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import volosyuk.easybizcard.R;
import volosyuk.easybizcard.models.BusinessCardElement;

public class EditElementsAdapter extends RecyclerView.Adapter<EditElementsAdapter.ViewHolder> {

    public interface OnElementActionListener {
        void onEdit(int position);
        void onDelete(int position);
        void onMoveUp(int position);
        void onMoveDown(int position);
    }

    private List<BusinessCardElement> elements;
    private OnElementActionListener listener;

    public EditElementsAdapter(List<BusinessCardElement> elements, OnElementActionListener listener) {
        this.elements = elements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_element_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusinessCardElement element = elements.get(position);
        holder.elementName.setText(getElementTitle(element));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(position));
        holder.btnMoveUp.setOnClickListener(v -> listener.onMoveUp(position));
        holder.btnMoveDown.setOnClickListener(v -> listener.onMoveDown(position));

        // Скрыть кнопки перемещения для крайних позиций
        holder.btnMoveUp.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.btnMoveDown.setVisibility(position < elements.size()-1 ? View.VISIBLE : View.INVISIBLE);
    }

    private String getElementTitle(BusinessCardElement element) {
        switch (element.getType()) {
            case "text": return "Текст: " + element.getText();
            case "image": return "Изображение";
            case "phone": return "Номер телефона: " + element.getText();
            case "email": return "Email: " + element.getText();
            case "link": return "Ссылка: " + element.getHyperText();
            case "socialMedia": return "Соц. сети";
            default: return "Element";
        }
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView elementName;
        ImageButton btnEdit, btnDelete, btnMoveUp, btnMoveDown;

        ViewHolder(View itemView) {
            super(itemView);
            elementName = itemView.findViewById(R.id.element_name);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnMoveUp = itemView.findViewById(R.id.btn_move_up);
            btnMoveDown = itemView.findViewById(R.id.btn_move_down);
        }
    }
}