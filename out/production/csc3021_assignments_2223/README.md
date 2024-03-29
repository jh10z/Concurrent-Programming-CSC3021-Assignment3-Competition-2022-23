# csc3021 / 2021 assignments / GraphProcessing

This directory holds code files for the graph processing problem.
It implements routines to solve the PageRank problem and the Connected Components problem. The latter is valid only for undirected graphs (a.k.a simple graphs).

The code is complete except for the implementation of the data structure that represents the graphs. Three variants are provided and can be selected at runtime: the Coordinate formate (COO), the Compressed Sparse Rows format (CSR) and the Compressed Sparse Columsn format (CSC).

Graph data sets are available from http://www.eeecs.qub.ac.uk/~H.Vandierendonck/CSC3021/graphs/. These are specified in the file format described in the assignment brief.

The programs can be called using a driver program called Driver.java. This driver requires command-line arguments to specify what to do (the problem: PageRank or Connected Components; the graph data structure; number of threads to use).

Command line arguments are easily specified when running the programs on the command line. For those with a UNIX-like setup, there is also a Makefile to compile the programs and to create a ZIP file for submission on the DOMjudge server.

IDEs also allow to set the programs command-line arguments, which requires a sequence of GUI actions that is more cumbersome. Google the docs for your GUI to find out how; any problems put questions on the Canvas forum.

The code contains assertions, which are checks for correctness that are executed at runtime. Sometimes programming errors can be caught faster and diagnosed more easily if proper assertions are put in. Java disables assertions by default (oh why?). To enable them, use the command-line argument -ea for the java runtime.

The command line arguments are as follows:

When compiling the DriverA2 file
% javac Driver.java
% java -ea Driver
Usage: java Driver algorithm num-threads outputfile format inputfiles...
% java Driver (pr|cc) 1 /path/to/outputfile.txt COO /path/to/graph.COO
Replace the path /path/to/graph.COO with the directory and filename for the graph file of your choice. Choose one of pr or cc.  Specify a file to store the program output (either PageRank values or histogram of cluster sizes). This code will execute the program on a single thread of execution.

This will execute using 4 threads, once you developed the code.
% java Driver (pr|cc) 4 /path/to/outputfile.txt PARCSC /path/to/graph.CSC
