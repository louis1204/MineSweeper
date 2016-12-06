package com.louis.minesweeper;

import java.util.HashMap;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.Toast;

public class GameFragment extends Fragment {
	private static final int NUM_ROWS = 8;
	private static final int NUM_COLS = 8;
	private static final int TILE_MARGIN = 4;
	private static int mTileWidth;
	private TableLayout mMineField;
	private ImageButton mValidateButton;
	private Button mCheatButton, mNewButton;
	private int mNumMines = 10;
	private HashMap<Coords, MineFieldTile> blocks = new HashMap<Coords, MineFieldTile>();
	private Boolean mIsGameOver = false;
	private SoundPool mSound = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
	private int mRevealId, mWinId, mLoseId, mSelectId, mFlagId;
	private int mHiddenTiles = 64;
	private static final int TOTAL_PADDING_SPACE = TILE_MARGIN * 2 * (NUM_COLS + 1);

	public static GameFragment newInstance() {
		GameFragment fragment = new GameFragment();
		return fragment;
	}

	public GameFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpTileWidth();
		setUpSounds();
	}

	@SuppressLint("NewApi")
	private void setUpTileWidth() {
		WindowManager wm = (WindowManager) getActivity().getSystemService(
				Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT < 13) {
			if (display.getWidth() < display.getHeight()) {
				mTileWidth = (display.getWidth() - TOTAL_PADDING_SPACE) / NUM_COLS;
			} else {
				mTileWidth = (display.getHeight() - TOTAL_PADDING_SPACE) / NUM_COLS;
			}
		} else {
			Point size = new Point();
			display.getSize(size);
			if (size.x < size.y) {
				mTileWidth = (size.x - (NUM_COLS * 9)) / NUM_COLS;
			} else {
				mTileWidth = (size.y - (NUM_COLS * 9)) / NUM_COLS;
			}
		}
	}

	private void setUpSounds() {
		mRevealId = mSound.load(getActivity(), R.raw.reveal, 1);
		mWinId = mSound.load(getActivity(), R.raw.win, 1);
		mLoseId = mSound.load(getActivity(), R.raw.lose, 1);
		mSelectId = mSound.load(getActivity(), R.raw.select, 1);
		mFlagId = mSound.load(getActivity(), R.raw.flag, 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_game, container, false);
		mValidateButton = (ImageButton) v.findViewById(R.id.validate_button);
		mValidateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSound.play(mSelectId, 1, 1, 1, 0, 1);
				if (!mIsGameOver) {
					checkWinConditions();
				}
			}
		});
		mNewButton = (Button) v.findViewById(R.id.new_button);
		mNewButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSound.play(mSelectId, 1, 1, 1, 0, 1);
				startGame();
			}
		});
		mCheatButton = (Button) v.findViewById(R.id.cheat_button);
		mCheatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mSound.play(mSelectId, 1, 1, 1, 0, 1);
				cheat();
			}
		});
		mMineField = (TableLayout) v.findViewById(R.id.mine_field);
		GameDB db = new GameDB(getActivity());
		if (db.getRowCount() == 0) {
			showMineField(null);
			setUpMines();
		} else {
			showMineField(db.getSavedGame(getActivity()));
		}
		return v;
	}

	private void showMineField(HashMap<Coords, MineFieldTile> blocks) {
		for (int row = 0; row < NUM_ROWS; row++) {
			TableRow tableRow = new TableRow(getActivity());
			tableRow.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			for (int column = 0; column < NUM_COLS; column++) {
				final MineFieldTile tile;
				if (blocks == null) {
					tile = new MineFieldTile(getActivity(), row, column);
				} else {
					tile = blocks.get(new Coords(row, column));
				}
				tile.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!mIsGameOver && !tile.isFlagged()) {
							if (!tile.isBomb()) {
								mSound.play(mRevealId, 0.3f, 0.3f, 1, 0, 1);
								revealTiles(tile);
								if (mHiddenTiles == 10) {
									winGameOnRevealing();
								}
							} else {
								Toast.makeText(getActivity(), "You lose",
										Toast.LENGTH_SHORT).show();
								mSound.play(mLoseId, 1, 1, 1, 0, 1);
								mIsGameOver = true;
								tile.setVisited(true);
								tile.setBackgroundColor(getResources()
										.getColor(R.color.red));
								tile.setText("B");
								tile.setTextColor(getResources().getColor(
										R.color.white));
								return;
							}
						}
					}
				});
				tile.setOnLongClickListener(new OnLongClickListener() {
					@SuppressLint("NewApi")
					@Override
					public boolean onLongClick(View v) {
						if (!mIsGameOver && !tile.isVisited()) {
							mSound.play(mFlagId, 0.3f, 0.3f, 1, 0, 1);
							if (!tile.isFlagged()) {
								tile.setFlagged(true);
								if (Build.VERSION.SDK_INT > 15) {
									tile.setBackground(getResources()
											.getDrawable(R.drawable.flag));
								} else {
									tile.setBackgroundDrawable(getResources()
											.getDrawable(R.drawable.flag));
								}
							} else if (tile.isFlagged()) {
								tile.setFlagged(false);
								if (Build.VERSION.SDK_INT > 15) {
									if (!tile.isVisited()) {
										tile.setBackground(getResources()
												.getDrawable(
														R.drawable.tile_selector));
									} else {
										tile.setBackgroundDrawable(getResources()
												.getDrawable(
														R.drawable.tile_selector));
									}
								}
							}
						}
						return true;
					}
				});
				this.blocks.put(new Coords(row, column), tile);
				LayoutParams params = new LayoutParams(mTileWidth, mTileWidth);
				params.setMargins(TILE_MARGIN, TILE_MARGIN, TILE_MARGIN,
						TILE_MARGIN);
				tile.setLayoutParams(params);
				setBackGround(tile);
				tableRow.addView(tile);
			}
			mMineField.addView(tableRow, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

	@SuppressLint("NewApi")
	private void setBackGround(MineFieldTile tile) {
		if (Build.VERSION.SDK_INT > 15) {
			if (tile.isVisited()) {
				if (tile.getBombsAround() > 0) {
					tile.setText("" + tile.getBombsAround());
					tile.setBackgroundColor(getResources().getColor(
							R.color.DarkGray));
					tile.setTextColor(getResources().getColor(R.color.white));
					mHiddenTiles--;
				} else {
					tile.getBackground().setAlpha(0);
					mHiddenTiles--;
				}
				if (tile.isBomb()) {
					tile.setBackgroundColor(getResources()
							.getColor(R.color.red));
					tile.setText("B");
					tile.setTextColor(getResources().getColor(R.color.white));
					mIsGameOver = true;
				}
			} else if (tile.isFlagged()) {
				tile.setBackground(getResources().getDrawable(R.drawable.flag));
			} else {
				tile.setBackground(getResources().getDrawable(
						R.drawable.tile_selector));
			}
		} else {
			if (tile.isVisited()) {
				if (tile.getBombsAround() > 0) {
					tile.setText("" + tile.getBombsAround());
					tile.setBackgroundColor(getResources().getColor(
							R.color.DarkGray));
					tile.setTextColor(getResources().getColor(R.color.white));
					mHiddenTiles--;
				} else {
					tile.getBackground().setAlpha(0);
					mHiddenTiles--;
				}
				if (tile.isBomb()) {
					tile.setBackgroundColor(getResources()
							.getColor(R.color.red));
					tile.setText("B");
					tile.setTextColor(getResources().getColor(R.color.white));
					mIsGameOver = true;
				}
			} else if (tile.isFlagged()) {
				tile.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.flag));
			} else {
				tile.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.tile_selector));
			}
		}
	}

	private void setUpMines() {
		int mineCount = 0;
		Random rand = new Random();
		while (mineCount < mNumMines) {
			Coords coords = new Coords(Math.abs(rand.nextInt() % 8),
					Math.abs(rand.nextInt() % 8));
			MineFieldTile tile = blocks.get(coords);
			if (tile != null && !tile.isBomb()) {
				tile.setIsBomb(true);
				incrementSurroundingBombs(coords);
				mineCount++;
			}
		}
	}

	private void incrementSurroundingBombs(Coords coords) {
		MineFieldTile tile;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				if (i != j || i != 0) {
					tile = blocks.get(new Coords(coords.first + i,
							coords.second + j));
					if (tile != null && !tile.isBomb()) {
						tile.setBombsAround(tile.getBombsAround() + 1);
					}
				}
			}
		}
	}

	private void revealTiles(MineFieldTile tile) {
		if (tile != null && !tile.isBomb() && !tile.isVisited()
				&& !tile.isFlagged()) {
			mHiddenTiles--;
			tile.setVisited(true);
			if (tile.getBombsAround() > 0) {
				tile.setText("" + tile.getBombsAround());
				tile.setBackgroundColor(getResources().getColor(
						R.color.DarkGray));
				tile.setTextColor(getResources().getColor(R.color.white));
			} else {
				tile.getBackground().setAlpha(0);
			}
			if (tile.getBombsAround() == 0) {
				for (int i = -1; i < 2; i++) {
					for (int j = -1; j < 2; j++) {
						if (i != j || i != 0) {
							int first = tile.getCoords().first;
							int second = tile.getCoords().second;
							MineFieldTile tileAround = blocks.get(new Coords(
									first + i, second + j));
							if (tileAround != null) {
								revealTiles(tileAround);
							}
						}
					}
				}
			}
		}
	}

	private void checkWinConditions() {
		mIsGameOver = true;
		for (MineFieldTile tile : blocks.values()) {
			if (!tile.isBomb() && !tile.isVisited()) {
				Toast.makeText(getActivity(), "You lose", Toast.LENGTH_SHORT)
						.show();
				mSound.play(mLoseId, 1, 1, 1, 0, 1);
				showRedBombs();
				return;
			}
		}
		Toast.makeText(getActivity(), "You win", Toast.LENGTH_SHORT).show();
		mSound.play(mWinId, 1, 1, 1, 0, 1);
		showGreenBombs();
		return;
	}

	private void startGame() {
		mHiddenTiles = 64;
		mMineField.removeAllViews();
		mIsGameOver = false;
		showMineField(null);
		setUpMines();
	}

	private void cheat() {
		for (MineFieldTile tile : blocks.values()) {
			if (tile.isBomb()) {
				tile.setText("B");
				tile.setTextColor(getResources().getColor(R.color.white));
			}
		}
	}

	private void showRedBombs() {
		for (MineFieldTile tile : blocks.values()) {
			if (tile.isBomb()) {
				tile.setText("B");
				tile.setTextColor(getResources().getColor(R.color.white));
				tile.setBackgroundColor(getResources().getColor(R.color.red));
			}
		}
	}

	private void showGreenBombs() {
		for (MineFieldTile tile : blocks.values()) {
			if (tile.isBomb()) {
				tile.setText("B");
				tile.setTextColor(getResources().getColor(R.color.white));
				tile.setBackgroundColor(getResources().getColor(R.color.green));
			}
		}
	}

	private void winGameOnRevealing() {
		mIsGameOver = true;
		Toast.makeText(getActivity(), "You win", Toast.LENGTH_SHORT).show();
		mSound.play(mWinId, 1, 1, 1, 0, 1);
		showGreenBombs();
	}

	@Override
	public void onDetach() {
		if (!mIsGameOver) {
			GameDB db = new GameDB(getActivity());
			db.addTiles(blocks.values());
		} else {
			GameDB db = new GameDB(getActivity());
			db.resetTables();
		}
		super.onDetach();
	}
}
