package com.mdwriter.app;

public class ProposalTemplate {
    public static String getDefaultTemplate() {
        return """
---
title: Blood Bank Management System
subject: Project - IV
code: BIT256CO
program: Bachelor of Information Technology
semester: IV
year: 2024
supervisor: Rubim Shrestha
hod: Saroj Pandey
students:
  - Anish Kumar Goit (320886)
  - Kusal Rimal (320900)
  - Dinesh Rimal (320894)
abstract: The Blood Bank Management System is intended to meet the requirements of administrators and donors by offering a stable platform for maintaining blood donation records. This Java programming project is aimed for intermediate Java developers who want to improve their coding abilities while supporting a vital component of the healthcare system.
abbreviations:
  - "RDBMS: Relational Database Management System"
  - "IDE: Integrated Development Environment"
  - "DFD: Data Flow Diagram"
  - "SDLC: Software Development Life Cycle"
---

[TOC]

# Chapter 1: Introduction
## 1.1 Background
Enter background here...

## 1.2 Problem Statement
Describe the problem...

## 1.3 Objectives
### 1.3.1 General Objectives
*   To develop...

### 1.3.2 Specific Objectives
*   To implement...

## 1.4 Scope and Limitation
...

# Chapter 2: Literature Review
## 2.1 Existing System Analysis
...

## 2.2 Proposed System
...

# Chapter 3: Methodology
## 3.1 Software Development Life Cycle
...

## 3.2 System Design
### 3.2.1 Use Case Diagram
![Use Case](https://via.placeholder.com/600x400 "Use Case Diagram")

### 3.2.2 Data Flow Diagram (DFD)
![DFD](https://via.placeholder.com/600x400 "Data Flow Diagram")

# Chapter 4: Expected Output
...

# References
*   Reference 1
*   Reference 2
""";
    }
}
