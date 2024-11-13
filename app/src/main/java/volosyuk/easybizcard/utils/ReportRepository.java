package volosyuk.easybizcard.utils;

import com.google.firebase.firestore.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import volosyuk.easybizcard.models.Report;

public class ReportRepository {

    private final CollectionReference reportsRef;

    public ReportRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        reportsRef = db.collection("reports");
    }

    // Метод для добавления нового репорта
    public CompletableFuture<Void> addReport(String title, String message, String cardId, String userId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String reportId = reportsRef.document().getId();
        Report report = new Report(reportId, cardId, userId, title, message, false);

        reportsRef.document(reportId).set(report)
                .addOnSuccessListener(documentReference -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // Метод для обновления статуса репорта на "рассмотренный"
    public CompletableFuture<Void> updateReportStatus(String reportId, boolean considered) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        reportsRef.document(reportId)
                .update("considered", considered)
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // Метод для загрузки всех репортов
    public CompletableFuture<List<Report>> getAllReports() {
        CompletableFuture<List<Report>> future = new CompletableFuture<>();
        reportsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Report> reports = querySnapshot.toObjects(Report.class);
                    future.complete(reports);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // Метод для загрузки всех рассмотренных репортов
    public CompletableFuture<List<Report>> getConsideredReports() {
        CompletableFuture<List<Report>> future = new CompletableFuture<>();
        reportsRef.whereEqualTo("considered", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Report> reports = querySnapshot.toObjects(Report.class);
                    future.complete(reports);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // Метод для загрузки всех нерассмотренных репортов
    public CompletableFuture<List<Report>> getUnconsideredReports() {
        CompletableFuture<List<Report>> future = new CompletableFuture<>();
        reportsRef.whereEqualTo("considered", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Report> reports = querySnapshot.toObjects(Report.class);
                    future.complete(reports);
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    // Метод для удаления репорта
    public CompletableFuture<Void> deleteReport(String reportId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        reportsRef.document(reportId)
                .delete()
                .addOnSuccessListener(unused -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}