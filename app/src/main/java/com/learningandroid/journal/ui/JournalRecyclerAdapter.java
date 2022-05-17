package com.learningandroid.journal.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.learningandroid.journal.JournalListActivity;
import com.learningandroid.journal.PostJournalActivity;
import com.learningandroid.journal.R;
import com.learningandroid.journal.model.Journal;
import com.squareup.picasso.Picasso;

import java.util.List;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;

    private static Dialog infoDialog;
    private CardView delete;
    private ProgressBar progressBarDelete;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");


    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                    .inflate(R.layout.journal_row, parent, false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Journal journal = journalList.get(position);
        String imageUrl;

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());
        imageUrl = journal.getImageUri();

        /*
            use Picasso library to download image from the url
         */

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.bk_photo_1)
                .fit()
                .into(holder.image);

        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal
                .getTimeAdded()
                .getSeconds() * 1000);

        holder.dateAdded.setText(timeAgo);

        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                    context.startActivity();
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String body = journal.getThought();
                String sub = journal.getTitle();
                myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
                myIntent.putExtra(Intent.EXTRA_TEXT,body);
                context.startActivity(Intent.createChooser(myIntent, "Share Using"));
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostJournalActivity.class);
                intent.putExtra("toUpdate", 1);
                intent.putExtra("journal", (Parcelable) journal);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title,
            thoughts,
            dateAdded,
            name;

        public ImageView image;

        String userID, username;
        public ImageButton shareButton;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thougth_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
            shareButton = itemView.findViewById(R.id.journal_row_share_button);



            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.d("check", "onLongClick: " + "very loong");
                    infoDialog = new Dialog(context);
                    infoDialog.setContentView(R.layout.delete_popup);
                    infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    infoDialog.show();

                    delete = infoDialog.findViewById(R.id.card_delete);
                    progressBarDelete = infoDialog.findViewById(R.id.progress_delete);

                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            progressBarDelete.setVisibility(View.VISIBLE);
//                            deleteJournal(getAdapterPosition());
                            final int position = getAdapterPosition();
                            final Journal journal = journalList.get(position);
                            final String userId = journal.getUserId();
                            final String url=journal.getImageUri();
                            final Timestamp timeAdded = journal.getTimeAdded();

                            collectionReference
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("timeAdded", timeAdded)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                            for (QueryDocumentSnapshot snapshot : value) {
//                                                Log.d(TAG, snapshot.getId());
                                                snapshot.getReference().delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                progressBarDelete.setVisibility(View.INVISIBLE);
                                                                Toast.makeText(context, "Journal Deleted", Toast.LENGTH_SHORT).show();

                                                                final Intent intent = new Intent(context, JournalListActivity.class);
                                                                context.startActivity(intent);
                                                            }
                                                        });
                                            }
                                        }
                                    });
                            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                            photoRef.delete();


                        }
                    });
                    return true;
                }
            });
        }
    }
}