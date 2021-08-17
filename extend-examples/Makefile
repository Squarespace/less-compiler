
TARGETS := $(shell basename -a -s .less *.less | xargs -I % echo %.css)
#LESSARGS := --compress

%.css : %.less
	../lessc $(LESSARGS) $< $@

all: $(TARGETS)

clean:
	rm -f *.css

.PHONY: all clean

