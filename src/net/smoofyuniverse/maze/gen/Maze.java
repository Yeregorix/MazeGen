package net.smoofyuniverse.maze.gen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import net.smoofyuniverse.common.fxui.task.ObservableTask;

public class Maze {
	public final int width, height;
	public final Point[] points;
	
	public ObservableTask task;
	
	private double lastUpdate;
	
	public Maze(int width, int height) {
		this.width = width;
		this.height = height;
		this.points = new Point[width * height];
	}
	
	private void update(double progress) {
		if (progress - this.lastUpdate > 0.001)
			forceUpdate(progress);
	}
	
	private void forceUpdate(double progress) {
		this.lastUpdate = progress;
		if (this.task != null)
			this.task.setProgress(progress);
	}
	
	public boolean contains(int x, int y) {
		return x >= 0 && x < this.width && y >= 0 && y < this.height;
	}
	
	public Point get(int x, int y) {
		return contains(x, y) ? this.points[y * this.width + x] : null;
	}
	
	public void fill() {
		forceUpdate(0);
		
		int pos = 0;
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				this.points[pos] = new Point(x, y);
				pos++;
				
				update(pos / (double) this.points.length);
			}
		}
		
		forceUpdate(1);
	}
	
	public boolean allConnected() {
		return this.points[0].group.size() == this.points.length;
	}
	
	public void connectAll(Random r) {
		if (this.task != null)
			this.task.setCancelled(false);
		
		forceUpdate(0);
		
		// Shuffle all
		int it = 0;
		for (Point p : this.points) {
			p.shuffleDirection(r);
			
			it++;
			update(it / (double) this.points.length);
		}
		
		forceUpdate(1);
		
		// Prepare to watch progress
		int phase1 = this.points.length;
		int phase2 = (int) (phase1 * 0.25);
		
		double p1 = (1d / (double) phase1) * 0.1;
		double p2 = (1d / (double) phase2) * 0.9;
		
		double progress = 0;
		it = 0;
		
		forceUpdate(0);
		
		// Connect all
		RandomQueue<Point> queue = RandomQueue.of(this.points, r);
		while (!allConnected() && (this.task == null || !this.task.isCancelled())) {
			queue.next().connect();
			
			// Watch progress
			it++;
			if (it < phase1)
				progress += p1;
			else
				progress += p2;
			update(progress);
		}
		
		forceUpdate(1);
	}
	
	public class Point extends Group.Member {
		public final int posX, posY;
		
		private boolean connectedRight = false, connectedDown = false;
		private Direction[] directions;
		private int dirIndex;
		
		public Point(int posX, int posY) {
			this.posX = posX;
			this.posY = posY;
		}
		
		public void shuffleDirection(Random r) {
			this.directions = Direction.randomCombinaison(r);
			this.dirIndex = -1;
		}
		
		public void connect() {
			while (this.dirIndex < 3) {
				this.dirIndex++;
				if (connect(this.directions[this.dirIndex]))
					return;
			}
		}
		
		public Point getRelative(Direction d) {
			return get(this.posX + d.dX, this.posY + d.dY);
		}
		
		public boolean connect(Direction d) {
			Point p2 = getRelative(d);
			if (!this.group.append(p2))
				return false;
			
			switch (d) {
			case UP:
				p2.connectedDown = true;
				break;
			case DOWN:
				this.connectedDown = true;
				break;
			case LEFT:
				p2.connectedRight = true;
				break;
			case RIGHT:
				this.connectedRight = true;
				break;
			}
			return true;
		}
	}
	
	public BufferedImage createImage(int whitePx, int blackPx) {
		forceUpdate(0);
		
		int imgWidth = (this.width * whitePx) + ((this.width +1) * blackPx);
		int imgHeight = (this.height * whitePx) + ((this.height +1) * blackPx);
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_BINARY);
		
		Graphics2D g = img.createGraphics();
		g.setColor(Color.WHITE);
		
		int pos = 0;
		int imgY = blackPx;
		for (int y = 0; y < this.height; y++) {
			int imgX = blackPx;
			for (int x = 0; x < this.width; x++) {
				Point p = this.points[pos++];
				
				g.fillRect(imgX, imgY, whitePx, whitePx);
				if (p.connectedRight)
					g.fillRect(imgX + whitePx, imgY, blackPx, whitePx);
				if (p.connectedDown)
					g.fillRect(imgX, imgY + whitePx, whitePx, blackPx);
				
				update(pos / (double) this.points.length);
				imgX += whitePx + blackPx;
			}
			imgY += whitePx + blackPx;
		}
		
		forceUpdate(1);
		
		return img;
	}
}
