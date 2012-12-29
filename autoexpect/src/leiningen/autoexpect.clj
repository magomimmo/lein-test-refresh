(ns leiningen.autoexpect
  (:require [leinjacker.deps :as deps]))

(defn- eval-in-project
  "Support eval-in-project in both Leiningen 1.x and 2.x."
  [project form init]
  (let [[eip two?] (or (try (require 'leiningen.core.eval)
                            [(resolve 'leiningen.core.eval/eval-in-project)
                             true]
                            (catch java.io.FileNotFoundException _))
                       (try (require 'leiningen.compile)
                            [(resolve 'leiningen.compile/eval-in-project)]
                            (catch java.io.FileNotFoundException _)))]
    (if two?
      (eip project form init)
      (eip project form nil nil init))))

(defn- add-deps [project]
  (-> project
      (deps/add-if-missing '[lein-autoexpect "0.2.4-SNAPSHOT"])
      (deps/add-if-missing '[org.clojure/tools.namespace "0.2.2"])))

(defn ^{:help-arglists '([])} autoexpect
  "Autoruns expecations on source change

USAGE: lein autoexpect
Runs expectations whenever there is a change to code in classpath.
Reports test successes and failures to STDOUT.

USAGE: lein autoexpect :growl
Runs expectations whenever code changes.
Reports results to growl and STDOUT."
  [project & args]
  (let [should-growl (some #{:growl ":growl" "growl"} args)]
    (eval-in-project
     (add-deps project)
     `(autoexpect.runner/monitor-project ~should-growl)
     `(require 'autoexpect.runner))))