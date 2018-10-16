package re.notifica.demo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareInboxItem;
import re.notifica.util.Log;


/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment implements InboxListAdapter.InboxItemClickListener, InboxListAdapter.InboxItemSelectionListener {

    private static final String TAG = InboxFragment.class.getSimpleName();

    private RecyclerView listView;
    private Set<NotificareInboxItem> itemsToRemove;
    private InboxListAdapter inboxAdapter;
    protected ArrayAdapter<NotificareInboxItem> inboxListAdapter;
    private ActionMode mActionMode;
    private Menu mOptionsMenu;
    private ProgressBar spinner;
    private Typeface lightFont;
    private Typeface regularFont;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_inbox);
        }
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        listView = rootView.findViewById(R.id.inboxList);

        spinner = rootView.findViewById(R.id.progressBar);

        lightFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Light.ttf");
        regularFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Lato-Regular.ttf");

        itemsToRemove = new HashSet<>();
//        inboxListAdapter = new InboxListAdapter(getActivity(), R.layout.inbox_list_cell);

        inboxAdapter = new InboxListAdapter(this, this);
        listView.setAdapter(inboxAdapter);

        TextView emptyText = rootView.findViewById(R.id.empty_message);

        if (Notificare.shared().getInboxManager() != null) {
            Notificare.shared().getInboxManager().getObservableItems().observe(this, notificareInboxItems -> {
                if (notificareInboxItems != null) {
                    if (notificareInboxItems.size() == 0) {
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                    }
                    inboxAdapter.submitList(new ArrayList<>(notificareInboxItems));
                }
            });
        }

//        listView.setEmptyView(emptyText);
//        listView.setOnItemLongClickListener((parent, view, position, id) -> {
//
//            if (view.findViewById(R.id.inbox_delete).getVisibility() == View.VISIBLE) {
//                //uncheck
//                view.findViewById(R.id.inbox_delete).setVisibility(View.INVISIBLE);
//                itemsToRemove.remove(inboxListAdapter.getItem(position));
//
//            } else {
//                //check
//                itemsToRemove.add(inboxListAdapter.getItem(position));
//                view.findViewById(R.id.inbox_delete).setVisibility(View.VISIBLE);
//            }
//
//            if (mActionMode != null) {
//                return true;
//            }
//
//            // Start the CAB using the ActionMode.Callback defined above
//            mActionMode = getActivity().startActionMode(mActionModeCallback);
//
//            view.setSelected(true);
//            return true;
//        });
//
//
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            NotificareInboxItem item = inboxListAdapter.getItem(position);
//            if (item != null) {
//                Notificare.shared().openInboxItem(getActivity(), item);
//            }
//        });

        return rootView;
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
                    spinner.setVisibility(View.VISIBLE);
                    Notificare.shared().getInboxManager().clearInbox(new NotificareCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer integer) {
                            Log.i(TAG, "inbox cleared");
                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(NotificareError notificareError) {
                            Log.e(TAG, "Failed to clear inbox: " + notificareError.getMessage());
                            spinner.setVisibility(View.GONE);
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemCLick(NotificareInboxItem item, int position) {
        if (item != null) {
            Notificare.shared().openInboxItem(getActivity(), item);
        }
    }

    @Override
    public void onItemSelected(NotificareInboxItem item, boolean selected) {
        if (item != null) {
            if (selected) {
                itemsToRemove.add(item);
            } else {
                itemsToRemove.remove(item);
            }
        }
        if (mActionMode == null) {
            mActionMode = getActivity().startActionMode(mActionModeCallback);
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

                    spinner.setVisibility(View.VISIBLE);
                    for (NotificareInboxItem itemToRemove : itemsToRemove) {
                        Notificare.shared().getInboxManager().removeItem(itemToRemove, new NotificareCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                //Log.d(TAG, "Removed inboxItem");
                                spinner.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(NotificareError notificareError) {
                                //Log.e(TAG, "Failed to remove inboxItem: " + notificareError.getMessage());
                                spinner.setVisibility(View.GONE);
                            }
                        });
                    }

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            inboxAdapter.clearSelection();
            itemsToRemove.clear();
            mActionMode = null;
        }
    };

}
