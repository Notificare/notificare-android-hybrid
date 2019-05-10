package re.notifica.demo;

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.collection.ArraySet;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Date;
import java.util.Set;

import re.notifica.model.NotificareInboxItem;
import re.notifica.util.AssetLoader;
import re.notifica.util.Log;

public class InboxListAdapter extends ListAdapter<NotificareInboxItem, InboxListAdapter.InboxListViewHolder> {

    private final static String TAG = InboxListAdapter.class.getSimpleName();
    private InboxItemClickListener itemClickListener;
    private InboxItemSelectionListener itemSelectedListener;
    private Set<NotificareInboxItem> selectedItems = new ArraySet<>();

    InboxListAdapter(InboxItemClickListener itemClickListener, InboxItemSelectionListener itemSelectedListener) {
        super(DIFF_CALLBACK);
        this.itemClickListener = itemClickListener;
        this.itemSelectedListener = itemSelectedListener;
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InboxListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new InboxListViewHolder(inflater.inflate(R.layout.inbox_list_cell, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InboxListViewHolder holder, int position) {
        holder.bind(getItem(position), position, itemClickListener, itemSelectedListener);
    }

    interface InboxItemClickListener {
        void onItemCLick(NotificareInboxItem item, int position);
    }

    interface InboxItemSelectionListener {
        void onItemSelected(NotificareInboxItem item, boolean selected);
    }

    /**
     * Callback for comparing items
     */
    public static final DiffUtil.ItemCallback<NotificareInboxItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<NotificareInboxItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull NotificareInboxItem oldItem, @NonNull NotificareInboxItem newItem) {
            Log.i(TAG, "comparing " + oldItem.getItemId() + " to " + newItem.getItemId());
            return oldItem.getItemId().equals(newItem.getItemId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull NotificareInboxItem oldItem, @NonNull NotificareInboxItem newItem) {
            Log.i(TAG, "comparing contents of " + oldItem.getItemId() + " to " + newItem.getItemId());
            return false; // makes sure time is always updated when list changes
//            return oldItem.getMessage().equals(newItem.getMessage()) && oldItem.getStatus().equals(newItem.getStatus());
        }

    };

    /**
     * ViewHolder for InboxList item
     */
    class InboxListViewHolder extends RecyclerView.ViewHolder {

        TextView dateView;
        TextView titleView;
        TextView messageView;
        ImageView deleteIconView;
        ImageView imageView;

        InboxListViewHolder(View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.inbox_date);
            titleView = itemView.findViewById(R.id.inbox_title);
            messageView = itemView.findViewById(R.id.inbox_message);
            deleteIconView = itemView.findViewById(R.id.inbox_delete);
            imageView = itemView.findViewById(R.id.inbox_image);
        }

        void bind(NotificareInboxItem item, int position, InboxItemClickListener itemClickListener, InboxItemSelectionListener itemSelectedListener) {
            dateView.setText(DateUtils.getRelativeTimeSpanString(item.getTimestamp().getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS));
            titleView.setText(item.getTitle());
            messageView.setText(item.getMessage());
            dateView.setTextColor(Color.BLACK);
            titleView.setTextColor(Color.BLACK);
            messageView.setTextColor(Color.BLACK);
            if (item.getStatus()) {
                dateView.setTextColor(Color.GRAY);
                titleView.setTextColor(Color.GRAY);
                messageView.setTextColor(Color.GRAY);
                imageView.setImageAlpha(128);
            }
            if (item.getAttachment() != null && item.getAttachment().getUri() != null) {
                AssetLoader.loadImage(item.getAttachment().getUri(), imageView);
            } else {
                imageView.setImageResource(R.drawable.no_attachment);
            }
            if (selectedItems.contains(item)) {
                //deleteIconView.setVisibility(View.VISIBLE);
                itemView.setSelected(true);
            } else {
                //deleteIconView.setVisibility(View.GONE);
                itemView.setSelected(false);
            }
            itemView.setOnClickListener(v -> {
                itemClickListener.onItemCLick(item, position);
            });

            itemView.setOnLongClickListener(v -> {
                if (v.isSelected()) {
//                    deleteIconView.setVisibility(View.GONE);
                    v.setSelected(false);
                    selectedItems.remove(item);
                    itemSelectedListener.onItemSelected(item, false);
                } else {
//                    deleteIconView.setVisibility(View.VISIBLE);
                    v.setSelected(true);
                    selectedItems.add(item);
                    itemSelectedListener.onItemSelected(item, true);
                }
                return true;
            });
        }
    }

}
