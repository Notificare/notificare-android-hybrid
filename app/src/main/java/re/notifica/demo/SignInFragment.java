package re.notifica.demo;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareUser;
import re.notifica.support.NotificareSupport;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {

    public SignInFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_signin);
        }

        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        Button lostPassButton = rootView.findViewById(R.id.buttonLostPass);
        Button signUpButton = rootView.findViewById(R.id.buttonSignup);
        Button signInButton = rootView.findViewById(R.id.buttonSignin);
        final EditText emailField = rootView.findViewById(R.id.email);
        final EditText passwordField = rootView.findViewById(R.id.pass);

        emailField.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
        passwordField.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaRegular"));
        signInButton.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
        signUpButton.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaBold"));
        lostPassButton.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaThin"));

        if (Notificare.shared().isLoggedIn()) {
            getFragmentManager().popBackStack();
        }

        rootView.findViewById(R.id.buttonSignup).setOnClickListener(view -> ((MainActivity)getActivity()).manageFragments("/signup"));

        rootView.findViewById(R.id.buttonLostPass).setOnClickListener(view -> ((MainActivity)getActivity()).manageFragments("/lostpass"));

        rootView.findViewById(R.id.buttonSignin).setOnClickListener(view -> {
            //signIn();

            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

                builder.setMessage(R.string.error_sign_in)
                        .setTitle(R.string.app_name)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                            //do things
                        });
                AlertDialog dialogInfo = builder.create();
                dialogInfo.show();

            } else if (password.length() < 5) {

                builder.setMessage(R.string.error_pass_too_short)
                        .setTitle(R.string.app_name)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                            //do things
                        });
                AlertDialog dialogInfo = builder.create();
                dialogInfo.show();

            } else if (!email.contains("@")) {
                builder.setMessage(R.string.error_invalid_email)
                        .setTitle(R.string.app_name)
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                            //do things
                        });
                AlertDialog dialogInfo = builder.create();
                dialogInfo.show();

            } else {

                final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", getString(R.string.loader), true);


                Notificare.shared().userLogin(email, password, new NotificareCallback<Boolean>() {

                    @Override
                    public void onError(NotificareError error) {
                        dialog.dismiss();
                        if (getActivity() != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.error_sign_in)
                                    .setTitle(R.string.app_name)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog1, id) -> {
                                        //do things
                                    });
                            AlertDialog dialogInfo = builder.create();
                            dialogInfo.show();
                        }
                        passwordField.setText(null);
                    }

                    @Override
                    public void onSuccess(Boolean result) {

                        Notificare.shared().fetchUserDetails(new NotificareCallback<NotificareUser>() {

                            @Override
                            public void onError(NotificareError error) {
                                dialog.dismiss();
                                if (getActivity() != null) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage(R.string.error_sign_in)
                                            .setTitle(R.string.app_name)
                                            .setCancelable(false)
                                            .setPositiveButton("OK", (dialog12, id) -> {
                                                //do things
                                            });
                                    AlertDialog dialogInfo = builder.create();
                                    dialogInfo.show();
                                }
                                passwordField.setText(null);
                            }

                            @Override
                            public void onSuccess(NotificareUser user) {

                                emailField.setText(null);
                                passwordField.setText(null);


                                if (user != null && (user.getAccessToken() == null || user.getAccessToken().isEmpty())) {

                                    Notificare.shared().generateAccessToken(new NotificareCallback<NotificareUser>() {
                                        @Override
                                        public void onSuccess(NotificareUser notificareUser) {
                                            finishSignIn(notificareUser);
                                            dialog.dismiss();
                                        }

                                        @Override
                                        public void onError(NotificareError notificareError) {
                                            if (getActivity() != null) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                builder.setMessage(R.string.error_sign_in)
                                                        .setTitle(R.string.app_name)
                                                        .setCancelable(false)
                                                        .setPositiveButton("OK", (dialog13, id) -> {
                                                            //do things
                                                        });
                                                AlertDialog dialogInfo = builder.create();
                                                dialogInfo.show();
                                            }
                                        }
                                    });

                                } else {
                                    finishSignIn(user);
                                    dialog.dismiss();
                                }

                            }

                        });

                    }

                });
            }
        });

        return rootView;
    }

    public void finishSignIn(NotificareUser user){
        ((MainActivity)getActivity()).manageFragments("/profile");

        if (AppBaseApplication.getMemberCardSerial().isEmpty()) {
            ((MainActivity)getActivity()).createMemberCard(user.getUserName(), user.getUserId());
        }
    }
}
