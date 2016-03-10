package com.mobcrush.mobcrush;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.android.volley.Response.Listener;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.DividerItemDecoration;

public class AllGamesFragment extends Fragment implements OnItemClickListener {
    private static final int COLUMNS_COUNT = 3;
    private GameAdapter mAdapter;
    private LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private Listener<Game[]> onResponseGames = new Listener<Game[]>() {
        public void onResponse(Game[] response) {
            if (response != null) {
                AllGamesFragment.this.mAdapter.addGames(response);
            }
        }
    };

    public static AllGamesFragment newInstance() {
        AllGamesFragment fragment = new AllGamesFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration decoration = new DividerItemDecoration(getResources().getDrawable(R.drawable.card_divider), false);
        decoration.enableHorizontalDelimiters(true, COLUMNS_COUNT);
        this.mRecyclerView.addItemDecoration(decoration);
        this.mLayoutManager = new GridLayoutManager(getActivity(), COLUMNS_COUNT);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mAdapter = new GameAdapter(getActivity(), new Game[0]);
        this.mAdapter.setOnItemClickListener(this);
        this.mRecyclerView.setAdapter(this.mAdapter);
        Network.getGames(getActivity(), this.onResponseGames, null);
        return view;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAMES_ALL);
        }
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Game game = null;
        if (this.mAdapter != null) {
            game = this.mAdapter.getItem(position);
        }
        if (game != null) {
            startActivity(GameActivity.getIntent(getActivity(), game));
        }
    }
}
