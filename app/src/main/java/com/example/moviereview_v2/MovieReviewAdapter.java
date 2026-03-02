package com.example.moviereview_v2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.ViewHolder> {

    private Context context;
    private List<MoviewReview> reviewList;
    private OnReviewActionListener listener;

    public interface OnReviewActionListener {
        void onEdit(MoviewReview review);
        void onDelete(MoviewReview review);
    }

    public MovieReviewAdapter(Context context, List<MoviewReview> reviewList, OnReviewActionListener listener) {
        this.context = context;
        this.reviewList = reviewList;
        this.listener = listener;
    }

    public void setReviews(List<MoviewReview> reviews) {
        this.reviewList = reviews;
        notifyDataSetChanged();
    }

    public void addReview(MoviewReview review) {
        this.reviewList.add(review);
        notifyItemInserted(reviewList.size() - 1);
    }

    public void updateReview(MoviewReview updatedReview) {
        for (int i = 0; i < reviewList.size(); i++) {
            if (reviewList.get(i).getId() == updatedReview.getId()) {
                reviewList.set(i, updatedReview);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void deleteReview(MoviewReview review) {
        int position = reviewList.indexOf(review);
        if (position != -1) {
            reviewList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public MovieReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_review_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieReviewAdapter.ViewHolder holder, int position) {
        MoviewReview review = reviewList.get(position);
        holder.movieName.setText(review.getmovie_name());
        holder.year.setText("Year: " + review.getYear());
        holder.duration.setText("Duration: " + review.getDuration() + " mins");
        holder.rating.setText("Rating: " + review.getRatingBar());
        holder.gender.setText("Gender: " + (review.isFemale().equals("Yes") ? "Female" : "Male"));
        holder.tech.setText("Java: " + review.getJavaMovie() +
                ", MSSQL: " + review.getMssqlMovie() +
                ", React: " + review.getReactMovie() +
                ", Python: " + review.getPythonMovie());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(review));

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Review")
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Yes", (dialog, which) -> listener.onDelete(review))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView movieName, year, duration, rating, gender, tech;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            movieName = itemView.findViewById(R.id.txtMovieName);
            year = itemView.findViewById(R.id.txtYear);
            duration = itemView.findViewById(R.id.txtDuration);
            rating = itemView.findViewById(R.id.txtRating);
            gender = itemView.findViewById(R.id.txtGender);
            tech = itemView.findViewById(R.id.txtTechnologies);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
