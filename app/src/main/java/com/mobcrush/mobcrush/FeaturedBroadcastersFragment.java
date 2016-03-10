package com.mobcrush.mobcrush;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;

public class FeaturedBroadcastersFragment extends Fragment {
    private static final String TAG = "BroadcastersFragment";
    private FeaturedBroadcastersAdapter mAdapter;
    private boolean mAllUsersWereLoaded;
    private boolean mFeaturedUsersWereLoaded;
    private RecyclerView mRecyclerView;
    private ViewGroup mRoot;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint({"ResourceAsColor"})
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_broadcasters, container, false);
        this.mRecyclerView = (RecyclerView) this.mRoot.findViewById(R.id.recycler_view);
        this.mAdapter = new FeaturedBroadcastersAdapter(getActivity());
        this.mRecyclerView.setAdapter(this.mAdapter);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadData();
        return this.mRoot;
    }

    public void onResume() {
        super.onResume();
    }

    private void updateLoadingState() {
        if (this.mRoot != null && isAdded() && this.mAllUsersWereLoaded && this.mFeaturedUsersWereLoaded) {
            View view = this.mRoot.findViewById(R.id.loading_layout);
            if (view != null) {
                this.mRoot.removeView(view);
            }
        }
    }

    public synchronized void loadData() {
        Network.getFeaturedSpotlightUsers(getActivity(), new Listener<User[]>() {
            public void onResponse(User[] response) {
                FeaturedBroadcastersFragment.this.mAdapter.addFeaturedUsers(response);
                FeaturedBroadcastersFragment.this.mFeaturedUsersWereLoaded = true;
                FeaturedBroadcastersFragment.this.updateLoadingState();
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                FeaturedBroadcastersFragment.this.mFeaturedUsersWereLoaded = true;
                FeaturedBroadcastersFragment.this.updateLoadingState();
            }
        });
        Network.getFeaturedTopUsers(getActivity(), new Listener<User[]>() {
            public void onResponse(User[] response) {
                FeaturedBroadcastersFragment.this.mAdapter.addUsers(response);
                FeaturedBroadcastersFragment.this.mAllUsersWereLoaded = true;
                FeaturedBroadcastersFragment.this.updateLoadingState();
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                FeaturedBroadcastersFragment.this.mAllUsersWereLoaded = true;
                FeaturedBroadcastersFragment.this.updateLoadingState();
            }
        });
    }
}
