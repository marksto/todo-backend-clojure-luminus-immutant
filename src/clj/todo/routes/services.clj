(ns todo.routes.services
  (:require [ring.util.http-response :refer :all]
            [clojure.tools.logging :as log]
            [todo.config :refer [env]]
            [compojure.api.sweet :refer :all]
            [todo.todos :as t]
            [todo.schema :as s]))

(defn with-url
  [todo request]
  (let [host (or (env :host) (-> request :headers (get "host" "localhost")))
        scheme (name (or (env :scheme) (:scheme request)))
        id (:id todo)]
    (merge todo {:url (str scheme "://" host "/todos/" id)})))

(defapi service-routes
  {:swagger {:ui   "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version     "1.0.0"
                           :title       "ToDo API"
                           :description "ToDo Services"}}}}

  (context "/todos" []
    :tags ["todo"]

    (OPTIONS "/" []
      :summary "Support OPTIONS"
      (ok))

    (OPTIONS "/:id" []
      :summary "Support OPTIONS"
      (ok))

    (POST "/" request
      :return s/ToDoResponse
      :summary "Add a todo item"
      :body [request-body s/ToDoCreate]
      (let [todo (t/create request-body)]
        (ok (with-url todo request))))

    (PATCH "/:id" request
      :return s/ToDoResponse
      :summary "Update a todo item"
      :body [request-body s/ToDoUpdate]
      :path-params [id :- s/Id]
      (if-let [current (t/read id)]
        (ok (with-url (t/update id (merge current request-body)) request))
        (not-found)))

    (GET "/:id" request
      :return s/ToDoResponse
      :path-params [id :- s/Id]
      :summary "Find a todo by id"
      (if-let [todo (t/read id)]
        (ok (with-url todo request))
        (not-found)))

    (GET "/" request
      :return [s/ToDoResponse]
      :summary "Get all todos"
      (ok (map #(with-url % request) (t/read-all))))

    (DELETE "/:id" []
      :return s/ToDoResponse
      :path-params [id :- s/Id]
      :summary "Delete a todo"
      (ok (t/delete id)))

    (DELETE "/" []
      :summary "Delete all todos"
      (ok (t/delete-all)))))
