(ns alexi.xml.parse
  (:refer-clojure  :exclude [ancestors descendants])
  (:require [clojure.xml :as xml]
            [clojure.string :as st]
            [clojure.zip :as zip])
  (:use [clojure.java.io :only (file copy)]
        [clojure.data.zip ]
        [clojure.data.zip.xml]))

(def from-dir "/Users/alexi/dev/presto/tr/sas/war/WEB-INF/lib/")
(def to-dir "/tmp/test/")
(def ide-xml-config-path  "/Users/alexi/dev/presto/tr/emml/nbproject/ide-file-targets.xml")

(defn copy-file [src-path dest-path]
    (copy (file src-path) (file dest-path)))


(defn zip-xml-file [file-path] (-> file-path file xml/parse zip/xml-zip))

(defn extract-path-str-from-zip 
  [xml-zip]
  "Find attribute path of <pathelement> which decendant of <target name='run-selected-file-insrc' "
  ;; using xml1 because xml return sequence not single string
  (xml1-> xml-zip :target [(attr= :name "run-selected-file-in-src")]  descendants :pathelement (attr :path) ))


(defn seq-xml-file [file-path] (-> file-path  file xml/parse xml-seq))

(defn extract-path-str-from-seq [xml-s] (first (for  [x  xml-s :when (= :pathelement (:tag x))] 
  (:path (:attrs x)))))

(defn list-path-elements[path-str]
  (st/split  path-str #":"))

(defn build-from-to-map [path-element]
  (if (.endsWith path-element ".jar")
    (let [jar-name (last (st/split path-element #"/")) ]
       {:from  (str from-dir jar-name) :to (str to-dir jar-name)})))

(def jar-map
  (remove nil? (let [jar-seq (-> ide-xml-config-path zip-xml-file extract-path-str-from-zip list-path-elements)] 
                 (map build-from-to-map jar-seq))))

(defn -main []
  (for [e jar-map]
    (copy-file (:from e) (:to e))))




