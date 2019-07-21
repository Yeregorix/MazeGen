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

import net.smoofyuniverse.common.task.ProgressListener;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Maze {
	public final int width, height;
	public final Point[] points;
	public ProgressListener listener;
	
	private double lastUpdate;
	
	public Maze(int width, int height) {
		this.width = width;
		this.height = height;
		this.points = new Point[width * height];
	}
	
	public void connectAll(Random r) {
		if (this.listener != null)
			this.listener.setCancelled(false);

		double total = (double) this.points.length;
		forceUpdate(0);
		int it = 0;

		// Shuffle all
		for (Point p : this.points) {
			p.shuffleCombinations(r);
			update(++it / total);
		}

		total *= 1.25;
		forceUpdate(0);
		it = 0;

		// Connect all
		RandomQueue<Point> queue = RandomQueue.of(this.points, r);
		while (!allConnected() && (this.listener == null || !this.listener.isCancelled())) {
			queue.next().connect();
			update(++it / total);
		}

		forceUpdate(1);
	}

	private void update(double progress) {
		if (progress - this.lastUpdate > 0.002)
			forceUpdate(progress);
	}

	public boolean contains(int x, int y) {
		return x >= 0 && x < this.width && y >= 0 && y < this.height;
	}

	public Point get(int x, int y) {
		return contains(x, y) ? this.points[y * this.width + x] : null;
	}

	public boolean allConnected() {
		return this.points[0].size() == this.points.length;
	}

	public void fill() {
		double total = (double) this.points.length;
		forceUpdate(0);

		int pos = 0;
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.points[pos] = new Point(x, y);
				update(++pos / total);
			}
		}

		forceUpdate(1);
	}

	private void forceUpdate(double progress) {
		this.lastUpdate = progress;
		if (this.listener != null)
			this.listener.setProgress(progress);
	}

	public BufferedImage createImage(int whitePx, int blackPx) {
		double total = (double) this.points.length;
		forceUpdate(0);

		int imgWidth = (this.width * whitePx) + ((this.width + 1) * blackPx);
		int imgHeight = (this.height * whitePx) + ((this.height + 1) * blackPx);
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_BINARY);

		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);

		int pos = 0;
		int imgY = blackPx;
		for (int y = 0; y < this.height; y++) {
			int imgX = blackPx;
			for (int x = 0; x < this.width; x++) {
				Point p = this.points[pos];

				g.fillRect(imgX, imgY, whitePx, whitePx);
				if (p.right)
					g.fillRect(imgX + whitePx, imgY, blackPx, whitePx);
				if (p.down)
					g.fillRect(imgX, imgY + whitePx, whitePx, blackPx);

				update(++pos / total);
				imgX += whitePx + blackPx;
			}
			imgY += whitePx + blackPx;
		}

		forceUpdate(1);

		return img;
	}

	public class Point extends Group {
		public final int posX, posY;

		private boolean right = false, down = false;
		private Direction[] directions;
		private int index;

		public Point(int posX, int posY) {
			this.posX = posX;
			this.posY = posY;
		}

		public void shuffleCombinations(Random r) {
			this.directions = Direction.randomCombination(r);
			this.index = -1;
		}

		public void connect() {
			while (this.index < 3) {
				this.index++;
				if (connect(this.directions[this.index]))
					return;
			}
		}

		public Point getRelative(Direction d) {
			return get(this.posX + d.dX, this.posY + d.dY);
		}

		public boolean connect(Direction d) {
			Point p2 = getRelative(d);
			if (!append(p2))
				return false;

			switch (d) {
			case UP:
				p2.down = true;
				break;
			case DOWN:
				this.down = true;
				break;
			case LEFT:
				p2.right = true;
				break;
			case RIGHT:
				this.right = true;
				break;
			}
			return true;
		}
	}
}
