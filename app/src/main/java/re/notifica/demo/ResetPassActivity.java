package re.notifica.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;

public class ResetPassActivity extends AppCompatActivity {

    private String token;
    private EditText passwordField;
    private EditText confirmPasswordField;
    private ProgressDialog dialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        builder = new AlertDialog.Builder(this);

        Typeface lightFont = Typeface.createFromAsset(this.getAssets(), "fonts/Lato-Light.ttf");
        Typeface regularFont = Typeface.createFromAsset(this.getAssets(), "fonts/Lato-Regular.ttf");

        Button resetButton = findViewById(R.id.buttonResetPass);
        passwordField = findViewById(R.id.pass);
        confirmPasswordField = findViewById(R.id.confirmPass);
        passwordField.setTypeface(lightFont);
        confirmPasswordField.setTypeface(lightFont);
        resetButton.setTypeface(lightFont);


        Uri data = getIntent().getData();
        if (data != null) {
            List<String> pathSegments = data.getPathSegments();
            if (pathSegments.size() >= 4 && pathSegments.get(0).equals("oauth") && pathSegments.get(2).equals(Notificare.shared().getApplicationInfo().getId()) && pathSegments.get(1).equals("resetpassword")) {
                token = pathSegments.get(3);
            } else {
                token = null;
            }
        } else {
            token = getIntent().getStringExtra(Notificare.INTENT_EXTRA_TOKEN);
        }

        if (token == null) {
            Toast.makeText(this, getString(R.string.error_reset_pass_token), Toast.LENGTH_LONG).show();
            finish();
        }

        findViewById(R.id.buttonResetPass).setOnClickListener(view -> resetPassword());
    }

    public void resetPassword() {

        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword)) {

            builder.setMessage(R.string.error_reset_pass)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        //do things
                    });
            builder.create();
            builder.show();

        } else if (!password.equals(confirmPassword)) {

            builder.setMessage(R.string.error_pass_not_match)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        //do things
                        confirmPasswordField.setText("");
                    });
            builder.create();
            builder.show();

        } else if (password.length() < 5) {

            builder.setMessage(R.string.error_pass_too_short)
                    .setTitle(R.string.app_name)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, id) -> {
                        //do things
                        confirmPasswordField.setText("");
                        passwordField.setText("");
                    });
            builder.create();
            builder.show();

        }else {

            dialog = ProgressDialog.show(ResetPassActivity.this, "", getString(R.string.loader), true);

            Notificare.shared().resetPassword(password, token, new NotificareCallback<Boolean>(){

                @Override
                public void onError(NotificareError arg0) {

                    dialog.dismiss();
                    builder.setMessage(arg0.getMessage())
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> {
                                confirmPasswordField.setText("");
                                passwordField.setText("");
                            });
                    builder.create();
                    builder.show();
                }

                @Override
                public void onSuccess(Boolean arg0) {

                    dialog.dismiss();
                    builder.setMessage(R.string.success_reset_pass)
                            .setTitle(R.string.app_name)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> finish());
                    builder.create();
                    builder.show();
                }

            });
        }
    }
}
