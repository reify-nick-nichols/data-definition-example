(ns sheep.malli
  (:require [malli.core :as m]
            [malli.dev.pretty :as pretty]
            [malli.error :as me]
            [malli.generator :as mg]
            [malli.instrument :as mi]
            [malli.json-schema :as json-schema]
            [malli.provider :as mp]
            [malli.transform :as mt]
            [sheep.data :as sheep]))

(def sheep
  [:map
   [:name
    [:string {:min                 1
              :description         "The name of our fluffy friend"
              :error/message       "Must be a non-empty string"
              :json-schema/example "Dolly"}]]
   [:age
    [:and
     {:description         "The age of the sheep in years, rounded up to the next positive integer"
      :error/message       "Sheep ages must be positive ints"
      :json-schema/example 5}
     :int [:> 0]]]
   [:weight
    {:description "The amount and unit of measure for a sheep's weight"}
    [:map
     [:amount
      [:and
       {:description         "The numeric value of a sheep's weight"
        :json-schema/example 23.5
        :gen/infinite?       false
        :gen/NaN?            false}
       :double [:> 0]]]
     [:unit
      [:enum
       {:description "The unit of measure for the sheep's weight. Allowed values ar `:lb` and `:kg`"}
       :lb
       :kg]]]]
   [:shorn?
    [:boolean
     {:description "Wether or not the sheep has been shorn recently. If true, it needs time to regrow its fluff before we sheer it again"}]]])

(def herd
  [:sequential {:gen/min 1 :gen/max 25} sheep])

(comment

  ;; Basic validation of data structures
  (m/validate sheep sheep/dolly)
  (m/validate sheep (dissoc sheep/dolly :name))
  (m/validate sheep (assoc sheep/dolly :bad-data "random noise"))
  (m/validate herd sheep/herd)
  (m/validate herd [])

  ;; Understanding why data doesn't match a spec - including custom errors
  (m/explain sheep (assoc sheep/dolly :name 1))
  (-> sheep
      (m/explain (assoc sheep/dolly :name 1))
      me/humanize)
  (-> herd
      (m/explain [{} (assoc sheep/derby :age "three")])
      me/humanize)

  ;; Conformance / Coercion
  (m/decode sheep sheep/dolly mt/string-transformer)
  (m/encode sheep sheep/dolly mt/string-transformer)
  (m/decode sheep sheep/round-tripped-herd mt/json-transformer)

  ;; Generate arbitrary conforming examples
  (mg/generate sheep)
  (mg/generate sheep {:seed 12345})
  (mg/generate herd)
  (mg/generate herd {:size 3})

  ;; Schema inference from sample data
  (mp/provide sheep/herd)

  ;; Loading from the default registry
  (m/validate sheep sheep/dolly {:registry m/default-registry})

  ;; JSON Schema generation from malli definitions
  (json-schema/transform sheep)

  ;; Function Specs
  (defn sheep->kg
    [sheep*]
    (if (= :kg (get-in sheep* [:weight :unit]))
      (get-in sheep* [:weight :amount])
      (* 2.20462 (get-in sheep* [:weight :amount]))))

  (defn sheep->kg*
    [sheep*]
    (if (m/validate sheep sheep*)
      (sheep->kg sheep*)
      (throw (AssertionError. "You didn't give me a sheep!"))))

  (sheep->kg 1)
  (sheep->kg
    {:name 1 :age 2 :weight {:amount 82.1 :unit :kg} :shorn? true})
  (sheep->kg sheep/derby)

  (def =>sheep->kg
    (m/schema [:=> [:cat sheep] number?]))

  (m/=> sheep->kg =>sheep->kg)
  (mi/instrument! {:report (pretty/reporter)}))
