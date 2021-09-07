/*
 * Copyright 2016-2019 Adrian Cotfas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.apps.adrcotfas.goodtime.statistics.AllSessions;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apps.adrcotfas.goodtime.database.Label;
import com.apps.adrcotfas.goodtime.R;
import com.apps.adrcotfas.goodtime.database.Session;
import com.apps.adrcotfas.goodtime.util.ThemeHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AllSessionsAdapter extends RecyclerView.Adapter<AllSessionsViewHolder> {
    private WeakReference<Context> mContext;
    public List<Session> mEntries = new ArrayList<>();
    public List<Long> mSelectedEntries = new ArrayList<>();
    private final List<Label> mLabels;

    AllSessionsAdapter(List<Label> labels) {
        // this and the override of getItemId are to avoid clipping in the view
        setHasStableIds(true);
        mLabels = labels;
    }

    @NonNull
    @Override
    public AllSessionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = new WeakReference<>(parent.getContext());
        LayoutInflater inflater = LayoutInflater.from(mContext.get());
        View statusContainer = inflater.inflate(R.layout.statistics_all_sessions_row, parent, false);
        return new AllSessionsViewHolder(statusContainer);
    }

    @Override
    public void onBindViewHolder(@NonNull AllSessionsViewHolder holder, int position) {
        Session session = mEntries.get(position);

        holder.bind(session, ColorStateList.valueOf(getColor(session.label)));

        holder.rowOverlay.setVisibility(
                mSelectedEntries.contains(mEntries.get(position).id) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    @Override
    public long getItemId(int position) {
        Session session = mEntries.get(position);
        return session.id;
    }

    private int getColor(String label) {
        int color = 0;
        for (Label l : mLabels) {
            if (l.title.equals(label)) {
                color = ThemeHelper.getColor(mContext.get(), l.colorId);
                break;
            }
        }
        return color;
    }

    public List<Label> getLabels() {
        return mLabels;
    }

    public void setData(List<Session> newSessions) {
        if (mEntries != null) {
            PostDiffCallback postDiffCallback = new PostDiffCallback(mEntries, newSessions);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(postDiffCallback);

            mEntries.clear();
            mEntries.addAll(newSessions);
            diffResult.dispatchUpdatesTo(this);
        } else {
            // first initialization
            mEntries = newSessions;
        }
    }

    public void setSelectedItems(List<Long> selectedItems) {
        this.mSelectedEntries = selectedItems;
        notifyDataSetChanged();
    }

    static class PostDiffCallback extends DiffUtil.Callback {

        private final List<Session> oldSessions, newSessions;

        private PostDiffCallback(List<Session> oldSessions, List<Session> newSessions) {
            this.oldSessions = oldSessions;
            this.newSessions = newSessions;
        }

        @Override
        public int getOldListSize() {
            return oldSessions.size();
        }

        @Override
        public int getNewListSize() {
            return newSessions.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldSessions.get(oldItemPosition).id == newSessions.get(newItemPosition).id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldSessions.get(oldItemPosition).equals(newSessions.get(newItemPosition));
        }
    }
}