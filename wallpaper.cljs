#!/usr/bin/env planck
(ns tkataja.papers
  (:require [planck.shell :refer [sh]]
            [planck.core :refer [exit]]
            [planck.core :refer [*command-line-args*]]
            planck.bundle
            [clojure.string :refer [trim-newline]]))

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
    (println "fetching random page:" url)
    (:out (sh "curl" url))))

(defn topic-page [topic]
  (let [url (str "http://papers.co/search/" topic)]
    (println "searching using topic:" topic)
    (:out (sh "curl" url))))

(defn scrape-names [page]
  (->> (re-seq #"<a href=\"http://papers.co/(.*)/" page)
       (map second)))

(def profile-postfixes
  {:macbook-retina-15 "-22-wallpaper.jpg"
   :iphone6 "-33-iphone6-wallpaper.jpg"})

(defn create-url [wallpaper-name {:keys [profile]}]
  (println "using profile:" profile)
  (let [profile-postfix (profile profile-postfixes)
        download-url (str "http://papers.co/wallpaper/papers.co-" wallpaper-name profile-postfix)]
    download-url))

(defn download! [url to]
  (println "downloading from url:" url "to" to)
  (sh "wget" url "-O" to)
  to)

;;
;; Only tested on El Capitan, should work with Yosemite...
;;

(defn set-as-background! [file-path]
  (let [db-file (str (home-dir) "/Library/Application Support/Dock/desktoppicture.db")
        query (goog.string.format "UPDATE data SET value = '%s'" file-path)]
    (println "using db file: " db-file)
    (println "execute query: " query)
    (sh "sqlite3" db-file query)
    (sh "killall" "Dock")))

(def help
  (str "Usage: ./wallpaper.cljs --arg1 val --arg2 val\n"
       "\n"
       "This script fetches random wallpaper and sets it as background.\n"
       "\n"
       "Supported args and values:\n"
       "  --dl-only true    Download only\n"
       "  --topic <string>  Fetches wallpaper with given topic"))

(defn print-help []
  (println help))

(defn show-help [args]
  (let [known-args #{"--topic" "--dl-only"}]
    (when (or (odd? (count args)))
      (print-help)
      (exit 0))))

(defn parse-cli-opts []
  (let [options-map (->> *command-line-args* (partition 2) (mapv vec) (into {}))]
    options-map))

(defn main []
  (show-help *command-line-args*)
  (let [cli-opts (parse-cli-opts)
        topic (get cli-opts "--topic")
        only-download? (get cli-opts "--dl-only")
        fetch-fn (fn []
                   (if topic
                     (topic-page topic)
                     (random-page (rand-int 4096))))
        wallpaper-name (->> (fetch-fn)
                            (scrape-names)
                            (take 12) ;; 12 results per page
                            (shuffle)
                            (first))
        to-location (str (working-dir) "/" wallpaper-name ".jpg")]

  (-> (create-url wallpaper-name {:profile :macbook-retina-15})
      (download! to-location)
      (cond-> (not only-download?) (set-as-background!)))))

(main)
