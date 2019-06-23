(defproject bowling "0.0.0"
  :description "tenpin scoring system"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                  [cljfmt "0.5.1"]
                 ]
  :main ^:skip-aot tenpin.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
