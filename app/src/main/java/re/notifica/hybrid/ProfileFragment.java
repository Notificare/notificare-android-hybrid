package re.notifica.hybrid;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareUser;
import re.notifica.model.NotificareUserPreference;
import re.notifica.model.NotificareUserPreferenceOption;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    public ListView userProfileList;
    public ProgressBar spinner;
    private int SEGMENTS_START = 5;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setShowHideAnimationEnabled(false);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_profile);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        spinner = (ProgressBar) rootView.findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        userProfileList =  (ListView) rootView.findViewById(R.id.userProfileList);
        refreshView();

        return rootView;
    }

    public void refreshView(){

        spinner.setVisibility(View.VISIBLE);

        if(Notificare.shared().isLoggedIn()){
            Notificare.shared().fetchUserDetails(new NotificareCallback<NotificareUser>() {

                @Override
                public void onSuccess(final NotificareUser userResult) {

                    final NotificareUser result = userResult;
                    Notificare.shared().fetchUserPreferences(new NotificareCallback<List<NotificareUserPreference>>() {
                        @Override
                        public void onSuccess(List<NotificareUserPreference> notificareUserPreferences) {

                            if(result.getAccessToken() == null){
                                Notificare.shared().generateAccessToken(new NotificareCallback<NotificareUser>() {
                                    @Override
                                    public void onSuccess(NotificareUser notificareUser) {

                                    }

                                    @Override
                                    public void onError(NotificareError notificareError) {

                                    }
                                });
                            }


                            final ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                            final ArrayList<NotificareUserPreference> prefs = new ArrayList<NotificareUserPreference>();


                            HashMap<String, String> avatar = new HashMap<String, String>();
                            avatar.put("label", getString(R.string.label_user_profile));
                            avatar.put("value", result.getUserId());
                            list.add(avatar);

                            HashMap<String, String> name = new HashMap<String, String>();
                            name.put("label", getString(R.string.label_name));
                            name.put("value", userResult.getUserName());
                            list.add(name);

                            HashMap<String, String> email = new HashMap<String, String>();
                            email.put("label", getString(R.string.label_email));
                            email.put("value", userResult.getUserId());
                            list.add(email);

                            HashMap<String, String> accessToken = new HashMap<String, String>();
                            accessToken.put("label", getString(R.string.label_push_email));
                            accessToken.put("value", userResult.getAccessToken());
                            list.add(accessToken);

                            HashMap<String, String> memberCard = new HashMap<String, String>();
                            memberCard.put("label", getString(R.string.label_member_card));
                            list.add(memberCard);

                            final HashMap<String, String> changePass = new HashMap<String, String>();
                            changePass.put("label", getString(R.string.label_change_pass));
                            list.add(changePass);

                            HashMap<String, String> generateToken = new HashMap<String, String>();
                            generateToken.put("label", getString(R.string.label_generate_token));
                            list.add(generateToken);

                            HashMap<String, String> signOut = new HashMap<String, String>();
                            signOut.put("label", getString(R.string.label_signout));
                            list.add(signOut);

                            HashMap<String, String> header2 = new HashMap<String, String>();
                            header2.put("label", getString(R.string.header_user_preferences));
                            list.add(header2);

                            SEGMENTS_START = list.size();

                            for (NotificareUserPreference preferenceObj : notificareUserPreferences) {
                                prefs.add(preferenceObj);
                                HashMap<String, String> pref = new HashMap<String, String>();
                                pref.put("label", preferenceObj.getLabel());
                                pref.put("preferenceId", preferenceObj.getId());

                                if(preferenceObj.getPreferenceType().equals("choice")){
                                    for (NotificareUserPreferenceOption segmentObj : preferenceObj.getPreferenceOptions()) {
                                        if(segmentObj.isSelected()){
                                            pref.put("name", segmentObj.getLabel());
                                        }
                                    }
                                }

                                if(preferenceObj.getPreferenceType().equals("single")){
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

                            userProfileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> aView, View view, int position,
                                                        long arg) {

                                    if(list.get(position).get("label").equals(getString(R.string.label_member_card))){
                                        Notificare.shared().getPassbookManager().open(AppBaseApplication.getMemberCardSerial());
                                    }

                                    if(list.get(position).get("label").equals(getString(R.string.label_change_pass))){
                                        doChangePassword();
                                    }

                                    if(list.get(position).get("label").equals(getString(R.string.label_generate_token))){
                                        doGenerateToken();
                                    }

                                    if(list.get(position).get("label").equals(getString(R.string.label_push_email))){
                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{list.get(position).get("value").concat("@pushmail.notifica.re")});
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "Android Demo App");
                                        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.your_message));
                                        intent.setData(Uri.parse("mailto:"));
                                        //intent.setType("text/plain");
                                        startActivity(intent);
                                    }


                                    if(list.get(position).get("label").equals(getString(R.string.label_signout))){
                                        doSignOut();
                                    }

                                    if(list.get(position).get("preferenceId") != null){

                                        NotificareUserPreference preference = prefs.get(position - SEGMENTS_START);

                                        if(preference.getPreferenceType().equals("choice")){
                                            showSingleChoiceOptions(prefs.get(position - SEGMENTS_START));
                                        } else {
                                            showMultiChoiceOptions(prefs.get(position - SEGMENTS_START));
                                        }


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

        AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());

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

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (!pass1.getText().toString().equals(pass2.getText().toString())) {
                    onChangePasswordError(getString(R.string.error_pass_not_match));
                } else if (pass1.getText().toString() == null && pass2.getText().toString() == null) {
                    onChangePasswordError(getString(R.string.error_reset_pass));
                } else if (pass1.getText().toString().length() < 5) {
                    onChangePasswordError(getString(R.string.error_pass_too_short));
                } else {
                    doChangePassword(pass1);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
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
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                alert.show();

            }

            @Override
            public void onError(NotificareError notificareError) {
                dialog.dismiss();
                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.error_change_password));
                alert.show();

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
            }
        });
    }

    public void onChangePasswordError(String error){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
        alert.setTitle(getString(R.string.app_name));
        alert.setMessage(error);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
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
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        refreshView();

                    }
                });
                alert.show();
            }

            @Override
            public void onError(NotificareError notificareError) {
                dialog.dismiss();
                alert.setTitle(getString(R.string.app_name));
                alert.setMessage(getString(R.string.error_generate_token));
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                alert.show();
            }
        });
    }

    public void showSingleChoiceOptions(final NotificareUserPreference preference){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
        alert.setTitle(getString(R.string.app_name));

        ArrayList<String> tmpList =  new ArrayList<String>();
        int selectedPosition = 0;
        for(int i = 0; i < preference.getPreferenceOptions().size(); i++){
            if(preference.getPreferenceOptions().get(i).isSelected()){
                selectedPosition = i;
            }
            tmpList.add(preference.getPreferenceOptions().get(i).getLabel());
        }

        CharSequence[] charSeqOfNames = tmpList.toArray(new CharSequence[tmpList.size()]);
        boolean bl[] = new boolean[tmpList.size()];

        alert.setSingleChoiceItems(charSeqOfNames, selectedPosition, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                Notificare.shared().userSegmentAddToUserPreference(preference.getPreferenceOptions().get(which).getUserSegmentId(), preference, new NotificareCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {


                    }

                    @Override
                    public void onError(NotificareError notificareError) {

                    }
                });

            }
        });

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                refreshView();

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public void showMultiChoiceOptions(final NotificareUserPreference preference){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
        alert.setTitle(getString(R.string.app_name));

        ArrayList<String> tmpList =  new ArrayList<String>();
        boolean bl[] = new boolean[preference.getPreferenceOptions().size()];
        int index = 0;
        for(NotificareUserPreferenceOption option : preference.getPreferenceOptions()){
            bl[index++] = option.isSelected();
            tmpList.add(option.getLabel());
        }

        CharSequence[] charSeqOfNames = tmpList.toArray(new CharSequence[tmpList.size()]);

        alert.setMultiChoiceItems(charSeqOfNames, bl, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                if(isChecked){

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
            }
        });
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                refreshView();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
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
    public class UserProfileAdapter extends BaseAdapter {

        protected final String TAG = UserProfileAdapter.class.getSimpleName();

        private Activity activity;
        private ArrayList<HashMap<String, String>> data;
        private ArrayList<Integer> headers;
        private ArrayList<Integer> userCells;
        private ArrayList<Integer> segmentCells;
        private ArrayList<NotificareUserPreference> prefs;
        private LayoutInflater inflater = null;

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private static final int TYPE_SIMPLE = 2;
        private static final int TYPE_SINGLE = 3;
        private static final int TYPE_CHOICE = 4;
        private static final int TYPE_SELECT = 5;

        private Typeface lightFont;
        private Typeface regularFont;
        private Typeface hairlineFont;

        public UserProfileAdapter(Activity activity, ArrayList<HashMap<String, String>> userProfileList, ArrayList<NotificareUserPreference> prefs) {
            this.activity = activity;
            this.data = userProfileList;
            this.headers = new ArrayList<Integer>();
            this.userCells = new ArrayList<Integer>();
            this.segmentCells = new ArrayList<Integer>();

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

            if(headers.contains(position)){
                return TYPE_SEPARATOR;
            } else {
                if(userCells.contains(position)){
                    return TYPE_ITEM;
                } else {
                    if(segmentCells.contains(position)){

                        if(this.prefs.get(position - SEGMENTS_START).getPreferenceType().equals("single")){
                            return TYPE_SINGLE;
                        } else if (this.prefs.get(position - SEGMENTS_START).getPreferenceType().equals("choice")){
                            return TYPE_CHOICE;
                        } else if (this.prefs.get(position - SEGMENTS_START).getPreferenceType().equals("select")){
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
            // TODO Auto-generated method stub
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            HashMap<String, String> itemHash = new HashMap<String, String>();
            itemHash = data.get(position);
            ViewHolder holder = null;
            int rowType = getItemViewType(position);

            if (convertView == null) {
                holder = new ViewHolder();
                switch (rowType) {
                    case TYPE_ITEM:
                        convertView = inflater.inflate(R.layout.row_user_profile, null);
                        holder.icon = (ImageView) convertView.findViewById(R.id.item_icon);

                        String url = "http://gravatar.com/avatar/" + ((MainActivity)getActivity()).md5(itemHash.get("value").trim().toLowerCase()) + "?s=512&x=" + new Date().getTime();

                        final ViewHolder finalHolder = holder;
                        Ion.with(finalHolder.icon).load(url);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = inflater.inflate(R.layout.row_header, null);
                        holder.name = (TextView) convertView.findViewById(R.id.item_label);
                        holder.name.setText(itemHash.get("label"));
                        holder.name.setTypeface(regularFont);
                        break;
                    case TYPE_SIMPLE:
                        convertView = inflater.inflate(R.layout.row_user_profile_simple, null);
                        holder.label = (TextView) convertView.findViewById(R.id.item_label);
                        holder.label.setText(itemHash.get("label"));
                        holder.label.setTypeface(regularFont);

                        if (itemHash.get("value") != null && !itemHash.get("value").isEmpty()) {
                            holder.name = (TextView) convertView.findViewById(R.id.item_name);
                            holder.name.setText(itemHash.get("value"));
                            holder.name.setTypeface(lightFont);
                        }

                        break;

                    case TYPE_SINGLE:
                        convertView = inflater.inflate(R.layout.row_segment_single, null);
                        holder.segmentSwitch = (Switch) convertView.findViewById(R.id.item_switch);
                        holder.name = (TextView) convertView.findViewById(R.id.item_label);
                        holder.name.setText(itemHash.get("label"));

                        if( itemHash.get("selected") != null && itemHash.get("selected").equals("1")){
                            holder.segmentSwitch.setChecked(true);
                        }

                        holder.segmentSwitch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                NotificareUserPreferenceOption segment = prefs.get(position - SEGMENTS_START).getPreferenceOptions().get(0);

                                if(((Switch) v).isChecked()) {

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

                            }
                        });
                        holder.name.setTypeface(regularFont);
                        break;

                    case TYPE_CHOICE:
                        convertView = inflater.inflate(R.layout.row_segment_choice, null);
                        holder.label = (TextView) convertView.findViewById(R.id.item_label);
                        holder.name = (TextView) convertView.findViewById(R.id.item_name);
                        holder.label.setText(itemHash.get("label"));
                        holder.name.setText(itemHash.get("name"));
                        holder.label.setTypeface(regularFont);
                        holder.name.setTypeface(lightFont);
                        break;

                    case TYPE_SELECT:
                        convertView = inflater.inflate(R.layout.row_segment_select, null);
                        holder.name = (TextView) convertView.findViewById(R.id.item_label);
                        holder.name.setText(itemHash.get("label"));
                        holder.name.setTypeface(regularFont);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            return convertView;
        }

        public class ViewHolder {
            public Switch segmentSwitch;
            public ImageView icon;
            public TextView name;
            public TextView email;
            public TextView token;
            public TextView label;
        }

    }
}
