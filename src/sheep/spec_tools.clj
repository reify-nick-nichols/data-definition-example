(ns sheep.spec-tools
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]
            [sheep.data :as sheep]
            [spec-tools.core :as st]
            [spec-tools.swagger.core :as swagger]))

(s/def ::name
  (st/spec {:type                :string
            :spec                (s/and string? #(not (str/blank? %)))
            :description         "The name of our fluffy friend"
            :json-schema/example "Dolly"
            :swagger/example     "Dale"}))

(s/def ::age
  (st/spec {:type                :long
            :spec                (s/and int? pos?)
            :description         "The age of the sheep in years, rounded up to the next positive integer"
            :reason              "Sheep ages must be positive ints"
            :json-schema/example 1}))

(s/def ::amount
  (st/spec {:type                :double
            :spec                (s/and number? pos?)
            :description         "The numeric value of a sheep's weight"
            :json-schema/example 75.2}))

(s/def ::unit
  (st/spec {:type                :keyword
            :spec                #{:kg :lb}
            :description         "The unit of measure for the sheep's weight. Allowed values ar `:lb` and `:kg`"
            :json-schema/example :kg}))

(s/def ::weight
  (st/spec {:type        :map
            :description "The amount and unit of measure for a sheep's weight"
            :spec        (s/keys :req-un [::amount ::unit])}))

(s/def ::shorn?
  (st/spec {:type        :boolean
            :description "Wether or not the sheep has been shorn recently. If true, it needs time to regrow its fluff before we sheer it again"
            :spec        boolean?}))

(s/def ::sheep
  (st/spec {:type        :map
            :description "A map of all the data about a sheep"
            :spec        (s/keys :req-un [::name ::age ::weight ::shorn?])}))

(s/def ::herd
  (st/spec {:type        :vector
            :description "A collection of sheep"
            :spec        (s/coll-of ::sheep)}))

(def strict-json-transformer
  (st/type-transformer
   st/json-transformer
   st/strip-extra-keys-transformer
   st/strip-extra-values-transformer))


(comment
  ;; Basic validation of data structures
  (s/valid? ::sheep sheep/dolly)
  (s/valid? ::sheep (dissoc sheep/dolly :name))
  (s/valid? ::sheep (assoc sheep/dolly :bad-data "random noise"))
  (s/valid? ::herd sheep/herd)
  (s/valid? ::herd [])

  ;; Understanding why data doesn't match a spec - including custom :reasons
  (s/explain ::herd [{} (assoc sheep/derby :age "three")])
  (s/explain-data ::herd [{} (assoc sheep/derby :age "three")])

  ;; Conformance / Corecion
  (st/coerce ::sheep (assoc sheep/dolly :bad-data "random noise") st/json-transformer)
  (st/coerce ::sheep (assoc sheep/dolly :bad-data "random noise") strict-json-transformer)
  (st/coerce ::herd sheep/round-tripped-herd st/json-transformer)
  (st/coerce ::herd sheep/herd-as-json st/json-transformer)

  ;; Generate arbitrary conforming examples
  (gen/generate (s/gen ::sheep))
  (gen/generate (s/gen ::herd))
  
  ;; Swagger generation from specs (Also works for OpenAPI and JSON Schema)
  (swagger/transform ::sheep)
  )
