package volosyuk.easybizcard.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import volosyuk.easybizcard.BusinessCardDetailActivity;
import volosyuk.easybizcard.R;
import volosyuk.easybizcard.models.BusinessCard;
import volosyuk.easybizcard.models.Report;
import volosyuk.easybizcard.utils.ReportRepository;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reports;
    private Context context;

    public ReportAdapter(List<Report> reports, Context context) {
        this.reports = reports;
        this.context = context;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        Report report = reports.get(position);
        holder.titleTextView.setText(report.getTitle());
        holder.messageTextView.setText(report.getMessage());
        holder.cardIdTextView.setText("Card ID: " + report.getCardId());
        holder.userIdTextView.setText("User ID: " + report.getUserId());
        holder.status.setText("Статус: " + report.getConsidered());

        holder.consideredButton.setOnClickListener(v -> {
            // Пометка жалобы как рассмотренной
            report.setConsidered(true);
            holder.reportRepository.updateReportStatus(report.getReportId(), report.getConsidered());
        });

        holder.cardIdTextView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BusinessCard.class);
            intent.putExtra(BusinessCardDetailActivity.EXTRA_CARD_ID, report.getCardId());
        });
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, messageTextView, cardIdTextView, userIdTextView, status;
        Button consideredButton;
        ReportRepository reportRepository;

        public ReportViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_title);
            messageTextView = itemView.findViewById(R.id.text_view_message);
            cardIdTextView = itemView.findViewById(R.id.text_view_card_id);
            userIdTextView = itemView.findViewById(R.id.text_view_user_id);
            consideredButton = itemView.findViewById(R.id.button_considered);
            status = itemView.findViewById(R.id.text_view_status);

            reportRepository = new ReportRepository();
        }
    }
}
