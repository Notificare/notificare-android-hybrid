package re.notifica.demo;


import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import re.notifica.Notificare;
import re.notifica.beacon.BeaconRangingListener;
import re.notifica.model.NotificareBeacon;
import re.notifica.support.NotificareSupport;


/**
 * A simple {@link Fragment} subclass.
 */
public class BeaconsFragment extends Fragment implements BeaconRangingListener {
    public BeaconListAdapter beaconListAdapter;

    public BeaconsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_beacons);
        }

        View rootView = inflater.inflate(R.layout.fragment_beacons, container, false);

        final ListView listView =  rootView.findViewById(R.id.beaconsList);
        beaconListAdapter = new BeaconListAdapter(getActivity(), R.layout.beacon_list_cell);
        listView.setAdapter(beaconListAdapter);

        TextView emptyText = rootView.findViewById(R.id.empty_message);
        emptyText.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaThin"));
        listView.setEmptyView(emptyText);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().addRangingListener(this);
        }
        Notificare.shared().setForeground(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().removeRangingListener(this);
        }
        Notificare.shared().setForeground(false);
    }

    @Override
    public void onRangingBeacons(final List<NotificareBeacon> list) {

        getActivity().runOnUiThread(() -> {

            beaconListAdapter.clear();
            for (NotificareBeacon beacon : list) {
                Log.d("Received Beacons", beacon.getName());
                beaconListAdapter.add(beacon);
            }
        });

    }

    private class BeaconListAdapter extends ArrayAdapter<NotificareBeacon> {

        private Activity context;
        private int resource;


         BeaconListAdapter(Activity context, int resource) {
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

            TextView nameView = rowView.findViewById(R.id.name);
            nameView.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
            TextView messageView = rowView.findViewById(R.id.message);
            messageView.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaThin"));
            ImageView iconView = rowView.findViewById(R.id.icon);
            NotificareBeacon beacon = getItem(position);
            if (beacon != null) {
                nameView.setText(beacon.getName());

                if (beacon.getNotification() != null) {
                    messageView.setText(beacon.getNotification().getMessage());
                } else {
                    messageView.setText("");
                }

                if (beacon.getCurrentProximity() == NotificareBeacon.PROXIMITY_IMMEDIATE) {
                    iconView.setImageResource(R.drawable.signal_immediate);
                } else if (beacon.getCurrentProximity() == NotificareBeacon.PROXIMITY_NEAR) {
                    iconView.setImageResource(R.drawable.signal_near);
                } else if (beacon.getCurrentProximity() == NotificareBeacon.PROXIMITY_FAR) {
                    iconView.setImageResource(R.drawable.signal_far);
                } else if (beacon.getCurrentProximity() == NotificareBeacon.PROXIMITY_UNKNOWN) {
                    iconView.setImageResource(R.drawable.signal_unkown);
                } else {
                    iconView.setImageResource(R.drawable.signal_unkown);
                }
            }
            return rowView;
        }

    }
}
