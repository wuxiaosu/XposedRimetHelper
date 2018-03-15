package com.wuxiaosu.rimethelper.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.jaeger.library.StatusBarUtil;
import com.wuxiaosu.rimethelper.R;
import com.wuxiaosu.rimethelper.bean.LocationSearchSuggestions;

import java.util.ArrayList;
import java.util.List;

public class AMapLiteActivity extends AppCompatActivity {

    public final static String LAT_KEY = "lat";
    public final static String LON_KEY = "lon";
    public final static int REQUEST_CODE = 1001;

    private MapView mMapView;
    private AMap mAMap;
    private FloatingSearchView mFloatingSearchView;
    private FloatingActionButton mFbDone;
    private Marker mChooseMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap_lite);

        StatusBarUtil.setTranslucentForImageView(this, findViewById(R.id.view_need_offset));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        initMap(savedInstanceState);
    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mapview);
        mFloatingSearchView = findViewById(R.id.floating_search_view);
        mFbDone = findViewById(R.id.fb_done);

        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        mAMap = mMapView.getMap();

        Intent intent = getIntent();
        String lat = intent.getStringExtra(LAT_KEY);
        String lon = intent.getStringExtra(LON_KEY);
        LatLng latLng = new LatLng(39.908692, 116.397477); //天安门

        mAMap.getUiSettings().setZoomControlsEnabled(false); //不显示缩放按钮

        if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
            latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
            addMarker(latLng, null);
        }
        cameraUpdate(latLng);

        mAMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarker(latLng, null);
            }
        });

        mFbDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDone();
            }
        });

        mFloatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                LocationSearchSuggestions suggestions = (LocationSearchSuggestions) searchSuggestion;
                addMarker(suggestions.getLatLng(), suggestions.getTitle());
                cameraUpdate(suggestions.getLatLng());
                mFloatingSearchView.clearSuggestions();
                mFloatingSearchView.clearSearchFocus();
            }

            @Override
            public void onSearchAction(String currentQuery) {
            }
        });

        mFloatingSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                poiSearch(mFloatingSearchView.getQuery());
            }
        });

        mFloatingSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView,
                                         ImageView leftIcon,
                                         TextView textView,
                                         SearchSuggestion item,
                                         int itemPosition) {

                LocationSearchSuggestions suggestions = (LocationSearchSuggestions) item;
                leftIcon.setImageDrawable(ContextCompat.getDrawable(AMapLiteActivity.
                        this, R.drawable.ic_current_location));
                String key = mFloatingSearchView.getQuery().split(" ")[1];
                String text = suggestions.getTitle()
                        .replaceFirst(key, "<font color=\"#E040FB\">" + key + "</font>")
                        + "<br/>" + suggestions.getBody();
                textView.setText(Html.fromHtml(text));
            }
        });
    }

    private void poiSearch(String key) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        final String[] strings = key.trim().split(" ");
        if (strings.length != 2) {
            Toast.makeText(this, "搜索格式错误", Toast.LENGTH_SHORT).show();
            return;
        }
        PoiSearch.Query query = new PoiSearch.Query(strings[1], "", strings[0]);
        query.requireSubPois(true);   //true 搜索结果包含POI父子关系; false
        query.setPageSize(20);
        query.setPageNum(0);
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (i == 1000) {
                    if (poiResult != null && poiResult.getPois() != null
                            && poiResult.getPois().size() != 0) {
                        mFloatingSearchView.swapSuggestions(searchResultData2SearchSuggestion(poiResult.getPois()));
                    } else {
                        Toast.makeText(AMapLiteActivity.this, "在“" +
                                strings[0] + "”搜索不到“" + strings[1] + "”", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AMapLiteActivity.this, "搜索错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPoiItemSearched(PoiItem poiItem, int i) {
            }
        });
        poiSearch.searchPOIAsyn();
    }

    private List<LocationSearchSuggestions> searchResultData2SearchSuggestion(List<PoiItem> data) {
        List<LocationSearchSuggestions> result = new ArrayList<>();
        for (PoiItem datum : data) {
            result.add(new LocationSearchSuggestions(datum.getTitle(),
                    datum.getCityName() + datum.getAdName() + datum.getSnippet(),
                    new LatLng(datum.getLatLonPoint().getLatitude(),
                            datum.getLatLonPoint().getLongitude())));
        }
        return result;
    }

    private void cameraUpdate(LatLng latLng) {
        CameraUpdate cameraSigma =
                CameraUpdateFactory.newCameraPosition(
                        new CameraPosition(latLng, 18, 0, 0));
        mAMap.moveCamera(cameraSigma);
    }

    /**
     * 添加标记 并移除旧的
     *
     * @param latLng
     * @return
     */
    private void addMarker(LatLng latLng, String name) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location));
        markerOptions.position(latLng);
        markerOptions.snippet(latLng.latitude + "," + latLng.longitude);
        markerOptions.title(TextUtils.isEmpty(name) ? "未知" : name);
        Marker marker = mAMap.addMarker(markerOptions);

        if (mChooseMarker != null) {
            mChooseMarker.remove();
        }
        mChooseMarker = marker;
    }

    public void chooseDone() {
        if (mChooseMarker != null) {
            Intent result = new Intent();
            result.putExtra(LAT_KEY, String.valueOf(mChooseMarker.getPosition().latitude));
            result.putExtra(LON_KEY, String.valueOf(mChooseMarker.getPosition().longitude));
            setResult(RESULT_OK, result);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    public static void actionStart(Activity context,
                                   String lat, String lon) {
        Intent intent = new Intent(context, AMapLiteActivity.class);
        intent.putExtra(AMapLiteActivity.LAT_KEY, lat);
        intent.putExtra(AMapLiteActivity.LON_KEY, lon);
        context.startActivityForResult(intent, REQUEST_CODE);
    }
}
