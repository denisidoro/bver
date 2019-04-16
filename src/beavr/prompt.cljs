(ns beavr.prompt
  (:require [beavr.ansi :as ansi]
            [beavr.argument :as arg]
            [beavr.options :as options]
            [beavr.shell :as sh]
            [beavr.text :as text]
            [clojure.string :as str]
            [quark.debug :as debug]))

(def ^:private readline-sync (js/require "readline-sync"))

(defn command-str
  [script props]
  (str/join
   " "
   (into [script]
         (map
          (fn [[k v]]
            (if v
              (str (-> k name arg/with-double-dashes) "=" (text/quoted v))
              (-> k name arg/with-double-dashes)))
          props))))

(defn fzf!
  [coll field]
  (cond
    (not (some-> coll seq))
    nil

    (= 1 (count coll))
    (first coll)

    :else
    (let [prompt-str (text/with-leading-space field)
          props      {:height      10
                      :prompt      prompt-str
                      ; :header header
                      :no-sort     nil
                      :reverse     nil
                      :inline-info nil}
          fzf        (command-str "fzf" props)
          cmd        (str "echo \"" (str/join "\n" coll) "\" | " fzf)
          opts       #js {:stdio #js ["inherit" "pipe" "inherit"] :shell true}]
      (sh/sh cmd opts))))

(defn read!
  [field]
  (let [result (.question readline-sync (str ansi/light-blue field ansi/reset " "))]
    (.write sh/stdout "\033[1A")
    (.write sh/stdout "\033[2K")
    result))
