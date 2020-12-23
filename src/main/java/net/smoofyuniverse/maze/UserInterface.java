/*
 * Copyright (c) 2017-2020 Hugo Dupanloup (Yeregorix)
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

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import net.smoofyuniverse.common.app.App;
import net.smoofyuniverse.common.fx.animation.Stopwatch;
import net.smoofyuniverse.common.fx.dialog.Popup;
import net.smoofyuniverse.common.fx.field.DoubleField;
import net.smoofyuniverse.common.fx.field.IntegerField;
import net.smoofyuniverse.common.fx.field.LongField;
import net.smoofyuniverse.common.fx.task.ObservableProgressTask;
import net.smoofyuniverse.common.task.ProgressTask;
import net.smoofyuniverse.common.util.GridUtil;
import net.smoofyuniverse.logger.core.Logger;
import net.smoofyuniverse.maze.gen.Maze;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

public final class UserInterface extends GridPane {
	private static final Logger logger = App.getLogger("UserInterface");
	private static final Random random = new Random();
	
	private static int taskId = 0;
	
	public UserInterface() {
		Label title = new Label(App.get().getName());
		title.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));

		LongField seed = new LongField(0);
		IntegerField width = new IntegerField(1, 10000, 30), height = new IntegerField(1, 10000, 30);
		DoubleField error = new DoubleField(0, 1, 0);
		IntegerField whitePx = new IntegerField(1, 100, 2), blackPx = new IntegerField(1, 100, 1);

		Button randomSeed = new Button("Aléatoire");
		Button genMaze = new Button("Générer");
		randomSeed.setMaxWidth(Double.MAX_VALUE);
		genMaze.setMaxWidth(Double.MAX_VALUE);

		addRow(0, new Label("Graine:"), seed, randomSeed);
		addRow(1, new Label("Dimensions:"), width, height);
		addRow(2, new Label("Erreur:"), error);
		addRow(3, new Label("Pixels:"), whitePx, blackPx);
		add(genMaze, 0, 4, 3, 1);

		setAlignment(Pos.CENTER);
		setPadding(new Insets(10));
		setVgap(5);
		setHgap(5);
		getColumnConstraints().addAll(GridUtil.createColumn(16), GridUtil.createColumn(42), GridUtil.createColumn(42));

		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("Image", "*.png"));
		
		randomSeed.setOnAction((e) -> seed.valueProperty().set(random.nextLong()));
		
		genMaze.setOnAction((ev) -> {
			File f = chooser.showSaveDialog(MazeGen.get().getStage().orElse(null));
			if (f == null)
				return;

			long seedV = seed.getValue();
			int widthV = width.getValue(), heightV = height.getValue();
			double errorV = error.getValue();
			int whitePxV = whitePx.getValue(), blackPxV = blackPx.getValue();
			int id = ++taskId;

			logger.info("Starting generation task #" + id + " .. (" + widthV + "x" + heightV + ", " + whitePxV + ":" + blackPxV + ", seed:" + seedV + ")");

			Stopwatch stopwatch = new Stopwatch(5);
			ObservableProgressTask t = new ObservableProgressTask();
			t.titleProperty().bind(Bindings.concat("Durée: ", stopwatch.textProperty()));

			Consumer<ProgressTask> consumer = (task) -> {
				stopwatch.start();

				task.setMessage("Initialisation: " + widthV + "x" + heightV);
				Maze maze = new Maze(widthV, heightV);
				maze.listener = task;
				task.setMessage("Graine: " + seedV);
				Random r = new Random(seedV);

				task.setMessage("Instanciation: " + maze.points.length + " points.");
				maze.fill();
				task.setMessage("Connection des points ..");
				maze.connectAll(r, errorV);
				
				if (task.isCancelled()) {
					stopwatch.pause();
					System.gc();

					logger.info("Task #" + id + " has been cancelled. Duration: " + stopwatch.getText());
				} else {
					task.setMessage("Génération de l'image: " + whitePxV + ":" + blackPxV);
					BufferedImage img = maze.createImage(whitePxV, blackPxV);

					stopwatch.pause();
					System.gc();

					try {
						task.setMessage("Écriture ..");
						ImageIO.write(img, "png", f);

						logger.info("Task #" + id + " has terminated. Duration: " + stopwatch.getText());

						Popup.info().message("Graine: " + seedV + "\nDimensions: " + widthV + "x" + heightV + "\nPixels: " + whitePxV + ":" + blackPxV + "\nDurée: " + stopwatch.getText())
								.title("Opération terminée").header("L'image labyrinthe a été générée et écrite avec succès.").show();
					} catch (IOException e) {
						logger.error("Task #" + id + " has terminated but failed to write the generated image. Duration: " + stopwatch.getText(), e);

						Popup.error().title("Erreur d'écriture").header("Une erreur est survenue lors de l'écriture de l'image.").message(e).show();
					}
				}
			};
			
			Popup.consumer(consumer).task(t).title("Génération ..").submitAndWait();
		});
	}
}
