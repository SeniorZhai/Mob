package com.mobcrush.mobcrush;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.Response.Listener;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.DividerItemDecoration;

public class PlayFragment extends Fragment {
    private static final int COLUMNS_COUNT = 3;
    private GameAdapter mAdapter;
    private LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private Listener<Game[]> onResponseGames = new Listener<Game[]>() {
        public void onResponse(Game[] response) {
            if (response != null) {
                PlayFragment.this.mAdapter.addGames(response);
            }
        }
    };

    public static PlayFragment newInstance() {
        PlayFragment fragment = new PlayFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        ((TextView) view.findViewById(R.id.description)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) view.findViewById(R.id.title)).setTypeface(UIUtils.getTypeface(getActivity(), "Klavika-Light.ttf"));
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration decoration = new DividerItemDecoration(getResources().getDrawable(R.drawable.card_divider), false);
        decoration.enableHorizontalDelimiters(true, COLUMNS_COUNT);
        this.mRecyclerView.addItemDecoration(decoration);
        this.mLayoutManager = new GridLayoutManager(getActivity(), COLUMNS_COUNT);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mAdapter = new GameAdapter(getActivity(), new Game[0]);
        this.mRecyclerView.setAdapter(this.mAdapter);
        Network.getGames(getActivity(), this.onResponseGames, null);
        return view;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAMES);
        }
    }

    public void onDetach() {
        super.onDetach();
    }
}
