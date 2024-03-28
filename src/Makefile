ANTLR=../antlr-4.13.0-complete.jar

all: generate compile build run

gen: generate compile

exec: build run

generate:
	java -jar $(ANTLR) -o output MBKG.g4

compile:
	javac -cp $(ANTLR):output:. Main.java

build:
	java -cp $(ANTLR):output:. Main $(file) > test.ll
	
run:	
	lli test.ll

clean:
	rm -f test.ll
	rm -f *.class
	rm -rf output

