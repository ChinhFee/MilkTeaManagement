package com.example.milkteamanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.milkteamanagement.models.Evaluation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EvaluationAdapter extends RecyclerView.Adapter<EvaluationAdapter.ViewHolder> {

    private List<Evaluation> evaluations;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public EvaluationAdapter(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evaluation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Evaluation eval = evaluations.get(position);
        
        holder.tvCustomerName.setText(eval.getCustomerName());
        holder.tvEvalDate.setText(dateFormat.format(new Date(eval.getTimestamp())));
        holder.ratingBarSmall.setRating(eval.getRating());
        holder.tvOrderIdSmall.setText("Mã ĐH: #" + eval.getOrderId().substring(0, Math.min(eval.getOrderId().length(), 8)).toUpperCase());
        
        if (eval.getComment() == null || eval.getComment().isEmpty()) {
            holder.tvComment.setVisibility(View.GONE);
        } else {
            holder.tvComment.setVisibility(View.VISIBLE);
            holder.tvComment.setText(eval.getComment());
        }
    }

    @Override
    public int getItemCount() {
        return evaluations == null ? 0 : evaluations.size();
    }

    public void updateList(List<Evaluation> newList) {
        this.evaluations = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvEvalDate, tvOrderIdSmall, tvComment;
        RatingBar ratingBarSmall;

        ViewHolder(View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvEvalDate = itemView.findViewById(R.id.tvEvalDate);
            tvOrderIdSmall = itemView.findViewById(R.id.tvOrderIdSmall);
            tvComment = itemView.findViewById(R.id.tvComment);
            ratingBarSmall = itemView.findViewById(R.id.ratingBarSmall);
        }
    }
}
