# this is the top level makefile
VDIR=emulator/generated-src
SDIR=src/main/scala

# build the verilog files
all: emulator

emulator: $(VDIR)/top.v
	make -f Makefile_ver

$(VDIR)/top.v: $(SDIR)/top/top.scala $(SDIR)/tile/*.scala
	sbt run

clean:
	rm -rf emulator/generated-src/*