(ns clj-holmes.filesystem
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.reader.edn :as edn])
  (:import (java.io File)))

(defn ^:private remove-dot-slash [filename]
  (if (string/starts-with? filename "./")
    (string/replace filename #"\./" "")
    filename))

(defn ^:private file? [^File file]
  (.isFile file))

(defn ^:private clj-file? [^File file]
  (and (file? file)
       (-> file .toString (string/includes? "project.clj") not)
       (-> file .toString (.endsWith ".clj"))))

(defn clj-files-from-directory! [^String directory]
  (let [file-sanitize (comp remove-dot-slash str)]
    (->> directory
         File.
         file-seq
         (filter clj-file?)
         (map file-sanitize))))

(defn load-rules! [^String directory]
  (let [reader (comp edn/read-string slurp)]
    (->> directory
         File.
         file-seq
         (filter file?)
         (map reader))))

(defn save-sarif-report! [sarif-report directory]
  (let [sarif-output-file (format "%s/report.sarif" directory)]
    (when sarif-report
      (->> sarif-report json/write-str (spit sarif-output-file))
      (println "Sarif report can be find in" sarif-output-file))))