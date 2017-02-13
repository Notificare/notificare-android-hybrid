package re.notifica.hybrid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import me.leolin.shortcutbadger.ShortcutBadger;
import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareInboxItem;
import re.notifica.model.NotificareNotification;
import re.notifica.util.Log;


/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment implements Notificare.OnNotificationReceivedListener {

    private static final String TAG = InboxFragment.class.getSimpleName();

    private ListView listView;
    private ArrayList<Integer> itemsToRemove;
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
        InboxFragment fragment = new InboxFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_inbox);

        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        listView = (ListView)rootView.findViewById(R.id.inboxList);

        Typeface lightFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Light.ttf");
        Typeface regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Regular.ttf");

        itemsToRemove = new ArrayList<Integer>();
        inboxListAdapter = new InboxListAdapter(getActivity(), R.layout.inbox_list_cell);
        listView.setAdapter(inboxListAdapter);

        inboxListAdapter.clear();
        if (Notificare.shared().getInboxManager() != null) {
            for (NotificareInboxItem item : Notificare.shared().getInboxManager().getItems()) {
                inboxListAdapter.add(item);
            }
        }

        TextView emptyText = (TextView)rootView.findViewById(R.id.empty_message);
        listView.setEmptyView(emptyText);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if(view.findViewById(R.id.inbox_delete).getVisibility() == View.VISIBLE){
                    //uncheck
                    view.findViewById(R.id.inbox_delete).setVisibility(View.INVISIBLE);
                    itemsToRemove.remove(new Integer(position));

                } else {
                    //check
                    itemsToRemove.add(position);
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
                Notificare.shared().getInboxManager().markItem(item);
                Notificare.shared().openNotification(getActivity(), item.getNotification());
                refreshInbox();
            }
        });

        return rootView;
    }

    public void refreshInbox(){
        if (inboxListAdapter != null) {
            inboxListAdapter.clear();
            if (Notificare.shared().getInboxManager() != null) {
                for (NotificareInboxItem item : Notificare.shared().getInboxManager().getItems()) {
                    inboxListAdapter.add(item);
                }
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
                inboxListAdapter.clear();
                if (Notificare.shared().getInboxManager() != null) {
                    Notificare.shared().getInboxManager().clearInbox();
                }
                Notificare.shared().clearInbox(new NotificareCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Log.d(TAG, "Inbox cleared");
                    }

                    @Override
                    public void onError(NotificareError notificareError) {
                        Log.e(TAG, "Failed to clear inbox: " + notificareError.getMessage());
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Notificare.shared().addNotificationReceivedListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Notificare.shared().removeNotificationReceivedListener(this);
    }

    @Override
    public void onNotificationReceived(NotificareNotification notificareNotification) {
        inboxListAdapter.clear();
        if (Notificare.shared().getInboxManager() != null) {
            for (NotificareInboxItem item : Notificare.shared().getInboxManager().getItems()) {
                inboxListAdapter.add(item);
            }
        }
    }


    /**
     * List adapter to show a row per beacon
     */
    public class InboxListAdapter extends ArrayAdapter<NotificareInboxItem> {

        private Activity context;
        private int resource;

        public InboxListAdapter(Activity context, int resource) {
            super(context, resource);
            this.context = context;
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = convertView;
            if (rowView == null) {
                rowView = inflater.inflate(resource, null, true);
            }
            TextView dateView = (TextView)rowView.findViewById(R.id.inbox_date);
            TextView messageView = (TextView)rowView.findViewById(R.id.inbox_message);
            dateView.setTypeface(regularFont);
            messageView.setTypeface(lightFont);
            NotificareInboxItem item = getItem(position);

            dateView.setText(DateUtils.getRelativeTimeSpanString(item.getTimestamp().getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS));
            messageView.setText(item.getNotification().getMessage());
            dateView.setTextColor(Color.BLACK);
            messageView.setTextColor(Color.BLACK);
            if(item.getStatus()){
                dateView.setTextColor(Color.GRAY);
                messageView.setTextColor(Color.GRAY);
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

                    for(Integer position : itemsToRemove){
                        NotificareInboxItem msg = inboxListAdapter.getItem(position);
                        Notificare.shared().getInboxManager().removeItem(msg);
                        Notificare.shared().deleteInboxItem(msg.getItemId(), new NotificareCallback<Boolean>() {
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
                        inboxListAdapter.remove(msg);
                    }

                    itemsToRemove.removeAll(itemsToRemove);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for(Integer position : itemsToRemove){
                listView.getChildAt(position).findViewById(R.id.inbox_delete).setVisibility(View.INVISIBLE);
            }
            itemsToRemove.removeAll(itemsToRemove);
            mActionMode = null;
        }
    };
}
