(ns sheep.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as stest]
            [clojure.string :as str]
            [clojure.test.check.generators :as generators]
            [sheep.data :as sheep]))

(s/def ::name
       (s/and string? #(not (str/blank? %))))

(s/def ::age
       (s/and int? pos?))

(s/def ::amount
       (s/with-gen
         (s/and number? pos?)
        #(s/gen (generators/double* {:infinite? false :NaN? false :min 0}))))

(s/def ::unit #{:kg :lb})

(s/def ::weight
       (s/keys :req-un [::amount ::unit]))

(s/def ::shorn? boolean?)

(s/def ::sheep
       (s/keys :req-un [::name ::age ::weight ::shorn?]))

(s/def ::herd (s/coll-of ::sheep))

(comment
  ;; Basic validation of data structures
  (s/valid? ::sheep sheep/dolly)
  (s/valid? ::sheep (dissoc sheep/dolly :name))
  (s/valid? ::sheep (assoc sheep/dolly :bad-data "random noise"))
  (s/valid? ::herd sheep/herd)
  (s/valid? ::herd [])

  ;; Understanding why data doesn't match a spec
  (s/explain ::herd [{} (assoc sheep/derby :name 1)])
  (s/explain-data ::herd [{} (assoc sheep/derby :name 1)])

  ;; Conformance
  (s/conform ::sheep (assoc sheep/dolly :bad-data "random noise"))
  (s/conform ::herd sheep/round-tripped-herd)

  ;; Generate arbitrary conforming examples
  (gen/generate (s/gen ::sheep))
  (gen/generate (s/gen ::herd))

  ;; Global Registry
  (s/registry)

  ;; Function Specs
  (defn sheep->kg
    [sheep]
    (if (= :kg (get-in sheep [:weight :unit]))
      (get-in sheep [:weight :amount])
      (* 2.20462 (get-in sheep [:weight :amount]))))

  (defn sheep->kg*
    [sheep]
    (if (s/valid? ::sheep sheep)
      (sheep->kg sheep)
      (throw (AssertionError. "You didn't give me a sheep!"))))

  (s/fdef sheep->kg
    :args (s/cat :sheep ::sheep)
    :ret ::amount)

  (sheep->kg 1)
  (sheep->kg sheep/derby)
  (stest/check `sheep->kg)
  (stest/instrument `sheep->kg))
