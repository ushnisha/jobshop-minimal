.PHONY: dirs build exec jdocs clean all
ROOT = $(shell pwd)
PKG = com/ushnisha/JobShop
SRC = sources
DEST = classes
TEST = tests
SCRIPTS = scripts
LIB = lib
DOCS = docs
JFLAGS = -g -d $(ROOT)/$(DEST)
JC = javac
JDOC = javadoc
JAR = jar
JARFLAGS = cvfm
TESTNAME = ALL

dirs: 
	( test -d $(ROOT)/$(DEST) || mkdir -p $(ROOT)/$(DEST)/$(PKG) ) && \
	( test -d $(ROOT)/$(LIB) || mkdir -p $(ROOT)/$(LIB) ) && \
	( test -d $(ROOT)/$(DOCS) || mkdir -p $(ROOT)/$(DOCS) ) && \
	( test -d $(ROOT)/$(TEST)/outputs || mkdir $(ROOT)/$(TEST)/outputs ) && \
	( test -d $(ROOT)/data || mkdir -p $(ROOT)/data )

build: dirs
	cd $(ROOT)/$(SRC) && \
	$(JC) $(JFLAGS) $(PKG)/*.java

exec: build
	cd $(ROOT)/$(DEST) && \
	$(JAR) $(JARFLAGS) JobShop.jar $(ROOT)/manifest.txt $(PKG)/*.class && \
	mv JobShop.jar $(ROOT)/$(LIB)

jdocs: dirs
	cd $(ROOT)/$(SRC) && \
	$(JDOC) -sourcepath . -Xdoclint:none com.ushnisha.JobShop -d $(ROOT)/$(DOCS)

clean: dirs
	$(RM) $(ROOT)/$(DEST)/$(PKG)/*.class && \
	$(RM) $(ROOT)/$(LIB)/JobShop.jar && \
	$(RM) $(ROOT)/$(DOCS)/*.html && \
	$(RM) $(ROOT)/$(DOCS)/*.css && \
	$(RM) $(ROOT)/$(DOCS)/*.js && \
	$(RM) $(ROOT)/$(DOCS)/package-list && \
	$(RM) $(ROOT)/$(DOCS)/$(PKG)/*.html && \
	$(RM) $(ROOT)/$(TEST)/outputs/*.out && \
	$(RM) $(ROOT)/data/*.csv

all: clean
	make jdocs && make build && \
	make exec

tests: all
	clear && \
	$(ROOT)/$(SCRIPTS)/run_tests.sh $(TESTNAME) $(ROOT)/$(TEST) $(ROOT)/$(LIB)
