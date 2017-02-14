package re.notifica.hybrid;


import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class BeaconsFragment extends Fragment implements BeaconRangingListener {
    public BeaconListAdapter beaconListAdapter;

    public BeaconsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_beacons);
        View rootView = inflater.inflate(R.layout.fragment_beacons, container, false);

        final ListView listView =  (ListView) rootView.findViewById(R.id.beaconsList);
        beaconListAdapter = new BeaconListAdapter(getActivity(), R.layout.beacon_list_cell);
        listView.setAdapter(beaconListAdapter);

        TextView emptyText = (TextView)rootView.findViewById(R.id.empty_message);
        listView.setEmptyView(emptyText);

        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().addRangingListener(this);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().addRangingListener(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (Notificare.shared().getBeaconClient() != null) {
            Notificare.shared().getBeaconClient().removeRangingListener(this);
        }
    }

    @Override
    public void onRangingBeacons(final List<NotificareBeacon> list) {
        Log.d("TAGGGGG", "HERERERE");
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                beaconListAdapter.clear();

                Log.d("TAGGGGG", "HERERERE");
                for (NotificareBeacon beacon : list) {
                    beaconListAdapter.add(beacon);
                }
            }

        });

    }

    public class BeaconListAdapter extends ArrayAdapter<NotificareBeacon> {

        private Activity context;
        private int resource;


        public BeaconListAdapter(Activity context, int resource) {
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

            Typeface hairlineTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Hairline.ttf");
            Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Regular.ttf");
            Typeface lightTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Light.ttf");


            TextView nameView = (TextView)rowView.findViewById(R.id.name);
            nameView.setTypeface(myTypeface);
            TextView messageView = (TextView)rowView.findViewById(R.id.message);
            messageView.setTypeface(lightTypeface);
            ImageView iconView = (ImageView)rowView.findViewById(R.id.icon);
            NotificareBeacon beacon = getItem(position);
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
            return rowView;
        }

    }
}
