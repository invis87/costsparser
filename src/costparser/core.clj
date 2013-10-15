(ns costparser.core
  (:require [clojure.java.io :as io :only (file readre)]
            [clj-time.format :as tFormat :only (formatter parse)]
            [clj-time.core :as tCore]
            [clojure.walk :refer :all]))

(def costsFile "path to your costs file")

(def tSep "---")
(def df (tFormat/formatter "dd.MM.yyyy"))
(defn mytime [str] (tFormat/parse df str))

(defrecord cost [when amount])

(defn hashFromStr [array]
  (keywordize-keys (apply hash-map
                          (update-in (into [] (reverse (map #(.trim %) array)))
                                     [1] #(Integer/parseInt %)))))

(defn parseLines [in]
  (reduce (fn [result line]
      (if (.startsWith line tSep)
        (let [d (mytime (.trim (.substring line 3)))]
          (conj result [d {}]))
          (let [pi (hashFromStr (.split line ":"))
            [date pis] (peek result)]
            (conj (pop result)
              [date
              (conj pis pi)]))))
    []  in))

(defn pricesDatWithReduce
  "read prices data from a dat file, using reduce which is amazing"
  [path]
  (with-open [in (io/reader (io/file path))]
    (parseLines (line-seq in))
    ))

(defn amount
  ([arr] (apply + (map #(:amount %) arr)))
  ([arr after]
   (amount (filter #(tCore/after? (:when %) (mytime after)) arr)))
  ([arr after before]
   (amount (filter #(tCore/before? (:when %) (mytime before)) arr) after))
  )

(defn costsFor [key] (reduce (fn [result item]
                         (if-not (nil? (key (last item)))
                           (let [when (first item)
                                 amount (key (last item))]
                             (conj result (cost. when amount)))
                           result))
                       [] (pricesDatWithReduce costsFile)))

(defn allTypes [] (reduce (fn [result item]
                           (let [when (first item)]
                             (into [] (concat result
                               (reduce (fn [res it]
                                 (conj res (cost. when it)))
                                   [] (vals (second item)))
                           ))))
                       [] (pricesDatWithReduce costsFile)))
