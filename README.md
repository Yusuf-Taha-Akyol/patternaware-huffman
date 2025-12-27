# Pattern-Aware Huffman Compressor (PWHA)

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Status](https://img.shields.io/badge/Implementation-From_Scratch-green.svg)

> **A custom data compression algorithm implementation that combines traditional Huffman Coding with Context-Aware Pattern Mining.**

This project demonstrates a deep understanding of data structures and algorithms by implementing core components (Priority Queue, Bitwise I/O, Tree Traversal) completely **from scratch**, without relying on high-level Java collections or compression libraries.

---

## 📖 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture & Algorithms](#-architecture--algorithms)
- [Project Structure](#-project-structure)
- [Performance & Results](#-performance--results)
- [Getting Started](#-getting-started)
- [Screenshots](#-screenshots)

---

## 💡 Overview

Standard Huffman coding compresses data based on character frequency. **Pattern-Aware Huffman (PWHA)** takes this a step further by identifying repeating **byte patterns** (substrings) within specific **contexts** (starting characters).

It constructs a **Two-Layered Tree Structure**:
1.  **Super-Tree:** Represents the frequency of "Contexts" (starting characters).
2.  **Sub-Trees:** Each context has its own Huffman Tree optimized for patterns that frequently follow it.

This approach allows for higher compression ratios on text-heavy data by encoding entire words or frequent syllables as single tokens.

---

## 🚀 Key Features

### 🛠️ Built From Scratch (No Shortcuts)
To demonstrate algorithmic proficiency, standard libraries were replaced with custom implementations:
* **Custom Priority Queue:** A generic Min-Heap implementation with dynamic resizing and `O(log n)` complexity.
* **Bitwise I/O:** `BitWriter` and `BitReader` classes handling bit-level manipulation (`<<`, `|`, `&`) to write variable-length codes.
* **Serialization:** Custom serialization logic for storing the dictionary header.

### 🧠 Algorithmic Innovations
* **Context-Aware Greedy Matching:** The encoder uses a greedy strategy to find the longest matching pattern in the dictionary for optimal compression.
* **Dynamic Context Switching:** The decoder dynamically switches between Huffman trees based on the previously decoded symbol.
* **Memory Optimization (Space-Saving):** Implements an eviction policy using **Randomized Sampling** to keep the frequency map size constant (`O(1)` eviction) and prevent memory overflow.

---

## 🏗 Architecture & Algorithms

### 1. The Two-Layered Model
Instead of a single giant Huffman tree, PWHA uses a hierarchical model:
* **Layer 1 (Contexts):** Handles the first letter of words (e.g., 't', 'a').
* **Layer 2 (Patterns):** Handles what comes *after* the context (e.g., for context 't', patterns might be "he", "ion", "hat").

### 2. Compression Pipeline
1.  **Analysis Phase:**
    * Reads the file byte-by-byte.
    * Mines patterns using a sliding window technique.
    * Populates `FrequencyMap` with contexts and patterns.
2.  **Tree Construction:**
    * Builds `Sub-Trees` for every context using the Custom Priority Queue.
    * Builds the `Super-Tree` connecting all contexts.
3.  **Encoding (Greedy Strategy):**
    * Writes the Context code.
    * Scans the remaining word and eagerly matches the *longest* available pattern in the sub-tree.

### 3. Decompression Pipeline
* Reads the serialized header to reconstruct the exact tree structure in memory.
* Reads the bit stream and traverses the `Super-Tree` or `Sub-Tree` depending on the current state.

---

## 📂 Project Structure

```bash
src/main/java/com/pwha/
├── core/           # Core Algorithm Logic
│   └── HuffmanStructure.java  # Tree building logic
├── engine/         # Compression Engine
│   ├── Encoder.java           # Greedy matching & writing
│   └── Decoder.java           # Tree traversal & state machine
├── io/             # Low-Level I/O
│   ├── BitWriter.java         # Bit packing
│   ├── BitReader.java         # Bit unpacking
│   └── ByteReader.java        # Word segmentation
├── model/          # Data Models
│   ├── node/                  # Tree Nodes (ContextLeaf, SimpleLeaf, InternalNode)
│   └── ByteArrayWrapper.java  # Byte array handling
├── service/        # Business Logic
│   └── FrequencyService.java  # Pattern mining & Analysis
├── util/           # Utilities
│   └── CustomPriorityQueue.java # FROM SCRATCH Heap Implementation
└── gui/            # User Interface
    ├── App.java               # Main Swing Application
    └── HuffmanTreePainter.java # Tree Visualization Component

---

## 📊 Performance & Benchmarks

The following benchmarks demonstrate the efficiency of the **Pattern-Aware Huffman (PWHA)** algorithm compared to **Classic Huffman Coding**. Tests were conducted on text-heavy datasets ranging from **10 MB to 2.5 GB**.

### 1. Scalability Test (PWHA Performance)
This table shows how the Pattern-Aware algorithm handles increasing file sizes.

| Input File Size | Compressed Size | Efficiency (Space Saved) | Time Taken |
|----------------:|----------------:|--------------------------|------------|
| **10.1 MB** | 5.7 MB          | 42.84%                   | 1.81 sec   |
| **50.0 MB** | 22.3 MB         | 55.41%                   | 6.95 sec   |
| **100.0 MB** | 43.0 MB         | 57.01%                   | 12.88 sec  |
| **250.0 MB** | 105.5 MB        | 57.83%                   | 31.94 sec  |
| **500.0 MB** | 209.2 MB        | 58.15%                   | 63.69 sec  |
| **1.0 GB** | 426.8 MB        | 58.32%                   | 131.68 sec |
| **2.5 GB** | 1.0 GB          | **58.43%** | 326.44 sec |

> **Observation:** The efficiency increases as the file size grows, stabilizing around **58%**. This proves that the "Context-Aware" approach becomes more effective when there are more repeating patterns in larger datasets.

---

### 2. Comparative Analysis: PWHA vs. Classic Huffman
Here we compare the final compressed sizes. Lower is better.

| Dataset Size | Classic Huffman (Size) | **PWHA (Size)** | Result |
|--------------|------------------------|-----------------|--------|
| 10 MB        | **5.32 MB** | 5.70 MB         | Classic wins (+0.38 MB) |
| 50 MB        | 26.45 MB               | **22.30 MB** | **PWHA wins (-4.15 MB)** |
| 100 MB       | 52.89 MB               | **43.00 MB** | **PWHA wins (-9.89 MB)** |
| 250 MB       | 132.25 MB              | **105.50 MB** | **PWHA wins (-26.75 MB)** |
| 500 MB       | ~240.00 MB             | **209.20 MB** | **PWHA wins (~30.8 MB)** |

--

## 📸 Screenshots

### 🖥️ Application Interface
The application features a user-friendly Swing interface for configuring algorithms and tracking performance.

| **Initial State** | **Processing & Results** |
|:---:|:---:|
| ![Main GUI](image/main-gui.png)<br>_Clean interface ready for file selection and configuration_ | ![Results](image/compression-result.png)<br>_Real-time logs showing compression ratio, time, and statistics_ |

---

### 🌳 Two-Layered Tree Visualization
A custom-built graph visualizer demonstrates the **Context-Aware** architecture. Users can navigate from the Super-Tree down to specific Pattern Sub-Trees.

| **Layer 1: Super-Tree (Contexts)** | **Layer 2: Sub-Tree (Context 'x')** |
|:---:|:---:|
| ![Super Tree](image/super-tree-view.png)<br>_The global tree organizing "Context" characters (e.g., first letters)_ | ![Sub Tree](image/x-sub-tree-view.png)<br>_The specialized Huffman tree for patterns belonging specifically to context 'x'_ |
