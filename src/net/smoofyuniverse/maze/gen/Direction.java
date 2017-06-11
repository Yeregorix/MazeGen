package net.smoofyuniverse.maze.gen;

import java.util.Random;

public enum Direction {
	UP(0, -1),
	DOWN(0, +1),
	LEFT(-1, 0),
	RIGHT(+1, 0);
	
	public final int dX, dY;
	
	private Direction(int dX, int dY) {
		this.dX = dX;
		this.dY = dY;
	}
	
	private static final Direction[][] combinaisons = new Direction[24][];
	
	public static Direction[] randomCombinaison(Random r) {
		return combinaisons[r.nextInt(24)];
	}
	
	static {
		int i = 0;
		for (Direction d1 : values()) {
			for (Direction d2 : values()) {
				if (d2 == d1)
					continue;
				for (Direction d3 : values()) {
					if (d3 == d1 || d3 == d2)
						continue;
					for (Direction d4 : values()) {
						if (d4 == d1 || d4 == d2 || d4 == d3)
							continue;
						combinaisons[i++] = new Direction[] {d1, d2, d3, d4};
					}
				}
			}
		}
	}
}
