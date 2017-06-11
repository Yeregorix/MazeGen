package net.smoofyuniverse.maze.gen;

public final class Group {
	private int size = 1;
	private Member first, last;
	
	private Group(Member m) {
		this.first = m;
		this.last = m;
	}
	
	public boolean append(Member m) {
		if (m == null)
			return false;
		return append(m.group);
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
		private Member next;
		public Group group;
		
		public Member() {
			this.group = new Group(this);
		}
	}
}
