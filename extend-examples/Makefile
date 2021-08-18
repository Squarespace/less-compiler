
TARGETS := chain.css ex1-extend.css ex1-mixin.css

%.css : %.less
	../lessc $(LESSARGS) $< $@

all: $(TARGETS)

clean:
	rm -f $(TARGETS)

.PHONY: all clean

