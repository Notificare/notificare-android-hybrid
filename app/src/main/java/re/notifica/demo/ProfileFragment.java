package re.notifica.demo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareUser;
import re.notifica.model.NotificareUserPreference;
import re.notifica.model.NotificareUserPreferenceOption;
import re.notifica.util.AssetLoader;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ListView userProfileList;
    private ProgressBar spinner;
    private int SEGMENTS_START = 5;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_profile);
        }

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        spinner = rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        userProfileList = rootView.findViewById(R.id.userProfileList);
        refreshView();

        return rootView;
    }

    public void refreshView(){

        spinner.setVisibility(View.VISIBLE);

        if (Notificare.shared().isLoggedIn()) {
            Notificare.shared().fetchUserDetails(new NotificareCallback<NotificareUser>() {

                @Override
                public void onSuccess(final NotificareUser userResult) {

                    Notificare.shared().fetchUserPreferences(new NotificareCallback<List<NotificareUserPreference>>() {
                        @Override
                        public void onSuccess(List<NotificareUserPreference> notificareUserPreferences) {

                            if (userResult.getAccessToken() == null) {
                                Notificare.shared().generateAccessToken(new NotificareCallback<NotificareUser>() {
                                    @Override
                                    public void onSuccess(NotificareUser notificareUser) {

                                    }

                                    @Override
                                    public void onError(NotificareError notificareError) {

                                    }
                                });
                            }


                            final List<Map<String, String>> list = new ArrayList<>();
                            final List<NotificareUserPreference> prefs = new ArrayList<>();


                            Map<String, String> avatar = new HashMap<>();
                            avatar.put("label", getString(R.string.label_user_profile));
                            avatar.put("value", userResult.getUserId());
                            list.add(avatar);

                            Map<String, String> name = new HashMap<>();
                            name.put("label", getString(R.string.label_name));
                            name.put("value", userResult.getUserName());
                            list.add(name);

                            Map<String, String> email = new HashMap<>();
                            email.put("label", getString(R.string.label_email));
                            email.put("value", userResult.getUserId());
                            list.add(email);

                            Map<String, String> accessToken = new HashMap<>();
                            accessToken.put("label", getString(R.string.label_push_email));
                            accessToken.put("value", userResult.getAccessToken());
                            list.add(accessToken);

                            Map<String, String> memberCard = new HashMap<>();
                            memberCard.put("label", getString(R.string.label_member_card));
                            list.add(memberCard);

                            final Map<String, String> changePass = new HashMap<>();
                            changePass.put("label", getString(R.string.label_change_pass));
                            list.add(changePass);

                            Map<String, String> generateToken = new HashMap<>();
                            generateToken.put("label", getString(R.string.label_generate_token));
                            list.add(generateToken);

                            Map<String, String> signOut = new HashMap<>();
                            signOut.put("label", getString(R.string.label_signout));
                            list.add(signOut);

                            Map<String, String> header2 = new HashMap<>();
                            header2.put("label", getString(R.string.header_user_preferences));
                            list.add(header2);

                            SEGMENTS_START = list.size();

                            for (NotificareUserPreference preferenceObj : notificareUserPreferences) {
                                prefs.add(preferenceObj);
                                Map<String, String> pref = new HashMap<>();
                                pref.put("label", preferenceObj.getLabel());
                                pref.put("preferenceId", preferenceObj.getId());

                                if (preferenceObj.getPreferenceType().equals("choice")) {
                                    for (NotificareUserPreferenceOption segmentObj : preferenceObj.getPreferenceOptions()) {
                                        if(segmentObj.isSelected()){
                                            pref.put("name", segmentObj.getLabel());
                                        }
                                    }
                                }

                                if (preferenceObj.getPreferenceType().equals("single")) {
                                    for (NotificareUserPreferenceOption segmentObj : preferenceObj.getPreferenceOptions()) {
                                        pref.put("segmentId", segmentObj.getUserSegmentId());
                                        if(segmentObj.isSelected()){
                                            pref.put("selected", "1");
                                        }
                                    }
                                }
                                list.add(pref);
                            }

                            ListAdapter adapter = new UserProfileAdapter(getActivity(), list, prefs);

                            userProfileList.setAdapter(adapter);

                            userProfileList.setOnItemClickListener((aView, view, position, arg) -> {

                                if (list.get(position).get("label").equals(getString(R.string.label_member_card))) {
                                    Notificare.shared().getPassbookManager().open(AppBaseApplication.getMemberCardSerial());
                                }

                                if (list.get(position).get("label").equals(getString(R.string.label_change_pass))) {
                                    doChangePassword();
                                }

                                if (list.get(position).get("label").equals(getString(R.string.label_generate_token))) {
                                    doGenerateToken();
                                }

                                if (list.get(position).get("label").equals(getString(R.string.label_push_email))) {
                                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{list.get(position).get("value").concat("@pushmail.notifica.re")});
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Android Demo App");
                                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.your_message));
                                    intent.setData(Uri.parse("mailto:"));
                                    //intent.setType("text/plain");
                                    startActivity(intent);
                                }


                                if (list.get(position).get("label").equals(getString(R.string.label_signout))) {
                                    doSignOut();
                                }

                                if (list.get(position).get("preferenceId") != null) {

                                    NotificareUserPreference preference = prefs.get(position - SEGMENTS_START);

                                    if (preference.getPreferenceType().equals("choice")) {
                                        showSingleChoiceOptions(prefs.get(position - SEGMENTS_START));
                                    } else {
                                        showMultiChoiceOptions(prefs.get(position - SEGMENTS_START));
                                    }


                                }

                            });
                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(NotificareError notificareError) {

                            spinner.setVisibility(View.GONE);
                        }
                    });

                }

                @Override
                public void onError(NotificareError error) {
                    spinner.setVisibility(View.GONE);
                    getFragmentManager().popBackStack();
                }
            });
        }
    }

    /**
     * Change Password
     */
    public void doChangePassword(){

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        LinearLayout layout = new LinearLayout(getActivity().getBaseContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(getString(R.string.text_change_pass));

        final EditText pass1 = new EditText(this.getActivity());
        final EditText pass2 = new EditText(this.getActivity());
        pass1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(pass1);
        layout.addView(pass2);
        alert.setView(layout);

        alert.setPositiveButton("Ok", (dialog, whichButton) -> {

            if (pass1.getText().toString().isEmpty() && pass2.getText().toString().isEmpty()) {
                onChangePasswordError(getString(R.string.error_reset_pass));
            } else if (!pass1.getText().toString().equals(pass2.getText().toString())) {
                onChangePasswordError(getString(R.string.error_pass_not_match));
            } else if (pass1.getText().toString().length() < 5) {
                onChangePasswordError(getString(R.string.error_pass_too_short));
            } else {
                doChangePassword(pass1);
            }
        });

        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });

        alert.show();

    }

    public void doChangePassword(EditText pass){
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", getString(R.string.loader), true);
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());

        String password = pass.getText().toString();
        Notificare.shared().changePassword(password, new NotificareCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                dialog.dismiss();
                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.success_change_password));
                alert.setPositiveButton("Ok", (dialog1, whichButton) -> {

                });
                alert.show();

            }

            @Override
            public void onError(NotificareError notificareError) {
                dialog.dismiss();
                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.error_change_password));
                alert.show();

                alert.setPositiveButton("Ok", (dialog12, whichButton) -> {

                });
            }
        });
    }

    public void onChangePasswordError(String error){
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(error);
        alert.setPositiveButton("Ok", (dialog, whichButton) -> {

        });
        alert.show();
    }

    /**
     * Generate Token
     */
    public void doGenerateToken(){

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", getString(R.string.loader), true);
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());


        Notificare.shared().generateAccessToken(new NotificareCallback<NotificareUser>() {
            @Override
            public void onSuccess(NotificareUser notificareUser) {
                dialog.dismiss();
                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.success_generate_token));
                alert.setPositiveButton("Ok", (dialog1, whichButton) -> refreshView());
                alert.show();
            }

            @Override
            public void onError(NotificareError notificareError) {
                dialog.dismiss();
                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.error_generate_token));
                alert.setPositiveButton("Ok", (dialog12, whichButton) -> {

                });
                alert.show();
            }
        });
    }

    public void showSingleChoiceOptions(final NotificareUserPreference preference){
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.app_name));

        List<String> tmpList =  new ArrayList<>();
        int selectedPosition = 0;
        for (int i = 0; i < preference.getPreferenceOptions().size(); i++) {
            if (preference.getPreferenceOptions().get(i).isSelected()) {
                selectedPosition = i;
            }
            tmpList.add(preference.getPreferenceOptions().get(i).getLabel());
        }

        CharSequence[] charSeqOfNames = tmpList.toArray(new CharSequence[tmpList.size()]);

        alert.setSingleChoiceItems(charSeqOfNames, selectedPosition, (dialog, which) -> Notificare.shared().userSegmentAddToUserPreference(preference.getPreferenceOptions().get(which).getUserSegmentId(), preference, new NotificareCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {


            }

            @Override
            public void onError(NotificareError notificareError) {

            }
        }));

        alert.setPositiveButton("Ok", (dialog, whichButton) -> refreshView());
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });

        alert.show();
    }

    public void showMultiChoiceOptions(final NotificareUserPreference preference){
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.app_name));

        List<String> names =  new ArrayList<>(preference.getPreferenceOptions().size());
        boolean bl[] = new boolean[preference.getPreferenceOptions().size()];
        int index = 0;
        for (NotificareUserPreferenceOption option : preference.getPreferenceOptions()) {
            bl[index++] = option.isSelected();
            names.add(option.getLabel());
        }

        CharSequence[] charSeqOfNames = names.toArray(new CharSequence[names.size()]);

        alert.setMultiChoiceItems(charSeqOfNames, bl, (dialog, which, isChecked) -> {

            if (isChecked) {

                Notificare.shared().userSegmentAddToUserPreference(preference.getPreferenceOptions().get(which).getUserSegmentId(), preference, new NotificareCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        refreshView();

                    }

                    @Override
                    public void onError(NotificareError notificareError) {

                    }
                });

            } else {

                Notificare.shared().userSegmentRemoveFromUserPreference(preference.getPreferenceOptions().get(which).getUserSegmentId(), preference, new NotificareCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                        refreshView();
                    }

                    @Override
                    public void onError(NotificareError notificareError) {

                    }
                });

            }
        });
        alert.setPositiveButton("Ok", (dialog, whichButton) -> refreshView());
        alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
            // Canceled.
        });
        alert.show();
    }


    /**
     * Sign Out
     */
    public void doSignOut(){

        spinner.setVisibility(View.VISIBLE);
        Notificare.shared().userLogout(new NotificareCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {

                getFragmentManager().popBackStack();
                spinner.setVisibility(View.GONE);
            }

            @Override
            public void onError(NotificareError error) {
                spinner.setVisibility(View.GONE);
            }
        });
    }
    /**
     * User Profile Adapter
     */
    private class UserProfileAdapter extends BaseAdapter {

        protected final String TAG = UserProfileAdapter.class.getSimpleName();

        private Activity activity;
        private List<Map<String, String>> data;
        private List<Integer> headers;
        private List<Integer> userCells;
        private List<Integer> segmentCells;
        private List<NotificareUserPreference> prefs;
        private LayoutInflater inflater;

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_SIMPLE = 2;
        private static final int TYPE_SINGLE = 3;
        private static final int TYPE_CHOICE = 4;
        private static final int TYPE_SELECT = 5;

        private Typeface lightFont;
        private Typeface regularFont;
        private Typeface hairlineFont;

        UserProfileAdapter(Activity activity, List<Map<String, String>> userProfileList, List<NotificareUserPreference> prefs) {
            this.activity = activity;
            this.data = userProfileList;
            this.headers = new ArrayList<>();
            this.userCells = new ArrayList<>();
            this.segmentCells = new ArrayList<>();

            this.prefs = prefs;

            for (int i=0; i < this.prefs.size(); i++) {
                segmentCells.add(i + SEGMENTS_START);
            }


            headers.add(8);
            userCells.add(0);

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            lightFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Lato-Light.ttf");
            regularFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Lato-Regular.ttf");
            hairlineFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Lato-Hairline.ttf");
        }


        @Override
        public int getItemViewType(int position) {
            if (headers.contains(position)) {
                return TYPE_SEPARATOR;
            } else {
                if (userCells.contains(position)) {
                    return TYPE_ITEM;
                } else {
                    if (segmentCells.contains(position)) {
                        if (this.prefs.get(position - SEGMENTS_START).getPreferenceType().equals("single")) {
                            return TYPE_SINGLE;
                        } else if (this.prefs.get(position - SEGMENTS_START).getPreferenceType().equals("choice")) {
                            return TYPE_CHOICE;
                        } else if (this.prefs.get(position - SEGMENTS_START).getPreferenceType().equals("select")) {
                            return TYPE_SELECT;
                        } else {
                            return TYPE_SINGLE;
                        }
                    } else {
                        return TYPE_SIMPLE;
                    }
                }
            }
        }


        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Map<String, String> itemHash;
            itemHash = data.get(position);
            ViewHolder holder;
            int rowType = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();
                switch (rowType) {
                    case TYPE_ITEM:
                        convertView = inflater.inflate(R.layout.row_user_profile, null);
                        holder.icon = convertView.findViewById(R.id.item_icon);

                        String url = "https://gravatar.com/avatar/" + MainActivity.md5(itemHash.get("value").trim().toLowerCase()) + "?s=512&x=" + new Date().getTime();

                        AssetLoader.loadImage(url, holder.icon);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = inflater.inflate(R.layout.row_header, null);
                        holder.name = convertView.findViewById(R.id.item_label);
                        holder.name.setText(itemHash.get("label"));
                        holder.name.setTypeface(regularFont);
                        break;
                    case TYPE_SIMPLE:
                        convertView = inflater.inflate(R.layout.row_user_profile_simple, null);
                        holder.label = convertView.findViewById(R.id.item_label);
                        holder.label.setText(itemHash.get("label"));
                        holder.label.setTypeface(regularFont);

                        if (itemHash.get("value") != null && !itemHash.get("value").isEmpty()) {
                            holder.name = convertView.findViewById(R.id.item_name);
                            holder.name.setText(itemHash.get("value"));
                            holder.name.setTypeface(lightFont);
                        }

                        break;

                    case TYPE_SINGLE:
                        convertView = inflater.inflate(R.layout.row_segment_single, null);
                        holder.segmentSwitch = convertView.findViewById(R.id.item_switch);
                        holder.name = convertView.findViewById(R.id.item_label);
                        holder.name.setText(itemHash.get("label"));

                        if (itemHash.get("selected") != null && itemHash.get("selected").equals("1")) {
                            holder.segmentSwitch.setChecked(true);
                        }

                        holder.segmentSwitch.setOnClickListener(v -> {

                            NotificareUserPreferenceOption segment = prefs.get(position - SEGMENTS_START).getPreferenceOptions().get(0);

                            if (((Switch) v).isChecked()) {

                                Notificare.shared().userSegmentAddToUserPreference(segment.getUserSegmentId(), prefs.get(position - SEGMENTS_START), new NotificareCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        refreshView();
                                    }

                                    @Override
                                    public void onError(NotificareError notificareError) {

                                    }
                                });

                            } else {

                                Notificare.shared().userSegmentRemoveFromUserPreference(segment.getUserSegmentId(), prefs.get(position - SEGMENTS_START), new NotificareCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        refreshView();
                                    }

                                    @Override
                                    public void onError(NotificareError notificareError) {

                                    }
                                });
                            }

                        });
                        holder.name.setTypeface(regularFont);
                        break;

                    case TYPE_CHOICE:
                        convertView = inflater.inflate(R.layout.row_segment_choice, null);
                        holder.label = convertView.findViewById(R.id.item_label);
                        holder.name = convertView.findViewById(R.id.item_name);
                        holder.label.setText(itemHash.get("label"));
                        holder.name.setText(itemHash.get("name"));
                        holder.label.setTypeface(regularFont);
                        holder.name.setTypeface(lightFont);
                        break;

                    case TYPE_SELECT:
                        convertView = inflater.inflate(R.layout.row_segment_select, null);
                        holder.name = convertView.findViewById(R.id.item_label);
                        holder.name.setText(itemHash.get("label"));
                        holder.name.setTypeface(regularFont);
                        break;
                }
                if (convertView != null) {
                    convertView.setTag(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            return convertView;
        }

        class ViewHolder {
            public Switch segmentSwitch;
            public ImageView icon;
            public TextView name;
            public TextView email;
            public TextView token;
            public TextView label;
        }

    }
}
