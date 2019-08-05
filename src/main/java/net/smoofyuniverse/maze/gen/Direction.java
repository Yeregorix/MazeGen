/*
 * Copyright (c) 2017-2019 Hugo Dupanloup (Yeregorix)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.smoofyuniverse.maze.gen;

import java.util.Random;

public enum Direction {
	UP, DOWN, LEFT, RIGHT;

	private static final Direction[][] combinations = new Direction[24][];

	public static Direction[] randomCombination(Random random) {
		return combinations[random.nextInt(24)];
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
						combinations[i++] = new Direction[]{d1, d2, d3, d4};
					}
				}
			}
		}
	}
}
