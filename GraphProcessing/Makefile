JFLAGS = -g
JAVAC = javac

CLASS_STEMS = Relax SparseMatrix SparseMatrixCOO SparseMatrixCSR \
	      SparseMatrixCSC PageRank ConnectedComponents ParallelContext \
	      ParallelContextHolder ParallelContextSingleThread \
	      ParallelContextSimple ParallelContextQ3 DisjointSetCC

PKG_CLASS_FILES = $(patsubst %,uk/ac/qub/csc3021/graph/%.class,$(CLASS_STEMS))
PKG_JAVA_FILES = $(patsubst %,uk/ac/qub/csc3021/graph/%.java,$(CLASS_STEMS))

PACKAGE = lib/graph.jar

CLASS_FILES = Driver.class

SUBMIT = Driver.java $(PKG_JAVA_FILES)

ALL = $(PKG_CLASS_FILES) $(PACKAGE) $(CLASS_FILES)

all: $(ALL)

Driver.class: $(PKG_CLASS_FILES) $(PACKAGE) Driver.java
	$(JAVAC) $(JFLAGS) -cp . Driver.java

Validator.class: $(PKG_CLASS_FILES) $(PACKAGE) Validator.java
	$(JAVAC) $(JFLAGS) -cp . Validator.java

%.class: %.java
	$(JAVAC) -d . -cp . $(JFLAGS) $<

$(PACKAGE): $(PKG_CLASS_FILES)
	mkdir -p lib
	jar cf $(PACKAGE) $(PKG_CLASS_FILES)

submit.zip: $(SUBMIT)
	zip submit.zip $(SUBMIT)

clean:
	$(RM) $(ALL)
	$(RM) uk/ac/qub/csc3021/graph/*.class
