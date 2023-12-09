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
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.environment.source.GitHubReleaseSource;
import net.smoofyuniverse.maze.gen.Maze;

import java.util.Random;

public class MazeGen extends Application {

	@Override
	public void init() throws Exception {
		if (!detectJavaFXSwing())
			getManager().setupDependencies("javafx-extra");
	}

	private static boolean detectJavaFXSwing() {
		try {
			Class.forName("javafx.embed.swing.SwingFXUtils");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	public void run() {
		runLater(() -> {
			Stage stage = createStage(550, 200, generateIcon());
			setStage(stage);

			stage.setScene(new Scene(new UserInterface()));
			stage.show();
		});

		getManager().runUpdater(new GitHubReleaseSource("Yeregorix", "MazeGen", null, "MazeGen", getManager().getConnectionConfig()));
	}
	
	private static Image generateIcon() {
		Maze m = new Maze(5, 5);
		m.fill();
		m.connectAll(new Random(), 0);
		return SwingFXUtils.toFXImage(m.createImage(4, 2), null);
	}
}
