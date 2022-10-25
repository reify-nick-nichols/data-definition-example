(ns sheep.malli-test
  (:require [malli.generator :as genr]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :as check.test]
            [clojure.test.check.properties :as prop]
            [sheep.data :as data]
            [sheep.sheep :as sheep]
            [sheep.malli :as sut]))

(deftest sheep->kg-test
  (testing "Demonstrative tests against static values"
    (is (= 364.2252702 (sheep/sheep->kg data/dale)))
    (is (= 82.1 (sheep/sheep->kg data/derby)))
    (is (= 74.85 (sheep/sheep->kg data/dolly))))
  (testing "Generative tests"
    (let [generated-sheep (genr/generate sut/sheep)
          weight          (get-in generated-sheep [:weight :amount])
          weight-in-kg    (sheep/sheep->kg generated-sheep)]
      (is (<= weight weight-in-kg)))))

;; Our spec defines weights as positive numbers, so the value should be positive
(check.test/defspec sheep->kg-is-positive 1000
  (prop/for-all [test-sheep (genr/generator sut/sheep)]
                (pos? (sheep/sheep->kg test-sheep))))

;; If the weight was in kilos, converting it wouldn't change the value
(check.test/defspec sheep->kg-identity-for-kilos 1000
  (prop/for-all [test-sheep (genr/generator sut/sheep)]
                (let [sheep* (assoc-in test-sheep [:weight :unit] :kg)
                      sheep-weight (get-in test-sheep [:weight :amount])]
                  (= sheep-weight (sheep/sheep->kg sheep*)))))

;; If the weight was in pounds, the new weight value is greater
(check.test/defspec sheep->kg-larger-for-pounds 1000
  (prop/for-all [test-sheep (genr/generator sut/sheep)]
                (let [sheep*       (assoc-in test-sheep [:weight :unit] :lb)
                      sheep-weight (get-in test-sheep [:weight :amount])]
                  (< sheep-weight (sheep/sheep->kg sheep*)))))