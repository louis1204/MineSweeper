package com.louis.minesweeper;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass. Activities that contain this fragment
 * must implement the {@link WelcomeFragment.OnFragmentInteractionListener}
 * interface to handle interaction events. Use the
 * {@link WelcomeFragment#newInstance} factory method to create an instance of
 * this fragment.
 *
 */
public class WelcomeFragment extends Fragment {

	private Button mStartNewButton, mContinueButton;
	private int mSelectId;
	SoundPool mSound = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

	public static WelcomeFragment newInstance() {
		WelcomeFragment fragment = new WelcomeFragment();
		return fragment;
	}

	public WelcomeFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSelectId = mSound.load(getActivity(), R.raw.select, 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_welcome, container, false);
		mStartNewButton = (Button) v.findViewById(R.id.button_new);
		mStartNewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSound.play(mSelectId, 1, 1, 1, 0, 1);
				GameDB db = new GameDB(getActivity());
				db.resetTables();
				Intent i = new Intent(getActivity(), GameActivity.class);
				getActivity().startActivity(i);
			}
		});
		mContinueButton = (Button) v.findViewById(R.id.button_continue);
		mContinueButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mSound.play(mSelectId, 1, 1, 1, 0, 1);
				Intent i = new Intent(getActivity(), GameActivity.class);
				getActivity().startActivity(i);
			}
		});
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		GameDB db = new GameDB(getActivity());
		if (db.getRowCount() == 0) {
			mContinueButton.setVisibility(View.INVISIBLE);
		}
	}
}
