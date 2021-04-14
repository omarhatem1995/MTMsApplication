package com.example.mtmsapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mtmsapplication.R;
import com.example.mtmsapplication.model.SourceLocation;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AddressList_Adapter extends RecyclerView.Adapter<AddressList_Adapter.AddresspListViewHolder> {

    private final Context mContext;
    private List<SourceLocation> sourceLocation;
    private static int VIEW_TYPE_ITEM = 0;
    private static int VIEW_TYPE_DIVIDER = 1;
    private final OnAddressItemClickListener onAddressItemClickListener;

    public AddressList_Adapter(Context mContext, List<SourceLocation> sourceLocation
            , OnAddressItemClickListener onAddressItemClickListener) {
        this.mContext = mContext;
        this.sourceLocation = sourceLocation;
        this.onAddressItemClickListener = onAddressItemClickListener;
    }

    @NonNull
    @Override
    public AddresspListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(mContext).inflate(
                viewType == VIEW_TYPE_ITEM ? R.layout.places_list_item : R.layout.recyclerview_divider
                , parent, false);
        AddresspListViewHolder viewHolder = new AddresspListViewHolder(v, viewType);

        return viewHolder;

    }

    @Override
    public int getItemCount() {
        if (sourceLocation != null)
            return sourceLocation.size() * 2;
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0)
            return VIEW_TYPE_ITEM;

        return VIEW_TYPE_DIVIDER;
    }

    @Override
    public void onBindViewHolder(@NonNull AddresspListViewHolder holder, int position) {

        if (holder.mViewType == VIEW_TYPE_ITEM)
            holder.addressName.setText(sourceLocation.get(position / 2).getName());

        Log.d("dododod", position / 2 + "");
        if (holder.constraintLayout != null)
            holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("dododod", position / 2 + "");

                    onAddressItemClickListener.onItemClick(position / 2);
                }
            });

    }

    public static class AddresspListViewHolder extends RecyclerView.ViewHolder {

        private TextView addressName;
        public int mViewType;
        private ConstraintLayout constraintLayout;

        public AddresspListViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            mViewType = viewType;
            if (mViewType == VIEW_TYPE_ITEM) {
                addressName = itemView.findViewById(R.id.address_name);
                constraintLayout = itemView.findViewById(R.id.view_constraintlayout_item);
            }
        }
    }
}
