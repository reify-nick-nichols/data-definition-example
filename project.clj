(defproject sheep "0.0.0"
  :description "An illustration of the differences between spec, spec-tools, and malli"
  :url "https://github.com/reify-nick-nichols/data-definition-example"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[cheshire "5.10.2"]
                 [metosin/malli "0.8.4"]
                 [metosin/spec-tools "0.10.5"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/test.check "0.9.0"]]
  :target-path "target/%s")
