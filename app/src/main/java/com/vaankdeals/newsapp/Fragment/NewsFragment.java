package com.vaankdeals.newsapp.Fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.vaankdeals.newsapp.Adapter.NewsAdapter;
import com.vaankdeals.newsapp.Model.NewsModel;
import com.vaankdeals.newsapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    ViewPager2 newsViewpager;
    NewsAdapter newsAdapter;
    private static final int NUMBER_OF_ADS = 1;
    private final List<UnifiedNativeAd> mNativeAds = new ArrayList<>();
    private RequestQueue mRequestQueue;
    private List<Object> mNewsList = new ArrayList<>() ;

    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        mRequestQueue = Volley.newRequestQueue((getActivity()));

        MobileAds.initialize(getActivity(), getString(R.string.admob_app_id));
        newsViewpager = rootView.findViewById(R.id.news_swipe);
        newsAdapter = new NewsAdapter(getContext(),mNewsList);
        newsViewpager.setAdapter(newsAdapter);
        parseJson();
        loadNativeAds();
        return rootView;
    }



    private void insertAdsInMenuItems() {
        if (mNativeAds.size() <= 0) {
            return;
        }
        if (mNewsList.size() <= 0) {
            return;
        }
        int offset = (mNewsList.size() / mNativeAds.size()) + 1;
        int index = 1;
        for (UnifiedNativeAd ad : mNativeAds) {
            mNewsList.add(index, ad);
            index = index + offset;
        }

    }

    private void loadNativeAds() {
        AdLoader.Builder builder = new AdLoader.Builder(Objects.requireNonNull(getActivity()), getString(R.string.ad_unit_id_news_main));

        // A native ad loaded successfully, check if the ad loader has finished loading
        // and if so, insert the ads into the list.
        // A native ad failed to load, check if the ad loader has finished loading
        // and if so, insert the ads into the list.
        // The AdLoader used to load ads.
        AdLoader adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // A native ad loaded successfully, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        mNativeAds.add(unifiedNativeAd);
                        if (mNativeAds.size() == NUMBER_OF_ADS) {
                            NewsFragment.this.insertAdsInMenuItems();

                        }

                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Log.e("MainActivity", "The previous native ad failed to load. Attempting to"
                                + " load another.");
                        if (mNativeAds.size() == NUMBER_OF_ADS) {
                            insertAdsInMenuItems();
                        }
                    }
                }).build();
        // Load the Native ads.

        adLoader.loadAds(new AdRequest.Builder()
                .build(), NUMBER_OF_ADS);
    }

    private void parseJson() {
        String url = getString(R.string.server_website);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("server_response");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject ser = jsonArray.getJSONObject(i);
                                String news_head = ser.getString("news_head");
                                String news_desc = ser.getString("news_desc");
                                String news_image = ser.getString("news_image");
                                String news_source = ser.getString("news_source");
                                String news_day = ser.getString("news_day");
                                String news_id = ser.getString("news_id");
                                String news_link = ser.getString("news_link");
                                String news_type = ser.getString("news_type");
                                mNewsList.add(new NewsModel(news_head,news_desc,news_image,news_source,news_day,news_id,news_link,news_type));
                            }
                            // Just call notifyDataSetChanged here

                            newsAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);
    }


    }


