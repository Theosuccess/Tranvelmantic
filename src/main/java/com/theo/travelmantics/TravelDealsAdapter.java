package com.theo.travelmantics;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TravelDealsAdapter extends RecyclerView.Adapter<TravelDealsAdapter.TravelDealsViewHolder> {
    private ArrayList<TravelDeal> travelDealArrayList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;

    public TravelDealsAdapter(TravelActivity activity){
        FirebaseUtil.openFbReference("traveldeals", activity);
        firebaseDatabase=FirebaseUtil.firebaseDatabase;
        databaseReference=FirebaseUtil.databaseReference;
        //travelDealArrayList =FirebaseUtil.travelDeals;
        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td=dataSnapshot.getValue(TravelDeal.class);
                //fetch random generated keys by Firebase
                td.setId(dataSnapshot.getKey());
                travelDealArrayList.add(td);
                //notify observers items have been inserted to update UI.
                notifyItemInserted(travelDealArrayList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);
        this.travelDealArrayList = FirebaseUtil.travelDeals;
    }
    @NonNull
    @Override
    public TravelDealsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row, parent, false);
        return new TravelDealsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelDealsViewHolder holder, int position) {
        TravelDeal deal= travelDealArrayList.get(position);
        holder.tvTitle.setText(deal.getTitle());
        holder.tvDescription.setText(deal.getDescription());
        holder.tvPrice.setText(deal.getPrice());
        holder.showImage(deal.getImageUrl());
    }

    @Override
    public int getItemCount() {
        return travelDealArrayList.size();
    }

    public class TravelDealsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvPrice;
        private final ImageView imageDealImage;

        public TravelDealsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imageDealImage=itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            int position=getAdapterPosition();
            Log.d("Click", String.valueOf(position));
            TravelDeal selectedDeal= travelDealArrayList.get(position);
            Intent intent=new Intent(view.getContext(), DealActivity .class);
            intent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(intent);
        }

        private void showImage(String imageUrl) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.with(imageDealImage.getContext())
                        .load(imageUrl)
                        .resize(240, 260)
                        .centerCrop()
                        .into(imageDealImage);
            }
        }
    }
}
