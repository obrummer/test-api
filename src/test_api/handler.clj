(ns test-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            [clojure.string :as str]))

; helper to generate uuid
(defn uuid [] (.toString (java.util.UUID/randomUUID)))

; people-collection mutable collection vector
(def people-collection (atom []))

; helper function to add a new person
(defn addperson [firstname surname]
  (swap! people-collection conj 
         {:firstname (str/capitalize firstname) 
          :surname (str/capitalize surname)
          :id (uuid)}))

; helper to update person
(defn updateperson [id firstname surname]
   (swap! people-collection
         (fn [people] (map #(if (= id (:id %)) 
                              (assoc % :firstname firstname :surname surname) %) people))))

; helper to delete person
(defn deleteperson [id]
  (swap! people-collection 
         (fn [people] (remove (fn [x] (= (:id x) id)) people))))

; return list of people
(defn people-handler [req]
       (response @people-collection))

; return person by id
(defn get-person-handler [req]
       (let [id (:id (:params req))]
       (response (filter (fn [x] (= (:id x) id)) @people-collection))))

; add person
(defn add-person-handler [req]
        (addperson (:firstname (:body req)) (:surname (:body req)) )
        (response @people-collection))

; update person
(defn update-person-handler [req]
        (updateperson (:id (:params req)) ( :firstname (:body req)) (:surname (:body req)))
        (response @people-collection))

; delete person
(defn delete-person-handler [req]
       (deleteperson (:id (:params req)))
       (response @people-collection))

; example JSON objects
(addperson "Functional" "Human")
(addperson "Micky" "Mouse")

; routes
(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/people" [] (wrap-json-response people-handler))
  (GET "/people/:id" _params (wrap-json-response get-person-handler))
  (POST "/people/add" [] (wrap-json-response (wrap-json-body add-person-handler {:keywords? true :bigdecimals? true})))
  (PUT "/people/:id" _params (wrap-json-response (wrap-json-body update-person-handler {:keywords? true :bigdecimals? true})))
  (DELETE "/people/:id" _params (wrap-json-response delete-person-handler))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false)))
