package re.notifica.demo;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.database.NotificareDatabase;
import re.notifica.model.NotificareInboxItem;
import re.notifica.model.NotificareNotification;
import re.notifica.util.Log;


/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment implements Notificare.OnNotificationReceivedListener {

    private static final String TAG = InboxFragment.class.getSimpleName();

    private ListView listView;
    private Set<NotificareInboxItem> itemsToRemove;
    protected ArrayAdapter<NotificareInboxItem> inboxListAdapter;
    private ActionMode mActionMode;
    private Menu mOptionsMenu;

    private Typeface lightFont;
    private Typeface regularFont;

    public InboxFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InboxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_inbox);
        }
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        listView = rootView.findViewById(R.id.inboxList);

        lightFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Light.ttf");
        regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Regular.ttf");

        itemsToRemove = new HashSet<>();
        inboxListAdapter = new InboxListAdapter(getActivity(), R.layout.inbox_list_cell);
        listView.setAdapter(inboxListAdapter);

        if (Notificare.shared().getInboxManager() != null) {
            LiveData<SortedSet<NotificareInboxItem>> temp = Notificare.shared().getInboxManager().getObservableItems();
            temp.observe(this, notificareInboxItems -> {
                Log.i(TAG, "inbox changed");
                inboxListAdapter.clear();
                inboxListAdapter.addAll(notificareInboxItems);
            });
        }

        TextView emptyText = rootView.findViewById(R.id.empty_message);
        listView.setEmptyView(emptyText);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.findViewById(R.id.inbox_delete).getVisibility() == View.VISIBLE) {
                    //uncheck
                    view.findViewById(R.id.inbox_delete).setVisibility(View.INVISIBLE);
                    itemsToRemove.remove(inboxListAdapter.getItem(position));

                } else {
                    //check
                    itemsToRemove.add(inboxListAdapter.getItem(position));
                    view.findViewById(R.id.inbox_delete).setVisibility(View.VISIBLE);
                }

                if (mActionMode != null) {
                    return true;
                }

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = getActivity().startActionMode(mActionModeCallback);

                view.setSelected(true);
                return true;
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NotificareInboxItem item = inboxListAdapter.getItem(position);
                if (item != null) {
                    Notificare.shared().openInboxItem(getActivity(), item);
                }
//                refreshInbox();
            }
        });

        return rootView;
    }

    public void refreshInbox(){
        if (inboxListAdapter != null) {
            inboxListAdapter.clear();
            if (Notificare.shared().getInboxManager() != null) {
                inboxListAdapter.addAll(Notificare.shared().getInboxManager().getItems());
            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mOptionsMenu = menu;
        inflater.inflate(R.menu.inbox, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_clear:
                if (Notificare.shared().getInboxManager() != null) {
                    Notificare.shared().getInboxManager().clearInbox(new NotificareCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            Log.i(TAG, "inbox cleared");
                        }

                        @Override
                        public void onError(NotificareError notificareError) {
                            Log.e(TAG, "Failed to clear inbox: " + notificareError.getMessage());
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Notificare.shared().addNotificationReceivedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Notificare.shared().removeNotificationReceivedListener(this);
    }

    @Override
    public void onNotificationReceived(NotificareNotification notificareNotification) {
        inboxListAdapter.clear();
        if (Notificare.shared().getInboxManager() != null) {
            inboxListAdapter.addAll(Notificare.shared().getInboxManager().getItems());
        }
    }


    /**
     * List adapter to show a row per beacon
     */
    private class InboxListAdapter extends ArrayAdapter<NotificareInboxItem> {

        private Activity context;
        private int resource;

        InboxListAdapter(Activity context, int resource) {
            super(context, resource);
            this.context = context;
            this.resource = resource;
        }


        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = convertView;
            if (rowView == null) {
                rowView = inflater.inflate(resource, null, true);
            }
            TextView dateView = rowView.findViewById(R.id.inbox_date);
            TextView messageView = rowView.findViewById(R.id.inbox_message);
            ImageView deleteIconView = rowView.findViewById(R.id.inbox_delete);

            NotificareInboxItem item = getItem(position);

            if (item != null) {
                dateView.setText(DateUtils.getRelativeTimeSpanString(item.getTimestamp().getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS));
                messageView.setText(item.getNotification().getMessage());
                dateView.setTextColor(Color.BLACK);
                messageView.setTextColor(Color.BLACK);
                if (item.getStatus()) {
                    dateView.setTextColor(Color.GRAY);
                    messageView.setTextColor(Color.GRAY);
                }
                if (itemsToRemove != null && itemsToRemove.contains(item)) {
                    deleteIconView.setVisibility(View.VISIBLE);
                }
            }
            return rowView;
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.inbox_action, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_trash:

                    for (NotificareInboxItem itemToRemove : itemsToRemove) {
                        Notificare.shared().getInboxManager().removeItem(itemToRemove, new NotificareCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                //Log.d(TAG, "Removed inboxItem");
                                int badgeCount = Notificare.shared().getInboxManager().getUnreadCount();
                                ShortcutBadger.applyCount(getActivity().getApplicationContext(), badgeCount);
                            }

                            @Override
                            public void onError(NotificareError notificareError) {
                                //Log.e(TAG, "Failed to remove inboxItem: " + notificareError.getMessage());
                            }
                        });
//                        int position = inboxListAdapter.getPosition(itemToRemove);
//                        listView.getChildAt(position).findViewById(R.id.inbox_delete).setVisibility(View.INVISIBLE);
//                        inboxListAdapter.remove(itemToRemove);
                    }

                    itemsToRemove.clear();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (NotificareInboxItem itemToRemove : itemsToRemove) {
                int position = inboxListAdapter.getPosition(itemToRemove);
                listView.getChildAt(position).findViewById(R.id.inbox_delete).setVisibility(View.INVISIBLE);
            }
            itemsToRemove.clear();
            mActionMode = null;
        }
    };
}
