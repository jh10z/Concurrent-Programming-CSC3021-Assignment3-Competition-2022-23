package uk.ac.qub.csc3021.graph;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.IntBinaryOperator;

// Calculate the connected components using disjoint set data structure
// This algorithm only works correctly for undirected graphs
public class DisjointSetCC {
    private static class DSCCRelax implements Relax {
		DSCCRelax(AtomicIntegerArray parent_, AtomicIntegerArray subset_) {
			this.parent = parent_;

		}

		public void relax(int src, int dst) {
			//sameSet(src, dst);
			union(src, dst);
			//weightedUnion(src, dst);
		}

		public int find(int x) {
			//path compression (online research)
			int u = x;
			while(u != parent.get(u))
			{
				parent.set(u, parent.get(parent.get(u))); //set u to grandparent (halving path length)
				u = parent.get(u);
			}
			return u;
//			int u = x;
//			while(u != parent.get(u))
//			{
//				int v = parent.get(u);
//				int w = parent.get(v);
//				parent.set(u, w); //set u to grandparent (halving path length)
//				u = parent.get(u);
//			}
//			return u;

			//no path compression
//			int u = x;
//			while(u != parent.get(u)) {
//				u = parent.get(u);
//			}
//			return u;

			//path splitting
//			int u = x;
//			while(true) {
//				int v = parent.get(u);
//				int w = parent.get(v);
//				if (v == w) return v;
//				else {
//					parent.compareAndSet(parent.get(u), v, w);
//					u = v;
//				}
//			}

			//path halving
//			int u = x;
//			while(true) {
//				int v = parent.get(u);
//				int w = parent.get(v);
//				if(v == w) return v;
//				else {
//					parent.compareAndSet(parent.get(u), v, w);
//					u = parent.get(u);
//				}
//			}
		}

		private boolean sameSet(int x, int y) {
			int u = x;
			int v = y;
			while(true) {
				u = find(u);
				v = find(v);
				if(u == v) {
					return true;
				}
				if(u == parent.get(u)) {
					return false;
				}
			}
		}

		private boolean union(int x, int y) { //link
			int u = x;
			int v = y;

			while(true) {
				u = find(u);
				v = find(v);
				if(u < v) {
					if(parent.compareAndSet(parent.get(u), u, v)) {
						return false;
					}
				}
				else if (u == v) {
					return true;
				}
				else if (parent.compareAndSet(parent.get(v), v, u)) {
					return false;
				}
			}
		}

//		private void weightedUnion(int x, int y) { //link
//			int u = find(x);
//			int v = find(y);
//			int subset_u = subset.get(u);
//			int subset_v = subset.get(y);
//
//			//if(sameSet(x, y)) return;
//
//			if(subset_u < subset_v) {
//				parent.set(u, subset_v);
//				subset.set(v, subset_v + subset_u);
//			}
//			else {
//				parent.set(v, subset_u);
//				subset.set(u, subset_u + subset_v);
//			}
//		}

		// Variable declarations
		private AtomicIntegerArray parent;
		private AtomicIntegerArray subset;
	};

    public static int[] compute(SparseMatrix matrix) {
		long tm_start = System.nanoTime();

		final int n = matrix.getNumVertices();
		final AtomicIntegerArray parent = new AtomicIntegerArray(n);
		final AtomicIntegerArray subset = new AtomicIntegerArray(n);
		final boolean verbose = true;

		//Make Set
		for(int i = 0; i < n; ++i) {
			parent.set(i, i);
			subset.set(i, i);
		}

		DSCCRelax DSCCrelax = new DSCCRelax(parent, subset);

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
		tm_start = System.nanoTime();

		// Post-process the labels

		// 1. Count number of components
		//    and map component IDs to narrow domain
		int ncc = 0;
		int remap[] = new int[n];
		for (int i = 0; i < n; ++i)
			if (DSCCrelax.find(i) == i) {
				//System.out.println("Component: " + i);
				remap[i] = ncc++;
				//System.out.println("Component Remap: " + remap[i]);
			}

		if(verbose) {
			System.err.println("Number of components: " + ncc);
		}

		// 2. Calculate size of each component
		int sizes[] = new int[ncc];
		for(int i = 0; i < n; ++i) {
			++sizes[remap[DSCCrelax.find(i)]];
		}

		if(verbose) {
			System.err.println("DisjointSetCC: " + ncc + " components");
		}

		return sizes;
    }
}
