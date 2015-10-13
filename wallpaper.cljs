#!/usr/bin/env planck
(ns tkataja.papers
  (:require [planck.shell :refer [sh *sh-dir*]]
            [planck.bundle :as b]
            [clojure.string :refer [trim-newline]]
            [goog.string :as gstring]))

;;
;; Shell stuff
;;

(defn home-dir []
  (-> (sh "printenv" "HOME") :out trim-newline))

(defn working-dir []
  (-> (sh "pwd") :out trim-newline))

;;
;; Wallpaper fetching stuff
;;

(defn random-page [seed]
  (let [url (str "http://papers.co/random/" seed "/")]
    (println "Fetching page:" url)
    (:out (sh "curl" url))))

(defn scrape-names [page]
  (->> (re-seq #"<a href=\"http://papers.co/(.*)/" page)
       (map second)))

(def profile-postfixes
  {:macbook-retina-15 "-22-wallpaper.jpg"
   :iphone6 "-33-iphone6-wallpaper.jpg"})

(defn create-url [wallpaper-name {:keys [profile]}]
  (println "Using profile:" profile)
  (let [profile-postfix (profile profile-postfixes)
        download-url (str "http://papers.co/wallpaper/papers.co-" wallpaper-name profile-postfix)]
    download-url))

(defn download! [url to]
  (println "Downloading from url:" url "to" to)
  (sh "wget" url "-O" to))

;;
;; Wallpaper setting stuff
;;

(defn set-as-background! [file-path]
  (let [db-file (str (home-dir) "/Library/Application Support/Dock/desktoppicture.db")
        query (goog.string.format "UPDATE data SET value = '%s'" file-path)]
    (sh "sqlite3" db-file query)
    (sh "killall" "Dock")))

;;
;; Execute stuff
;;

(let [wallpaper-name (->> (random-page (rand-int 4096))
                          (scrape-names)
                          (take 5)
                          (shuffle)
                          (first))
      to-location (str (working-dir) "/" wallpaper-name ".jpg")]
  (-> (create-url wallpaper-name {:profile :macbook-retina-15})
      (download! to-location))
  (set-as-background! to-location))
