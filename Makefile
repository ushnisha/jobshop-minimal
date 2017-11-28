.PHONY: build exec jdocs clean all
ROOT = $(shell pwd)
PKG = com/ushnisha/JobShop
SRC = sources
DEST =classes
BIN = bin
DOCS = docs
JFLAGS = -g -d $(ROOT)/$(DEST)
JC = javac
JDOC = javadoc
JAR = jar
JARFLAGS = cvfm

build:
	cd $(ROOT)/$(SRC) && \
	$(JC) $(JFLAGS) $(PKG)/*.java

exec: build
	cd $(ROOT)/$(DEST) && \
	$(JAR) $(JARFLAGS) JobShop.jar $(ROOT)/manifest.txt $(PKG)/*.class && \
	mv JobShop.jar $(ROOT)/$(BIN)

jdocs:
	cd $(ROOT)/$(SRC) && \
	$(JDOC) -sourcepath . com.ushnisha.JobShop -d $(ROOT)/$(DOCS)

clean:
	$(RM) $(ROOT)/$(DEST)/$(PKG)/*.class && \
	$(RM) $(ROOT)/$(BIN)/*.jar && \
	$(RM) $(ROOT)/$(DOCS)/$(PKG)/*.html && \
	$(RM) $(ROOT)/$(DOCS)/$(PKG)/*.css && \
	$(RM) $(ROOT)/$(DOCS)/$(PKG)/*.js && \
	$(RM) $(ROOT)/$(DOCS)/$(PKG)/package-list

all: clean
	make jdocs && make build && \
	make exec
