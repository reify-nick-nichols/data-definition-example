(ns sheep.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [sheep.data :as sheep]))

(s/def ::name
       (s/and string? #(not (str/blank? %))))

(s/def ::age
       (s/and int? pos?))

(s/def ::amount
       (s/and number? pos?))

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
  )
