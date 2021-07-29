package naiveversion2.common.fast;

/**
 * An implementation of a ring buffer queue
 */
@SuppressWarnings("unchecked")
public class FastQueue<T> {
	private T[] buf;
	private int l;
	private int r;
	private int ln;

	public FastQueue() {
		ln = 10000;
		buf = (T[])new Object[ln];
		l = 0;
		r = 0;
	}

	public FastQueue(int maxlen) {
		ln = maxlen + 5;
		buf = (T[])new Object[ln];
		l = 0;
		r = 0;
	}

	public boolean isEmpty() {
		return l == r;
	}

	public void clear() {
		l = r;
	}

	public int size() {
		return (r - l + ln) % ln;
	}

	public boolean add(T e) {
		if ((r + 1) % ln == l) return false;
		buf[r] = e;
		r++;
		r %= ln;
		return true;
	}

	public T peek() {
		if (l == r) return null;
		return buf[l];
	}

	public T poll() {
		if (l == r) return null;
		T v = buf[l];
		l++;
		l %= ln;
		return v;
	}

	// O(n) temp solution to remove.
	// TODO: Figure out a better way to remove from a queue. Priority queue?
	public void remove(T e) {
		if (l == r) return;

		int i = l;
		while(i != r && !buf[i].equals(e)) {
			i = (i + 1) % ln;
		}

		while(i != r) {
			buf[i] = buf[(i + 1) % ln];
			i = (i + 1) % ln;
		}
		r--;
	}
}