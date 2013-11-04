(ns costparser.core
  (:require [clojure.java.io :as io :only (file readre)]
            [clj-time.format :as tFormat :only (formatter parse)]
            [clj-time.core :as tCore]
            [incanter.core :as incanter]
            [incanter.stats :as stats]
            [incanter.charts :as charts]))

(def costsFile "path to your costs file")

(def tSep "---")
(def df (tFormat/formatter "dd.MM.yyyy"))
(defn mytime [str] (tFormat/parse df str))


(defn hashFromStr [str]
  (let [type-price (map #(.trim %) (.split str ":"))]
    {:type (keyword (second type-price)) :amount (Integer/parseInt (first type-price))}))

(defn- parseTimeStr [timeStr]
  (mytime (.trim (.substring timeStr (count tSep)))))

(defn- parseCostLine [date costStr]
  (assoc (hashFromStr costStr) :when date))

(defn- openTimeArr [timeCostsArray]
  (let [time (first timeCostsArray)
        costs (second timeCostsArray)]
    (map #(parseCostLine time %) costs)))

(defn- intoTimeCostsArray [lineSeq]
  (reduce (fn [result line]
            (if (.startsWith line tSep)
              (conj result [(parseTimeStr line) []])
              (let [[date costs] (peek result)]
                (conj (pop result) [date (conj costs line)]))))
          [] lineSeq))

(defn costs [lineSeq] (apply concat (map openTimeArr
    (intoTimeCostsArray lineSeq))))

(defn costsForFile [path]
  (with-open [in (io/reader (io/file path))]
    (costs (line-seq in))))

(defn costsFor
  "Compute costs for costsFile filename"
  ([key] (filter #(zero? (compare (:type %) key))
          (costsForFile costsFile)))
  ([key after] (filter #(tCore/after? (:when %) (mytime after)) (costsFor key)))
  ([key after before] (filter #(tCore/before? (:when %) (mytime before)) (costsFor key after))))

(defn costsForAll [] (costsForFile costsFile))

(defn amount [arr]
  (apply + (map #(:amount %) arr)))

(defn groupByFunc [costs func]
  (apply merge
         (map (fn [groupElement] (hash-map (first groupElement) (amount (second groupElement))))
              (group-by func costs))))

(defn barChart [costs func]
 (let [groups (groupByFunc costs func)]
  (incanter/view (charts/bar-chart (keys groups) (map #(* -1 %) (vals groups))))))

(barChart (costsForAll) #(tCore/month (:when %)))
