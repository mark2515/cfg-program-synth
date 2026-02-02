# cfg-program-synth
This project implements an example-based program synthesizer for a small expression language defined by a context-free grammar (CFG). Given a set of input-output examples, the synthesizer automatically constructs a program f(x, y, z) that is consistent with all provided examples, if such a program exists.

# Project Structure
```
cfg-program-synth/
├── benchmarks/        # Sample input examples
├── src/
│   ├── main/java/synth/
│   │   ├── cfg/        # CFG data structures
│   │   ├── core/       # Synthesizer, AST, interpreter
│   │   ├── util/       # Utilities (parser, file I/O)
│   │   └── Main.java   # Program entry point
│   └── test/java/      # JUnit tests
├── pom.xml             # Maven configuration
└── README.md
```

# Compilation
This project uses Maven.
From the cfg-program-synth directory, run:
```
mvn package
```

The compiled JAR file will be located at:
```
target/synth-1.0.jar
```

# Execution
Run the synthesizer with:
```
java -cp lib:target/synth-1.0.jar synth.Main <path-to-examples-file>
```
On Windows, replace : with ; in the classpath.

# Contributing
If you want to contribute to this project, please fork this repository and create a pull request, or drop me an email at kha112@sfu.ca
