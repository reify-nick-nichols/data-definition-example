# data-definition-example

An illustration of the differences between spec, spec-tools, and malli.

## Motivation

Our programs' behaviors are driven by the data they receive.
As a naive example, consider addition.
The behavior of addition changes based on the positivity, negativity, and relative magnitude of the inputs:

- Two positive integers added together will produce a positive integer of greater magnitude.
- Two negative integers added together will produce a negative integer of greater magnitude.
- Adding any number and zero produces the original number
- And so on and so forth

Unlike other languages, clojure has not overloaded the `+` operator to work as a string concatenator.
Addition is only valid for numbers; if we try to add a number and a string, the behavior is undefined and the program will throw an exception.

```clojure
clj꞉sheep.spec-tools꞉> 
(+ 1 "not a number")
; Execution error (ClassCastException) at sheep.spec-tools/eval11157 (form-init10988348665744625087.clj:528).
; class java.lang.String cannot be cast to class java.lang.Number (java.lang.String and java.lang.Number are in module java.base of loader 'bootstrap')
```

In the course of normal processing, exceptions are generally unexpected.
Exceptions mean that our programs have entered a state we have not designed them to handle, and can stem from multiple sources:

- Bad, incomplete, or malformed data entering the system (e.g. a map with a missing key)
- Failures external to our program (e.g. a file system unable to resolve a directory)
- Corner-cases unhandled by our software (e.g. race conditions)

For this reason, most programming languages have a mechanism for inspecting the data we can operate against.
In Object-Oriented languages, we often use the constructor and checks within methods to extend the type system to handle more complex data.
In functional languages, we often write predicates to inspect data; however, this could become tedious without a robust way to define the data we can operate against.

## Problem Statement

As we consider more complex data structures and more complex rules around what is and is not valid, we need a mechanism to programatically encode, describe, and check the data we might operate on.
This mechanism should be robust enough to handle the common needs of modern systems and extensible to handle future needs.

In Clojure, there are three primary libraries used to handle these needs:

- [clojure.spec](https://clojure.org/guides/spec)
- [metosin/spec-tools](https://github.com/metosin/spec-tools)
- [metosin/malli](https://github.com/metosin/malli)

In this overview, we'll cover each tool incrememntally to discuss what is and is not solved by each.

## Sample Problem

Imagine we're writing software to track herds of sheep.
Each sheep has a few attributes we want to track:

- Name: The name we've given our fluffy friend (e.g. "Dolly")
- Age: The sheep's age in whole years starting at 1
- Weight: How much the sheep weighs as of the last time we checked
- Shorn?: Wether or not our sheep has recently been shorn for wool.

We can easily imagine how this might we encoded as a sequence of maps.

```clojure
[{:name   "Dolly"
  :age    3
  :weight {:amount 74.85
           :unit   :kg}
  :shorn? false}
 {:name   "Derby"
  :age    2
  :weight {:amount 82.1
           :unit   :kg}
  :shorn? true}
 {:name   "Dale"
  :age    4
  :weight {:amount 165.21
           :unit   :lb}
  :shorn? false}]
```

Throughout this demo, we'll use this data structure and the test data available in [`sheep/data`](src/sheep/data.clj).
We'll compare how each library encodes these data requirements and what other functionality it provides.

### Spec

`clojure.spec` is a core library to clojure- meaning it is bundled with the language, but as a separate library.
Spec was the first library designed to solve this class of problem, and has become a staple of the community since the language's inception.
The library's primary goal is providing a means of describing data while providing enough extensibility to integrate into other solutions.
For this reason, it is generally ubiquitous; however, it does come with some limitations.

A sample spec and basic uses are available in [`sheep/spec_tools`](src/sheep/spec_tools.clj).

#### Spec Pros

- As a core library, `clojure.spec` incurs no additional dependencies
- As a core library, `clojure.spec` is generally supported by other libraries which may interact with or require data definitions

#### Spec Cons

- `clojure.spec` is very much a collection of basic tools, deriving additional value requires work
  - No built-in syntax for type coercion, parsing
  - No built-in syntax for metadata / documentation
- `clojure.spec/explain` and `clojure.spec/explain-data` leak the incoming data that could not be coerced
  - Requires manual redaction for PII, PHI, etc
- `clojure.spec/explain` and `clojure.spec/explain-data` leak the function names failing coercion
  - May require redaction depending on needs, function names, etc.
- `clojure.spec` relies on a global registry- a solution pattern which has generally fallen out of favor.

### Spec Tools

Metosin developed `spec-tools` to address many of the common problems and challenges developers encountered while using `clojure.spec`.
The biggest additions were:

- A more robust and extensivle interface that allowed structured data to be transformed into specs
- Built-in tools to handle common needs
  - Coercing data from strings/json into clojure types and datastructures
  - Generating common data definition formats
    - OpenAPI
    - Swagger
    - JSON Schema
- A way to associate documentation and examples to spec definitions

This was built using a macro to compile maps down into `clojure.spec` definitions- providing clean interop into tools that could consume specs.

#### Spec Tools Pros

- The compiled specs are natively compatible with `clojure.spec`, meaning migration is simple
- There are clean tools for coercing data types out of common wire formats
- There is a clean and composable way to leverage specs for common data definition formats
- The code defining a spec is extensible, meaning future, backwards-compatible additions may be made

#### Spec Tools Cons

- `spec-tools` relies upon `clojure.spec`, so many of its drawbacks are inherited.
  - `clojure.spec/explain` and `clojure.spec/explain-data` leak the incoming data that could not be coerced
    - Requires manual redaction for PII, PHI, etc
  - `clojure.spec/explain` and `clojure.spec/explain-data` leak the function names failing coercion
    - May require redaction depending on needs, function names, etc.
  - `clojure.spec` relies on a global registry- a solution pattern which has generally fallen out of favor.
- Metosin has focused more effort on developing `malli` than expanding `spec-tools`
- Metosin documentation can be sparse and difficult to navigate
  - For example, digging into the `:type` key requires reading some source code

### Malli

Metosin eventually created their own library for creating data definitions.
This stemmed from multiple needs and pressures:

- Difficulty maintaining and expanding `spec-tools`
- The global registry of `clojure.spec`
- The underlying macros of `clojure.spec` lock a lot of implementation choices to compile time rather than run time.

During development, they also changed the syntax significantly- with data definitions looking much more like hiccup.
Additionally, registries can definied by users, with a sane default, allowing them to be dynamically loaded.

A sample malli definition and basic uses are available in [`sheep/malli`](src/sheep/malli.clj).

#### Malli Pros

- There are clean tools for coercing data types out of common wire formats
- There is a clean and composable way to leverage specs for common data definition formats
- The code defining a spec is extensible, meaning future, backwards-compatible additions may be made
- The registry is defined by a protocol, so it may be replaced and substituted at runtime
- The data definitions are plain clojure data structures, so they can be operated on with normal clojure functions
- Malli can do some schema inference from sampled data, making initial development quicker
- Some devtool integration with kondo/clojure-lsp, but very beta

#### Malli Cons

- Malli definitions aren't drop-in replacements for clojure specs
- Libraries leveraging external data definitions must be extended to use Malli definitions in place of clojure specs
- Metosin documentation can be sparse and difficult to navigate

### Common Pros and Cons

- When defining maps, all three libraries have a syntax for optional/required keys. This conflates what data may be present in a map with what may be required for a given different sets of functionality. The second iteration of spec plans to address this, but is years away.
- Each library allows for easy composability of validation functions
- 100% opt-in instrumentation of functions. Can be enabled for dev/testing and disabled for production.
- Out-of-the-box validation middleware for Compojure/Reitit provide adapters for all three implementations

## Copyright

Copyright © 2022 [Nick A Nichols](https://nnichols.github.io/)

MIT License provided in full text within the LICENSE file.
