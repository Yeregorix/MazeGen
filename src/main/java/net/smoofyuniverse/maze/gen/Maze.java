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
	public final int width, height, size;
	public final Point[] points;
	public ProgressListener listener;

	private double lastUpdate;
	
	public Maze(int width, int height) {
		if (width <= 0)
			throw new IllegalArgumentException("width");
		if (height <= 0)
			throw new IllegalArgumentException("height");

		this.width = width;
		this.height = height;
		this.size = width * height;
		this.points = new Point[this.size];
	}

	public void fill() {
		forceUpdate(0);

		for (int i = 0; i < this.size; i++) {
			this.points[i] = new Point(i);
			update(i / (double) this.size);
		}

		forceUpdate(1);
	}

	public void connectAll(Random random, double errorFactor) {
		if (random == null)
			throw new IllegalArgumentException("random");
		if (errorFactor < 0 || errorFactor > 1)
			throw new IllegalArgumentException("errorFactor");

		if (this.listener != null)
			this.listener.setCancelled(false);

		forceUpdate(0);

		for (int i = 0; i < this.size; i++) {
			this.points[i].shuffleCombinations(random);
			update(i / (double) this.size);
		}

		forceUpdate(0);

		int max = this.size - 1, connections = 0;
		RandomQueue<Point> queue = RandomQueue.of(this.points, random);
		while (this.listener == null || !this.listener.isCancelled()) {
			if (queue.next().tryConnect()) {
				update(++connections / (double) max);
				if (connections == max)
					break;
			}
		}

		if (this.listener != null && this.listener.isCancelled())
			return;

		forceUpdate(1);

		int errors = (int) ((this.size - this.width - this.height + 1) * errorFactor);
		if (errors == 0)
			return;

		forceUpdate(0);

		for (int i = 0; i < this.size; i++) {
			this.points[i].resetIndex();
			update(i / (double) this.size);
		}

		forceUpdate(0);

		connections = 0;
		queue.reset();
		while (this.listener == null || !this.listener.isCancelled()) {
			if (queue.next().forceConnect()) {
				update(++connections / (double) errors);
				if (connections == errors)
					break;
			}
		}

		if (this.listener != null && this.listener.isCancelled())
			return;

		forceUpdate(1);
	}

	private void update(double progress) {
		if (progress - this.lastUpdate > 0.002)
			forceUpdate(progress);
	}

	private void forceUpdate(double progress) {
		this.lastUpdate = progress;
		if (this.listener != null)
			this.listener.setProgress(progress);
	}

	public BufferedImage createImage(int whitePx, int blackPx) {
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

				update(++pos / (double) this.size);
				imgX += whitePx + blackPx;
			}
			imgY += whitePx + blackPx;
		}

		forceUpdate(1);

		return img;
	}

	public class Point extends Group {
		public final int position;

		private boolean right = false, down = false;
		private Direction[] directions;
		private byte index;

		public Point(int position) {
			this.position = position;
		}

		public void shuffleCombinations(Random random) {
			this.directions = Direction.randomCombination(random);
			resetIndex();
		}

		public void resetIndex() {
			this.index = -1;
		}

		public boolean tryConnect() {
			while (this.index < 3) {
				this.index++;
				if (tryConnect(this.directions[this.index]))
					return true;
			}
			return false;
		}

		public boolean tryConnect(Direction d) {
			switch (d) {
				case UP:
					int rel = this.position - Maze.this.width;
					if (rel < 0)
						return false;

					Point p = Maze.this.points[rel];
					if (!append(p))
						return false;

					p.down = true;
					return true;
				case DOWN:
					rel = this.position + Maze.this.width;
					if (rel >= Maze.this.size)
						return false;

					p = Maze.this.points[rel];
					if (!append(p))
						return false;

					this.down = true;
					return true;
				case LEFT:
					if (this.position % Maze.this.width == 0)
						return false;

					p = Maze.this.points[this.position - 1];
					if (!append(p))
						return false;

					p.right = true;
					return true;
				case RIGHT:
					rel = this.position + 1;
					if (rel % Maze.this.width == 0)
						return false;

					p = Maze.this.points[rel];
					if (!append(p))
						return false;

					this.right = true;
					return true;
			}
			throw new IllegalArgumentException();
		}

		public boolean forceConnect() {
			while (this.index < 3) {
				this.index++;
				if (forceConnect(this.directions[this.index]))
					return true;
			}
			return false;
		}

		public boolean forceConnect(Direction d) {
			switch (d) {
				case UP:
					int rel = this.position - Maze.this.width;
					if (rel < 0)
						return false;

					Point p = Maze.this.points[rel];
					if (p.down)
						return false;

					p.down = true;
					return true;
				case DOWN:
					rel = this.position + Maze.this.width;
					if (rel >= Maze.this.size)
						return false;

					if (this.down)
						return false;

					this.down = true;
					return true;
				case LEFT:
					if (this.position % Maze.this.width == 0)
						return false;

					p = Maze.this.points[this.position - 1];
					if (p.right)
						return false;

					p.right = true;
					return true;
				case RIGHT:
					rel = this.position + 1;
					if (rel % Maze.this.width == 0)
						return false;

					if (this.right)
						return false;

					this.right = true;
					return true;
			}
			throw new IllegalArgumentException();
		}
	}
}
