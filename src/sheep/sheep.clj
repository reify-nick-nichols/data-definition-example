(ns sheep.sheep)

(defn sheep->kg
  "Given a `sheep`, return its weight in kilograms.

   If the weight is stored in pounds, convert it first."
  [sheep]
  (if (= :kg (get-in sheep [:weight :unit]))
    (get-in sheep [:weight :amount])
    (* 2.20462 (get-in sheep [:weight :amount]))))