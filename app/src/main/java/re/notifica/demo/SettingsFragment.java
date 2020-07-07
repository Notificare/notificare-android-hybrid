package re.notifica.demo;


import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.billing.BillingManager;
import re.notifica.billing.NotificareBillingResult;
import re.notifica.billing.NotificarePurchase;
import re.notifica.model.NotificareProduct;
import re.notifica.model.NotificareTimeOfDay;
import re.notifica.model.NotificareTimeOfDayRange;
import re.notifica.support.NotificareSupport;
import re.notifica.support.recyclerview.decorators.ConditionalDividerItemDecoration;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2;

    private SettingsAdapter mAdapter;
    private Setting mSettingDnd;
    private Setting mSettingDndStart;
    private Setting mSettingDndEnd;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_settings);
        }

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);


        RecyclerView recyclerView = rootView.findViewById(R.id.list);

        mAdapter = new SettingsAdapter(getContext());
        recyclerView.setAdapter(mAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        configListDecorations(recyclerView);



        mSettingDnd = new Setting(SettingsAdapter.TYPE_DND);
        mSettingDndStart = new Setting(SettingsAdapter.TYPE_DND_START);
        mSettingDndEnd = new Setting(SettingsAdapter.TYPE_DND_END);

        if (AppBaseApplication.getDndEnabled()) {
            Notificare.shared().fetchDoNotDisturb(new NotificareCallback<NotificareTimeOfDayRange>() {
                @Override
                public void onSuccess(NotificareTimeOfDayRange notificareTimeOfDayRange) {
                    if (notificareTimeOfDayRange != null) {
                        mSettingDndStart.setData(notificareTimeOfDayRange.getStart());
                        mSettingDndEnd.setData(notificareTimeOfDayRange.getEnd());
                    }
                    loadData();
                }

                @Override
                public void onError(NotificareError notificareError) {
                    // todo show proper error
                    //Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadData();
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    public void requestLocationPermission() {
        if (Notificare.shared().shouldShowForegroundRequestPermissionRationale(this)) {
            // Here we should show a dialog explaining location updates
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.alert_location_permission_rationale)
                    .setCancelable(true)
                    .setNegativeButton(R.string.button_location_permission_rationale_cancel, (dialog, id) -> {
                        Log.i(TAG, "foreground location not agreed");
                    })
                    .setPositiveButton(R.string.button_location_permission_rationale_ok, (dialog, id) -> Notificare.shared().requestForegroundLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE))
                    .show();
        } else {
            Notificare.shared().requestForegroundLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestBackgroundLocationPermission() {
        if (Notificare.shared().shouldShowBackgroundRequestPermissionRationale(this)) {
            // Here we should show a dialog explaining location updates
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.alert_background_location_permission_rationale)
                    .setCancelable(true)
                    .setNegativeButton(R.string.button_location_permission_rationale_cancel, (dialog, id) -> {
                        Log.i(TAG, "background location not agreed");
                    });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setPositiveButton(getContext().getPackageManager().getBackgroundPermissionOptionLabel(), (dialog, id) -> Notificare.shared().requestBackgroundLocationPermission(this, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE));
            } else {
                builder.setPositiveButton(R.string.button_location_permission_rationale_ok, (dialog, id) -> Notificare.shared().requestBackgroundLocationPermission(this, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE));
            }
            builder.show();
        } else {
            Notificare.shared().requestBackgroundLocationPermission(this, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (Notificare.shared().checkRequestForegroundLocationPermissionResult(permissions, grantResults)) {
                    Log.i(TAG, "foreground locations permission granted");
                    Notificare.shared().enableLocationUpdates();
                    if (BuildConfig.ENABLE_BEACONS) {
                        Notificare.shared().enableBeacons(30000);
                    }
                    requestBackgroundLocationPermission();
                }
                break;
            case BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE:
                if (Notificare.shared().checkRequestBackgroundLocationPermissionResult(permissions, grantResults)) {
                    android.util.Log.i(TAG, "background location permission granted");
                    Notificare.shared().enableLocationUpdates();
                    if (BuildConfig.ENABLE_BEACONS) {
                        Notificare.shared().enableBeacons(30000);
                    }
                }
                break;
        }
    }

    private void configListDecorations(RecyclerView recyclerView) {
        ConditionalDividerItemDecoration conditionalDivider = new ConditionalDividerItemDecoration(getContext(), null, false, false);
        conditionalDivider.addViewTypeConfiguration(new ConditionalDividerItemDecoration.ViewTypeConfiguration(SettingsAdapter.SectionViewHolder.class));
        conditionalDivider.addViewTypeConfiguration(new ConditionalDividerItemDecoration.ViewTypeConfiguration(SettingsAdapter.NotificationsViewHolder.class, true, 0, 0));
        conditionalDivider.addViewTypeConfiguration(new ConditionalDividerItemDecoration.ViewTypeConfiguration(SettingsAdapter.DndViewHolder.class, false, 0, 0));
        conditionalDivider.addViewTypeConfiguration(new ConditionalDividerItemDecoration.ViewTypeConfiguration(SettingsAdapter.SettingViewHolder.class, false, 0, 0));

        recyclerView.addItemDecoration(conditionalDivider);
    }

    private void loadData() {
        List<Object> items = new ArrayList<>();
        items.add(new Section(getString(R.string.settings_section_title_general)));
        items.add(new Setting(SettingsAdapter.TYPE_NOTIFICATIONS));

        if (Notificare.shared().isNotificationsEnabled()) {
            items.add(mSettingDnd);

            if (AppBaseApplication.getDndEnabled()) {
                items.add(mSettingDndStart);
                items.add(mSettingDndEnd);
            }
        }

        items.add(new Setting(SettingsAdapter.TYPE_LOCATION));

        items.add(new Section(getString(R.string.settings_section_title_others)));
        items.add(new Setting(SettingsAdapter.TYPE_FEEDBACK));
        items.add(new Setting(SettingsAdapter.TYPE_APP_VERSION));

        mAdapter.addAll(items);
    }


    private class Setting {
        private int type;
        private Object data;


        Setting(int type) {
            this(type, null);
        }

        Setting(int type, Object data) {
            this.type = type;
            this.data = data;
        }


        public int getType() {
            return type;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    private class Section {
        private String name;


        Section(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }
    }

    private class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_SECTION = 1;
        static final int TYPE_NOTIFICATIONS = 2;
        static final int TYPE_LOCATION = 3;
        static final int TYPE_FEEDBACK = 4;
        static final int TYPE_APP_VERSION = 5;
        static final int TYPE_DND = 6;
        static final int TYPE_DND_START = 7;
        static final int TYPE_DND_END = 8;
        Config config;

        private LayoutInflater mLayoutInflater;
        private List<Object> mData;


        SettingsAdapter(Context context) {
            this(context, null);
        }

        SettingsAdapter(Context context, List<Object> items) {
            mLayoutInflater = LayoutInflater.from(context);
            mData = items == null ? new ArrayList<>() : items;
            config = new Config(getContext());
        }


        private void add(int position, Object item) {
            mData.add(position, item);
            notifyItemInserted(position);
        }

        private void addAll(Collection<?> collection) {
            int itemCount = mData.size();
            mData.addAll(collection);
            notifyItemRangeInserted(itemCount, collection.size());
        }

        private void addAll(int position, Collection<?> collection) {
            mData.addAll(position, collection);
            notifyItemRangeInserted(position, collection.size());
        }

        private void remove(int position) {
            mData.remove(position);
            notifyItemRemoved(position);
        }

        private void removeRange(int positionStart, int itemCount) {
            for (int i = positionStart; i < positionStart + itemCount; i++) {
                mData.remove(positionStart);
            }
            notifyItemRangeRemoved(positionStart, itemCount);
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SECTION) {
                View view = mLayoutInflater.inflate(R.layout.row_material_subheader_list, parent, false);
                return new SectionViewHolder(view);
            } else if (viewType == TYPE_NOTIFICATIONS) {
                View view = mLayoutInflater.inflate(R.layout.row_material_three_lines_switch, parent, false);
                return new NotificationsViewHolder(view);
            } else if (viewType == TYPE_LOCATION) {
                View view = mLayoutInflater.inflate(R.layout.row_material_three_lines_switch, parent, false);
                return new NotificationsViewHolder(view);
            } else if (viewType == TYPE_FEEDBACK || viewType == TYPE_APP_VERSION) {
                View view = mLayoutInflater.inflate(R.layout.row_material_two_lines, parent, false);
                return new SettingViewHolder(view);
            } else if (viewType == TYPE_DND) {
                View view = mLayoutInflater.inflate(R.layout.row_material_three_lines_switch, parent, false);
                return new NotificationsViewHolder(view);
            } else if (viewType == TYPE_DND_START || viewType == TYPE_DND_END) {
                View view = mLayoutInflater.inflate(R.layout.row_material_two_lines, parent, false);
                return new DndViewHolder(view);
            } else {
                View view = mLayoutInflater.inflate(R.layout.row_material_two_lines, parent, false);
                return new SettingViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            if (viewType == TYPE_SECTION) {
                SettingsFragment.Section section = (SettingsFragment.Section) mData.get(position);
                ((SettingsFragment.SettingsAdapter.SectionViewHolder) holder).name.setText(section.getName());
            } else if (viewType == TYPE_NOTIFICATIONS) {
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).label.setText(R.string.settings_general_notifications_label);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).description.setText(R.string.settings_general_notifications_description);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setChecked(Notificare.shared().isNotificationsEnabled());
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        Notificare.shared().enableNotifications();
                        add(2, mSettingDnd);
                    } else {
                        Notificare.shared().disableNotifications();
                        if (AppBaseApplication.getDndEnabled()) {
                            removeRange(2, 3);
                        } else {
                            remove(2);
                        }
                        AppBaseApplication.resetDnd();
                    }
                });
            } else if (viewType == TYPE_LOCATION) {
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).label.setText(R.string.settings_general_location_label);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).description.setText(R.string.settings_general_location_description);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setChecked(Notificare.shared().isLocationUpdatesEnabled());
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        // Check if we have any permissions
                        if (!Notificare.shared().hasForegroundLocationPermissionGranted()) {
                            requestLocationPermission();
                        } else {
                            Notificare.shared().enableLocationUpdates();
                            if (BuildConfig.ENABLE_BEACONS) {
                                Notificare.shared().enableBeacons(30000);
                            }
                            if (!Notificare.shared().hasBackgroundLocationPermissionGranted()) {
                                requestBackgroundLocationPermission();
                            }
                        }
                    } else {
                        Notificare.shared().disableLocationUpdates();
                        if (BuildConfig.ENABLE_BEACONS) {
                            Notificare.shared().disableBeacons();
                        }
                    }
                });
            } else if (viewType == TYPE_FEEDBACK) {
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).label.setText(R.string.settings_others_feedback_label);
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).description.setText(R.string.settings_others_feedback_description);
                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{config.getProperty("email")});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Android Demo App");
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.your_message));
                    intent.setData(Uri.parse("mailto:"));
                    startActivity(intent);

//                    /*
//                     * Test unlaunch
//                     */
//                    Notificare.shared().unlaunch();

//                    /*
//                     * Test buying a consumable
//                     */
//                    if (Notificare.shared().getBillingManager() != null) {
//                        NotificareProduct testProduct = Notificare.shared().getBillingManager().getProduct("re.notifica.test.app.consumable");
//                        if (testProduct != null) {
//                            Notificare.shared().getBillingManager().launchPurchaseFlow(getActivity(), testProduct, (notificareBillingResult, notificarePurchase) -> {
//                                Log.i(TAG, "Billing Result: " + notificareBillingResult.getMessage() + " for " + notificarePurchase.getProductId());
//                            });
//                        }
//                    }
                });
            } else if (viewType == TYPE_APP_VERSION) {
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).label.setText(R.string.settings_others_app_version_label);
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).description.setText(BuildConfig.VERSION_NAME);
                holder.itemView.setClickable(false);
            } else if (viewType == TYPE_DND) {
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).label.setText(R.string.settings_general_dnd_label);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).description.setText(R.string.settings_general_dnd_description);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setChecked(AppBaseApplication.getDndEnabled());
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        AppBaseApplication.setDndEnabled(true);
                        addAll(3, Arrays.asList(mSettingDndStart, mSettingDndEnd));
                    } else {
                        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.app_name), getString(R.string.settings_general_dnd_updating_message), true, false);
                        Notificare.shared().clearDoNotDisturb(new NotificareCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                progressDialog.dismiss();
                                mSettingDndStart.setData(null); // reset cached values
                                mSettingDndEnd.setData(null); // reset cached values
                                AppBaseApplication.setDndEnabled(false);
                                removeRange(3, 2);
                            }

                            @Override
                            public void onError(NotificareError notificareError) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                });
            } else if (viewType == TYPE_DND_START) {
                NotificareTimeOfDay timeOfDay = (NotificareTimeOfDay) ((SettingsFragment.Setting) mData.get(position)).getData();
                ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).label.setText(R.string.settings_general_dnd_start_label);
                ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).description.setText(timeOfDay == null ? null : String.format(Locale.getDefault(), "%02d:%02d", timeOfDay.getHour(), timeOfDay.getMinute()));
                holder.itemView.setOnClickListener(v -> {
                    NotificareTimeOfDay timeOfDay1 = (NotificareTimeOfDay) mSettingDndStart.getData();

                    int hour = timeOfDay1 == null ? 0 : timeOfDay1.getHour();
                    int minute = timeOfDay1 == null ? 0 : timeOfDay1.getMinute();
                    TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute1) -> {
                        mSettingDndStart.setData(new NotificareTimeOfDay(hourOfDay, minute1));
                        notifyItemRangeChanged(3, 2);

                        if (mSettingDndStart.getData() != null && mSettingDndEnd.getData() != null) {
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.app_name), getString(R.string.settings_general_dnd_updating_message), true, false);
                            Notificare.shared().updateDoNotDisturb(
                                    new NotificareTimeOfDayRange(
                                            (NotificareTimeOfDay) mSettingDndStart.getData(),
                                            (NotificareTimeOfDay) mSettingDndEnd.getData()),
                                    new NotificareCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            progressDialog.dismiss();
                                            AppBaseApplication.setDndRange(new NotificareTimeOfDayRange((NotificareTimeOfDay) mSettingDndStart.getData(), (NotificareTimeOfDay) mSettingDndEnd.getData()));
                                        }

                                        @Override
                                        public void onError(NotificareError notificareError) {
                                            progressDialog.dismiss();
                                            // todo show error
                                        }
                                    });
                        }
                    };

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), listener, hour, minute, true);
                    timePickerDialog.show();

                });
            } else if (viewType == TYPE_DND_END) {
                NotificareTimeOfDay timeOfDay = (NotificareTimeOfDay) ((SettingsFragment.Setting) mData.get(position)).getData();
                ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).label.setText(R.string.settings_general_dnd_end_label);
                if (timeOfDay != null) {
                    boolean isNextDay = TimeOfDayUtils.endsInNextDay((NotificareTimeOfDay) mSettingDndStart.getData(), timeOfDay);
                    if (!isNextDay) {
                        ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).description.setText(String.format(Locale.getDefault(), "%02d:%02d", timeOfDay.getHour(), timeOfDay.getMinute()));
                    } else {
                        ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).description.setText(String.format(Locale.getDefault(), "%02d:%02d %s", timeOfDay.getHour(), timeOfDay.getMinute(), getString(R.string.settings_general_dnd_next_day_label)));
                    }
                } else {
                    ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).description.setText(null);
                }

                holder.itemView.setOnClickListener(v -> {
                    NotificareTimeOfDay timeOfDay12 = (NotificareTimeOfDay) mSettingDndEnd.getData();

                    int hour = timeOfDay12 == null ? 0 : timeOfDay12.getHour();
                    int minute = timeOfDay12 == null ? 0 : timeOfDay12.getMinute();
                    TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute12) -> {
                        mSettingDndEnd.setData(new NotificareTimeOfDay(hourOfDay, minute12));
                        notifyItemChanged(4);

                        if (mSettingDndStart.getData() != null && mSettingDndEnd.getData() != null) {
                            final ProgressDialog progressDialog = ProgressDialog.show(getContext(), getString(R.string.app_name), getString(R.string.settings_general_dnd_updating_message), true, false);
                            Notificare.shared().updateDoNotDisturb(
                                    new NotificareTimeOfDayRange(
                                            (NotificareTimeOfDay) mSettingDndStart.getData(),
                                            (NotificareTimeOfDay) mSettingDndEnd.getData()),
                                    new NotificareCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            progressDialog.dismiss();
                                            AppBaseApplication.setDndRange(new NotificareTimeOfDayRange((NotificareTimeOfDay) mSettingDndStart.getData(), (NotificareTimeOfDay) mSettingDndEnd.getData()));
                                        }

                                        @Override
                                        public void onError(NotificareError notificareError) {
                                            progressDialog.dismiss();
                                            // todo show error
                                        }
                                    });
                        }
                    };

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), listener, hour, minute, true);
                    timePickerDialog.show();

                });
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public int getItemViewType(int position) {
            Object data = mData.get(position);
            if (data instanceof SettingsFragment.Section) {
                return TYPE_SECTION;
            } else {
                return ((SettingsFragment.Setting) data).getType();
            }
        }


        class SectionViewHolder extends RecyclerView.ViewHolder {
            public TextView name;

            SectionViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView;
                name.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
            }
        }

        class NotificationsViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;
            public Switch switchEditor;

            NotificationsViewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                description = itemView.findViewById(R.id.description);
                switchEditor = itemView.findViewById(R.id.switch_editor);

                label.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
                description.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
            }
        }

        class TopicsViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;

            TopicsViewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                description = itemView.findViewById(R.id.description);
                label.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
                description.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
            }
        }

        class SettingViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;

            SettingViewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                description = itemView.findViewById(R.id.description);

                label.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
                description.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
            }
        }

        class DndViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;

            DndViewHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.label);
                description = itemView.findViewById(R.id.description);

                label.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
                description.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
            }
        }

    }

}
