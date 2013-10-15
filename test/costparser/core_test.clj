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
(expect -500 (:test (hashFromStr ["-500" "test"])))
(expect 250 (:test (hashFromStr ["+250" "test"])))

;; Test parsing function
(expect -220 (:test (last (first (parseLines ["--- 02.10.2013" "-220 : test"])))))
(expect true (= (mytime "02.10.2013") (first (first (parseLines ["--- 02.10.2013" "-220 : test"])))))



