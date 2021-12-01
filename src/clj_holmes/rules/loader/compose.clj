(ns clj-holmes.rules.loader.compose
  (:require [clj-holmes.rules.loader.builder :as rules.builder]
            [clojure.walk :as walk]))

(defn ^:private condition-fn-by-condition
  "Returns a function to validate the rule patterns results."
  [condition]
  (case condition
    :and (fn [& elements]
           (every? identity elements))
    :not not))

(defn ^:private compose-rule*
  "Replace the :pattern and :pattern-not within the match function for the expression."
  [entry]
  (if (and (map? entry)
           (or (:pattern entry)
               (:pattern-not entry)))
    (let [condition (if (:pattern entry) :and :not)
          condition-fn (condition-fn-by-condition condition)]
      (-> entry
          (assoc :condition-fn condition-fn)
          (assoc :check-fn (rules.builder/build-pattern-fn entry))))
    entry))

(defn compose-rule [rule]
  (walk/prewalk compose-rule* rule))
