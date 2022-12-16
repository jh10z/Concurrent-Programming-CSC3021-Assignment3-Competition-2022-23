package uk.ac.qub.csc3021.graph;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Calculate the connected components using disjoint set data structure
// This algorithm only works correctly for undirected graphs
public class DisjointSetCC {
    private static class DSCCRelax implements Relax {
		DSCCRelax(AtomicIntegerArray parent_, AtomicIntegerArray rank_) {
			this.parent = parent_;
			this.rank = rank_;
		}

		public void relax(int src, int dst) {
			union(src, dst);
		}

		public int find(int x) { //recursive path halving
			if(parent.get(x) != x) {
				parent.set(x, find(parent.get(x)));
			}
			return parent.get(x);
		}

		private void union(int a, int b) { //union by rank
			int r = find(a);
			int s = find(b);

			if(r != s) {
				int rank_r = rank.get(r);
				int rank_s = rank.get(s);

				if(rank_r > rank_s) {
					parent.set(s, r);
				} else if(rank_r < rank_s) {
					parent.set(r, s);
				} else {
					parent.set(r, s);
					rank.set(s, ++rank_s);
				}
			}
		}

		// Variable declarations
		private final AtomicIntegerArray parent;
		private final AtomicIntegerArray rank;
	}

    public static int[] compute(SparseMatrix matrix) {
		long tm_start = System.nanoTime();

		final int n = matrix.getNumVertices();
		final AtomicIntegerArray parent = new AtomicIntegerArray(n);
		final AtomicIntegerArray rank = new AtomicIntegerArray(n);
		final boolean verbose = true;

		//Make Set
		for(int i = 0; i < n; ++i) {
			parent.set(i, i);
			rank.set(i, 0);
		}

		DSCCRelax DSCCrelax = new DSCCRelax(parent, rank);

		double tm_init = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println("Initialisation: " + tm_init + " seconds");
		tm_start = System.nanoTime();

		ParallelContext context = ParallelContextHolder.get();

		// 1. Make pass over graph
		context.edgemap(matrix, DSCCrelax);

		double tm_step = (double)(System.nanoTime() - tm_start) * 1e-9;
		if(verbose) {
			System.err.println("processing time=" + tm_step + " seconds");
		}

		// Post-process the labels

		// 1. Count number of components
		//    and map component IDs to narrow domain
		int ncc = 0;
		int[] remap = new int[n];
		for (int i = 0; i < n; ++i)
			if (DSCCrelax.find(i) == i) {
				remap[i] = ncc++;
			}

		if(verbose) {
			System.err.println("Number of components: " + ncc);
		}

		// 2. Calculate size of each component
		int[] sizes = new int[ncc];
		for(int i = 0; i < n; ++i) {
			++sizes[remap[DSCCrelax.find(i)]];
		}

		if(verbose) {
			System.err.println("DisjointSetCC: " + ncc + " components");
		}

		return sizes;
    }
}
