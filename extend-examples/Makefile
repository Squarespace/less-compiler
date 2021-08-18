
TARGETS := chain.css ex1-mixin.css ex1-extend.css ex2-mixin.css ex2-extend.css

%.css : %.less
	../lessc $(LESSARGS) $< $@

all: $(TARGETS)

clean:
	rm -f $(TARGETS)

.PHONY: all clean

