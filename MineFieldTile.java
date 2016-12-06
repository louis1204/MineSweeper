package com.louis.minesweeper;

import android.content.Context;
import android.widget.Button;

public class MineFieldTile extends Button {

	private boolean mIsBomb;
	private boolean mIsVisited;
	private Coords mCoords;
	private int mBombsAround;
	private boolean mIsFlagged;

	public MineFieldTile(Context context) {
		super(context);
	}

	public MineFieldTile(Context context, Integer row, Integer col) {
		super(context);
		this.mCoords = new Coords(row, col);
		this.mBombsAround = 0;
		this.mIsBomb = false;
		this.mIsVisited = false;
		this.mIsFlagged = false;
	}

	public boolean isBomb() {
		return mIsBomb;
	}
	public void setIsBomb(boolean isBomb) {
		this.mIsBomb = isBomb;
	}

	public boolean isVisited() {
		return mIsVisited;
	}

	public void setVisited(boolean isVisited) {
		this.mIsVisited = isVisited;
	}

	public Coords getCoords() {
		return this.mCoords;
	}

	public int getBombsAround() {
		return mBombsAround;
	}

	public void setBombsAround(int bombsAround) {
		mBombsAround = bombsAround;
	}

	public boolean isFlagged() {
		return mIsFlagged;
	}

	public void setFlagged(boolean isFlagged) {
		mIsFlagged = isFlagged;
	}
}
