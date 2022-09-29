package uk.ac.qub.csc3021.graph;

public abstract class SparseMatrix {
    // Return number of vertices in the graph
    public abstract int getNumVertices();

    // Return number of edges in the graph
    public abstract int getNumEdges();

    // Auxiliary in preparation of PageRank iteration: pre-calculate the
    // out-degree (number of outgoing edges) for each vertex
    public abstract void calculateOutDegree( int outdeg[] );

    // Perform one sweep over all edges in the graph, calling the functional
    // interface Relax once for each edge.
    public abstract void edgemap( Relax relax );

    // Perform part of a sweep, visiting only a subset of the edges. This
    // method is used only in Assignment 2.
    public abstract void ranged_edgemap( Relax relax, int from, int to );
}

