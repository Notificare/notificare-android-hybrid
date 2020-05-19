package re.notifica.demo;


import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import android.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import re.notifica.Notificare;
import re.notifica.NotificareCallback;
import re.notifica.NotificareError;
import re.notifica.model.NotificareAsset;
import re.notifica.support.NotificareSupport;
import re.notifica.util.AssetLoader;


/**
 * A simple {@link Fragment} subclass.
 */
public class StorageFragment extends Fragment {

    private static final String TAG = StorageFragment.class.getSimpleName();

    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private AssetsAdapter mAdapter;
    public TextView emptyView;
    private boolean mHasStoragePermission;

    public StorageFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_storage);
        }

        View rootView = inflater.inflate(R.layout.fragment_storage, container, false);

        GridView listView = rootView.findViewById(R.id.gridView);
        emptyView = rootView.findViewById(R.id.emptyMessage);
        emptyView.setText(getString(R.string.title_intro_assets));
        emptyView.setTypeface(NotificareSupport.shared().getTypefaceCache().get("ProximaNovaThin"));

        mAdapter = new AssetsAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setEmptyView(emptyView);

        mHasStoragePermission = isStoragePermissionGranted();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_storage, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        mSearchMenuItem = menu.findItem(R.id.search);
        Drawable searchIcon = mSearchMenuItem.getIcon().mutate();
        searchIcon.setColorFilter(new LightingColorFilter(0x000000, 0xFFFFFF));
        mSearchMenuItem.setIcon(searchIcon);

        mSearchView = (SearchView) mSearchMenuItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.hint_search_assets));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                mSearchMenuItem.collapseActionView();
                mAdapter.clear();
                Notificare.shared().fetchAssets(query, new NotificareCallback<List<NotificareAsset>>() {
                    @Override
                    public void onSuccess(List<NotificareAsset> notificareAssets) {
                        Log.i(TAG, String.format("Found %d assets", notificareAssets.size()));
                        mAdapter.addAll(notificareAssets);
                    }

                    @Override
                    public void onError(NotificareError notificareError) {
                        Log.e(TAG, "Error fetching assets", notificareError);
                        emptyView.setText(getString(R.string.title_empty_assets));
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                emptyView.setText(getString(R.string.title_intro_assets));
                return false;
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    private class AssetsAdapter extends ArrayAdapter<NotificareAsset> {

        private LayoutInflater mInflater;

        AssetsAdapter(Context context) {
            this(context, new ArrayList<>());
        }

        AssetsAdapter(Context context, List<NotificareAsset> items) {
            super(context, 0, items);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.row_asset, parent, false);
            }
            NotificareAsset notificareAsset = getItem(position);

            if (notificareAsset != null) {
                AppCompatImageView imageView = convertView.findViewById(R.id.assetImage);
                if (notificareAsset.getKey() == null) {
                    imageView.setVisibility(View.GONE);
                } else {
                    if (notificareAsset.getContentType() != null &&
                            (notificareAsset.getContentType().equals("image/jpeg") ||
                                    notificareAsset.getContentType().equals("image/gif") ||
                                    notificareAsset.getContentType().equals("image/png"))) {
                        AssetLoader.loadImage(Notificare.shared().getPushApiBaseUrl() + "/asset/file/" + notificareAsset.getKey(), imageView);
                    } else if (notificareAsset.getContentType() != null &&
                            notificareAsset.getContentType().equals("video/mp4")) {

                        imageView.setImageResource(R.drawable.video);

                    } else if (notificareAsset.getContentType() != null &&
                            notificareAsset.getContentType().equals("application/pdf")) {

                        imageView.setImageResource(R.drawable.pdf);

                    } else if (notificareAsset.getContentType() != null &&
                            notificareAsset.getContentType().equals("application/json")) {

                        imageView.setImageResource(R.drawable.json);

                    } else if (notificareAsset.getContentType() != null &&
                            notificareAsset.getContentType().equals("text/javascript")) {

                        imageView.setImageResource(R.drawable.js);

                    } else if (notificareAsset.getContentType() != null &&
                            notificareAsset.getContentType().equals("text/css")) {

                        imageView.setImageResource(R.drawable.css);

                    } else if (notificareAsset.getContentType() != null &&
                            notificareAsset.getContentType().equals("text/html")) {

                        imageView.setImageResource(R.drawable.html);

                    }
                }
            }

            //((TextView) convertView.findViewById(R.id.asset_title)).setText(notificareAsset.getTitle());
            //((TextView) convertView.findViewById(R.id.asset_description)).setText(Html.fromHtml(notificareAsset.getDescription()));

            convertView.setClickable(false);
            convertView.setTag(notificareAsset);

            if (mHasStoragePermission) {
                convertView.setOnLongClickListener(v -> {
                    NotificareAsset asset = (NotificareAsset) v.getTag();

                    if (asset.getKey() != null) {
                        String url = Notificare.shared().getPushApiBaseUrl() + "/asset/file/" + asset.getKey();

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setTitle(asset.getTitle());
                        if (Build.VERSION.SDK_INT < 24) {
                            //noinspection deprecation
                            request.setDescription(Html.fromHtml(asset.getDescription()));
                        } else {
                            request.setDescription(Html.fromHtml(asset.getDescription(), Html.FROM_HTML_MODE_LEGACY));
                        }
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, asset.getOriginalFileName());

                        DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }

                    return true;
                });
            } else {
                convertView.setLongClickable(false);
            }


            return convertView;
        }
    }

}