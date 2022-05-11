(ns sheep.data
  (:require [cheshire.core :as json]))

(def dolly
  {:name   "Dolly"
   :age    3
   :weight {:amount 74.85
            :unit   :kg}
   :shorn? false})

(def derby
  {:name   "Derby"
   :age    2
   :weight {:amount 82.1
            :unit   :kg}
   :shorn? true})

(def dale
  {:name   "Dale"
   :age    4
   :weight {:amount 165.21
            :unit   :lb}
   :shorn? false})

(def herd
  [dolly derby dale])

(def herd-as-json
  (json/generate-string herd))

(def round-tripped-herd
  (-> herd
      json/generate-string
      (json/parse-string true)))

(def donny
  {:name   "Donny"
   :age    5
   :shorn? false})

(def new-herd
  (conj herd donny))

