package net.smoofyuniverse.maze.gen;

import java.util.Arrays;
import java.util.Random;

public class RandomQueue<T> {
	private Random random;
	private T[] values;
	private int remainingSize;
	
	public RandomQueue(T[] values, Random r) {
		this.values = values;
		this.random = r;
	}
	
	public T next() {
		if (this.remainingSize == 0)
			this.remainingSize = this.values.length;
		int index = this.random.nextInt(this.remainingSize);
		T value = this.values[index];
		this.remainingSize--;
		this.values[index] = this.values[this.remainingSize];
		this.values[this.remainingSize] = value;
		return value;
	}
	
	public static <T> RandomQueue<T> of(T[] values, Random r) {
		return new RandomQueue(Arrays.copyOf(values, values.length), r);
	}
}
