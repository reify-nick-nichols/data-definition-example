(defproject data "0.0.0"
  :description "An illustration of the differences between spec, spec-tools, and malli"
  :url "https://github.com/reify-nick-nichols/data-definition-example"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.3"]]
  :main ^:skip-aot data.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
