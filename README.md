# Ten pin scoring system
Scoring for the classical game of ten pin
<!-- toc -->

- [Overview](#overview)
- [System Requirements](#system-requirements)
- [Functionality](#functionality)
- [Implementation Details And Todos](#implementation-details-and-todos)
- [Unit Test](#unit-test)
<!-- tocstop -->

## Overview
- Given a list of scores:
  - Validates the input scores
  - Builds the score card

## System Requirements
- jvm
- clojure

## Functionality
- `compute-score-card` is the main function. Accepts a list of scores
  - it validates a list of scores and takes care of cases like:
    - Too many scores in the input list
    - Extra turn when the last one is a strike or spare
    - Invalid score combination (i.e > 10) for a given turn
  - For an input like `[1 9 2 4 10 10 10 7 1 2 8 7 0 0 0 10 1 9]` it gives an output as:
    ```
     {:turns
     ({:first-pins 1,
       :second-pins 9,
       :curr-turn-score 12,
       :score 12,
       :turn-type :spare}
      {:first-pins 2,
       :second-pins 4,
       :curr-turn-score 6,
       :score 18,
       :turn-type :open}
      {:first-pins 10,
       :second-pins nil,
       :curr-turn-score 30,
       :score 48,
       :turn-type :strike}
      {:first-pins 10,
       :second-pins nil,
       :curr-turn-score 27,
       :score 75,
       :turn-type :strike}
      {:first-pins 10,
       :second-pins nil,
       :curr-turn-score 18,
       :score 93,
       :turn-type :strike}
      {:first-pins 7,
       :second-pins 1,
       :curr-turn-score 8,
       :score 101,
       :turn-type :open}
      {:first-pins 2,
       :second-pins 8,
       :curr-turn-score 17,
       :score 118,
       :turn-type :spare}
      {:first-pins 7,
       :second-pins 0,
       :curr-turn-score 7,
       :score 125,
       :turn-type :open}
      {:first-pins 0,
       :second-pins 0,
       :curr-turn-score 0,
       :score 125,
       :turn-type :open}
      {:first-pins 10,
       :second-pins 1,
       :curr-turn-score 20,
       :score 145,
       :turn-type :strike,
       :third-pins 9}),
     :status :over}
      ```
     where `turns` gives the elaborate score card and `status` denotes of game has started/ on-going/ over
     and `third-pins` only appear in the last turn score if it is a strike or spare


## Implementation Details and Todos
- The function signature accepts a list of scores so frontend has to store minimum data as game state
- Used minimum clojure features
- Used recursion where possible
- Would like to improve the validation function to precisely say why the validation failed
- Would definitely like to refactor the code to reduce procedural conditions and make more functional

## Unit Test
```
lein test
```

