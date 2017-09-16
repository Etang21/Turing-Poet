
class NextWord implements Comparable<NextWord> {
	String word;
	double freq;
	
	NextWord(String wordIn, double freqIn) {
		word = wordIn;
		freq = freqIn;
	}

	@Override
	public int compareTo(NextWord o) {
		if(freq > o.freq) {
			return 1;
		}
		if(freq == o.freq) {
			return 0;
		}
		return -1;
	}
	
	public String toString() {
		return freq + ":" + word;
	}	

}


