package re.notifica.demo;


import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareTimeOfDay;
import re.notifica.model.NotificareTimeOfDayRange;
import re.notifica.support.recyclerview.decorators.ConditionalDividerItemDecoration;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {


    private SettingsAdapter mAdapter;
    private Setting mSettingDnd;
    private Setting mSettingDndStart;
    private Setting mSettingDndEnd;

    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_settings);
        }

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);


        RecyclerView recyclerView = rootView.findViewById(R.id.list);

        mAdapter = new SettingsAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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

    private void configListDecorations(RecyclerView recyclerView) {
        ConditionalDividerItemDecoration conditionalDivider = new ConditionalDividerItemDecoration(getActivity(), null, false, false);
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

        if (AppBaseApplication.getNotificationsEnabled()) {
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
            config = new Config(getActivity());
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


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_SECTION) {
                View view = mLayoutInflater.inflate(R.layout.row_material_subheader_list, parent, false);
                return new SettingsFragment.SettingsAdapter.SectionViewHolder(view);
            } else if (viewType == TYPE_NOTIFICATIONS) {
                View view = mLayoutInflater.inflate(R.layout.row_material_three_lines_switch, parent, false);
                return new SettingsFragment.SettingsAdapter.NotificationsViewHolder(view);
            } else if (viewType == TYPE_LOCATION) {
                View view = mLayoutInflater.inflate(R.layout.row_material_three_lines_switch, parent, false);
                return new SettingsFragment.SettingsAdapter.NotificationsViewHolder(view);
            } else if (viewType == TYPE_FEEDBACK || viewType == TYPE_APP_VERSION) {
                View view = mLayoutInflater.inflate(R.layout.row_material_two_lines, parent, false);
                return new SettingsFragment.SettingsAdapter.SettingViewHolder(view);
            } else if (viewType == TYPE_DND) {
                View view = mLayoutInflater.inflate(R.layout.row_material_three_lines_switch, parent, false);
                return new SettingsFragment.SettingsAdapter.NotificationsViewHolder(view);
            } else if (viewType == TYPE_DND_START || viewType == TYPE_DND_END) {
                View view = mLayoutInflater.inflate(R.layout.row_material_two_lines, parent, false);
                return new SettingsFragment.SettingsAdapter.DndViewHolder(view);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            if (viewType == TYPE_SECTION) {
                SettingsFragment.Section section = (SettingsFragment.Section) mData.get(position);
                ((SettingsFragment.SettingsAdapter.SectionViewHolder) holder).name.setText(section.getName());
            } else if (viewType == TYPE_NOTIFICATIONS) {
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).label.setText(R.string.settings_general_notifications_label);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).description.setText(R.string.settings_general_notifications_description);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setChecked(AppBaseApplication.getNotificationsEnabled());
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            AppBaseApplication.setNotificationsEnabled(true);
                            add(2, mSettingDnd);
                        } else {
                            AppBaseApplication.setNotificationsEnabled(false);

                            if (AppBaseApplication.getDndEnabled()) {
                                removeRange(2, 3);
                            } else {
                                remove(2);
                            }

                            AppBaseApplication.resetDnd();
                        }
                    }
                });
            } else if (viewType == TYPE_LOCATION) {
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).label.setText(R.string.settings_general_location_label);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).description.setText(R.string.settings_general_location_description);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setChecked(AppBaseApplication.getLocationEnabled());
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            AppBaseApplication.setLocationEnabled(true);
                            Notificare.shared().enableLocationUpdates();
                        } else {
                            AppBaseApplication.setLocationEnabled(false);
                            Notificare.shared().disableLocationUpdates();
                        }
                    }
                });
            } else if (viewType == TYPE_FEEDBACK) {
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).label.setText(R.string.settings_others_feedback_label);
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).description.setText(R.string.settings_others_feedback_description);
                holder.itemView.setClickable(true);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{config.getProperty("email")});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Android Demo App");
                        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.your_message));
                        intent.setData(Uri.parse("mailto:"));
                        startActivity(intent);
                    }
                });
            } else if (viewType == TYPE_APP_VERSION) {
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).label.setText(R.string.settings_others_app_version_label);
                ((SettingsFragment.SettingsAdapter.SettingViewHolder) holder).description.setText(BuildConfig.VERSION_NAME);
                holder.itemView.setClickable(false);
            } else if (viewType == TYPE_DND) {
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).label.setText(R.string.settings_general_dnd_label);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).description.setText(R.string.settings_general_dnd_description);
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setChecked(AppBaseApplication.getDndEnabled());
                ((SettingsFragment.SettingsAdapter.NotificationsViewHolder) holder).switchEditor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            AppBaseApplication.setDndEnabled(true);
                            addAll(3, Arrays.asList(mSettingDndStart, mSettingDndEnd));
                        } else {
                            final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getString(R.string.app_name), getString(R.string.settings_general_dnd_updating_message), true, false);
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
                    }
                });
            } else if (viewType == TYPE_DND_START) {
                NotificareTimeOfDay timeOfDay = (NotificareTimeOfDay) ((SettingsFragment.Setting) mData.get(position)).getData();
                ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).label.setText(R.string.settings_general_dnd_start_label);
                ((SettingsFragment.SettingsAdapter.DndViewHolder) holder).description.setText(timeOfDay == null ? null : String.format(Locale.getDefault(), "%02d:%02d", timeOfDay.getHour(), timeOfDay.getMinute()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NotificareTimeOfDay timeOfDay = (NotificareTimeOfDay) mSettingDndStart.getData();

                        int hour = timeOfDay == null ? 0 : timeOfDay.getHour();
                        int minute = timeOfDay == null ? 0 : timeOfDay.getMinute();
                        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mSettingDndStart.setData(new NotificareTimeOfDay(hourOfDay, minute));
                                notifyItemRangeChanged(3, 2);

                                if (mSettingDndStart.getData() != null && mSettingDndEnd.getData() != null) {
                                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getString(R.string.app_name), getString(R.string.settings_general_dnd_updating_message), true, false);
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
                            }
                        };

                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), listener, hour, minute, true);
                        timePickerDialog.show();

                    }
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

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NotificareTimeOfDay timeOfDay = (NotificareTimeOfDay) mSettingDndEnd.getData();

                        int hour = timeOfDay == null ? 0 : timeOfDay.getHour();
                        int minute = timeOfDay == null ? 0 : timeOfDay.getMinute();
                        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mSettingDndEnd.setData(new NotificareTimeOfDay(hourOfDay, minute));
                                notifyItemChanged(4);

                                if (mSettingDndStart.getData() != null && mSettingDndEnd.getData() != null) {
                                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getString(R.string.app_name), getString(R.string.settings_general_dnd_updating_message), true, false);
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
                            }
                        };

                        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), listener, hour, minute, true);
                        timePickerDialog.show();

                    }
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
            }
        }

        class NotificationsViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;
            public Switch switchEditor;

            public NotificationsViewHolder(View itemView) {
                super(itemView);
                label = (TextView) itemView.findViewById(R.id.label);
                description = (TextView) itemView.findViewById(R.id.description);
                switchEditor = (Switch) itemView.findViewById(R.id.switch_editor);
            }
        }

        class TopicsViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;

            public TopicsViewHolder(View itemView) {
                super(itemView);
                label = (TextView) itemView.findViewById(R.id.label);
                description = (TextView) itemView.findViewById(R.id.description);
            }
        }

        class SettingViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;

            public SettingViewHolder(View itemView) {
                super(itemView);
                label = (TextView) itemView.findViewById(R.id.label);
                description = (TextView) itemView.findViewById(R.id.description);
            }
        }

        class DndViewHolder extends RecyclerView.ViewHolder {
            public TextView label;
            public TextView description;

            public DndViewHolder(View itemView) {
                super(itemView);
                label = (TextView) itemView.findViewById(R.id.label);
                description = (TextView) itemView.findViewById(R.id.description);
            }
        }

    }

}
