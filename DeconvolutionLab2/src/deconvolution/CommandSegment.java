package deconvolution;

public class CommandSegment implements Comparable<CommandSegment> {
	public String	keyword;
	public int		index	= 0;

	public CommandSegment(String keyword, int index) {
		this.keyword = keyword;
		this.index = index;
	}

	@Override
	public int compareTo(CommandSegment o) {
		return index > o.index ? 1 : -1;
	}

}