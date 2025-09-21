# CS 4240 Compilers and Interpreters (Spring 2025) Project 1

# Tiger-IR Optimizer (CS 4240 Project 1)

This project was completed as part of **CS 4240: Compilers & Interpreters** at Georgia Tech. Note that much of the codebase was created by those who help run the course.
The goal was to build a **middle-end optimizer** for the Tiger-IR intermediate representation, targeting the MIPS32 architecture.

---

## Overview

- **Language:** Tiger (pedagogic language) → Tiger-IR → Optimized IR  
- **Focus:** Static analysis & optimization of IR code  
- **Implemented Optimization:** Dead code elimination (with reaching definitions)  
- **Performance Metric:** Reduced *dynamic instruction count* while preserving program correctness  

---

## Features

- Reads Tiger-IR text input  
- Performs static analysis & optimization passes  
- Outputs optimized Tiger-IR  
- Includes `build.sh` and `run.sh` scripts for compilation & execution  

---

### Deliverables

- design.pdf — Architecture & design choices
- build.sh / run.sh — Scripts for building & running
- Source code implementing the optimizer

---

### Evaluation

- Public & hidden test cases validated against instructor-provided IR interpreter
- Optimized programs achieved lower dynamic instruction counts compared to baseline implementations

---

### Skills & Concepts

- Compiler middle-end design
- Static analysis & reaching definitions
- Dead code elimination
- Intermediate representations (IR)
- Build automation

### Build

To build the project, run the `build.sh` script from the root directory:

```sh
./build.sh
```

### Run

After building, run the application using the `run.sh` script. You must provide two arguments.

**Usage:**

```sh
./run.sh <arg1> <arg2>
```

`<arg1>`: The input IR program to optimize.
`<arg2>`: The output file to store optimized IR program.