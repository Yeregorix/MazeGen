package net.smoofyuniverse.maze;

import java.util.Random;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import net.smoofyuniverse.common.app.Application;
import net.smoofyuniverse.common.app.Arguments;
import net.smoofyuniverse.maze.gen.Maze;

public class MazeGen extends Application {

	public static void main(String args[]) {
		new MazeGen(Arguments.parse(args));
	}
	
	public MazeGen(Arguments args) {
		super(args, "MazeGen", "Maze Generator", "1.0.0");
		initServices(Executors.newSingleThreadExecutor());
		Platform.runLater(() -> {
			initStage(550, 200, false, generateIcon());
			setScene(new UserInterface()).show();
		});
		checkForUpdate();
	}
	
	private static Image generateIcon() {
		Maze m = new Maze(5, 5);
		m.fill();
		m.connectAll(new Random());
		return SwingFXUtils.toFXImage(m.createImage(4, 2), null);
	}
}
