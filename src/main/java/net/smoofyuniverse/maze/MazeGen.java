/*
 * Copyright (c) 2017-2021 Hugo Dupanloup (Yeregorix)
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

package net.smoofyuniverse.maze;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.Arguments;
import net.smoofyuniverse.common.environment.ApplicationUpdater;
import net.smoofyuniverse.common.environment.source.GithubReleaseSource;
import net.smoofyuniverse.maze.gen.Maze;

import java.util.Random;

public class MazeGen extends Application {

	public MazeGen(Arguments args) {
		super(args, "MazeGen", "Maze Generator", "1.3.2");
	}

	@Override
	public void init() {
		requireGUI();
		initServices();

		runLater(() -> {
			initStage(550, 200, generateIcon());
			setScene(new UserInterface()).show();
		});

		new ApplicationUpdater(this, new GithubReleaseSource("Yeregorix", "MazeGen", null, "MazeGen", getConnectionConfig())).run();
	}

	public static void main(String[] args) {
		new MazeGen(Arguments.parse(args)).launch();
	}
	
	private static Image generateIcon() {
		Maze m = new Maze(5, 5);
		m.fill();
		m.connectAll(new Random(), 0);
		return SwingFXUtils.toFXImage(m.createImage(4, 2), null);
	}
}
