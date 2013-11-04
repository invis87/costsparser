(ns costparser.core-test
  (:require [expectations :refer :all]
            [costparser.core :refer :all]
            [clj-time.core :as tCore]))

;; Compare times
(expect true (tCore/after? (mytime "11.11.2013") (mytime "11.10.2013")))
(expect true (tCore/after? (mytime "12.10.2013") (mytime "11.10.2013")))
(expect true (tCore/after? (mytime "11.10.2014") (mytime "11.10.2013")))
(expect false (tCore/after? (mytime "11.10.2013") (mytime "11.10.2013")))

;; Test string parsing function
(expect -500 (:amount (hashFromStr "-500 : test")))
(expect 250 (:amount (hashFromStr "+250 : test")))

;; Test parsing function
(def testStr "--- 02.10.2013
  -220 : test")
(expect -220 (:amount (first (costs (.split testStr "\n")))))






