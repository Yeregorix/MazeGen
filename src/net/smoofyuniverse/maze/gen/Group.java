/*
 * Copyright (c) 2017 Hugo Dupanloup (Yeregorix)
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

public final class Group {
	private int size = 1;
	private Member first, last;
	
	private Group(Member m) {
		this.first = m;
		this.last = m;
	}
	
	public boolean append(Member m) {
		return m != null && append(m.group);
	}
	
	public boolean append(Group g) {
		if (this == g)
			return false;
		this.last.next = g.first;
		this.last = g.last;
		this.size += g.size;
		
		Member c = g.first;
		while (c != null) {
			c.group = this;
			c = c.next;
		}
		return true;
	}
	
	public int size() {
		return this.size;
	}
	
	public static class Member {
		public Group group;
		private Member next;
		
		public Member() {
			this.group = new Group(this);
		}
	}
}
