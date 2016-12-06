package com.louis.minesweeper;

import android.support.v4.util.Pair;

public class Coords extends Pair<Integer, Integer> {

	public Coords(Integer first, Integer second) {
		super(first, second);
	}

	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Coords) {
			return ((Coords)o).first == this.first && ((Coords)o).second == this.second;
		}
		return super.equals(o);
	}
}
