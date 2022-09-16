(ns dactyl-keyboard.hotswap-mesh
  (:refer-clojure :exclude
                  [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.utils :refer [deg2rad]]
            [dactyl-keyboard.constants :refer :all]
            [dactyl-keyboard.placement :refer :all]
            [dactyl-keyboard.thumbs :refer :all]
            [dactyl-keyboard.utils :refer [deg2rad]]
            [dactyl-keyboard.hotswap :refer [official-hotswap-clamp]]
            [dactyl-keyboard.screws :refer [screw-insert-shape]]
            [dactyl-keyboard.trackball :refer [trackball-mount-translated-to-model]]))

;;;;;;;;;;;;;
;; Hotswap ;;
;;;;;;;;;;;;;

(def hotswap-connector (translate [0 3 -2.5] (cube 6 4 4)))
(def bottom-hotswap-connector (rotate (deg2rad 180) [0 0 1] hotswap-connector))

(defn connector-place [column row hotswap hotswap-connector]
  (if (and
       (not (and (= column 3) (= row lastrow))))
    (let [bottom          (key-place column row (translate [0 0 (if (or (and (= row (- lastrow 1)) (= column 2))
                                                                        (< row (- lastrow 1))) -4 0)] hotswap-connector))
          bottom-next-row (key-place column (+ row 1) (translate [0 0 -1] hotswap-connector))]
      (union
       ; Hull directly down
       (hull (key-place column row hotswap-connector) bottom)
       ; hull over.
       (if (or (and (= row (- lastrow 1)) (= column 2))
               (and (< row (- lastrow 1)) (< column lastcol)))
         (hull bottom (key-place (+ 1 column) row (translate [0 0 (if (= row 0) -6 -2)] hotswap-connector))))
       ; hull to the next row
       (if (or (and (= row (- lastrow 1)) (or (= column 2) (= column 3)))
               (< row (- lastrow 1)))
         (hull bottom bottom-next-row))))))

(defn thumb-hotswap-place [hotswap]
  (let [top-hotswap              (rotate (deg2rad 180) [0 0 1] hotswap)]
    (union
     (thumb-mr-place top-hotswap)
     (thumb-br-place top-hotswap)
     (if trackball-enabled nil (thumb-tl-place hotswap))
     (thumb-bl-place hotswap)
     (thumb-tr-place hotswap))))

(def thumb-hotswap-mesh-connectors
  (if trackball-enabled
    (union
     (hull (thumb-mr-place bottom-hotswap-connector) (thumb-tr-place bottom-hotswap-connector))
     (hull (thumb-mr-place bottom-hotswap-connector) (thumb-bl-place bottom-hotswap-connector))
     (hull (thumb-bl-place bottom-hotswap-connector) (thumb-br-place hotswap-connector)))
    (union
     (hull (thumb-tl-place bottom-hotswap-connector) (thumb-tr-place bottom-hotswap-connector))
     (hull (thumb-tl-place bottom-hotswap-connector) (thumb-bl-place bottom-hotswap-connector))
     (hull (thumb-tl-place bottom-hotswap-connector) (thumb-mr-place hotswap-connector))
     (hull (thumb-mr-place hotswap-connector) (thumb-br-place hotswap-connector))
     (hull (thumb-bl-place bottom-hotswap-connector) (thumb-br-place hotswap-connector)))))

(defn hotswap-place [hotswap]
  (let [top-hotswap              (rotate (deg2rad 180) [0 0 1] hotswap)
        bottom-hotswap-connector (rotate (deg2rad 180) [0 0 1] hotswap-connector)]
    (union
     ; top row is a litte different
     (apply union
            (for [column columns]
              (union
               (->> hotswap
                    (key-place column 0))
               (connector-place column 0 hotswap bottom-hotswap-connector))))
     (apply union
            (for [column columns
                  row    (range 1 nrows)
                  :when  (or (.contains [2 3] column)
                             (not= row lastrow))]
              (union
               (->> top-hotswap
              (translate [0 (if (= row lastrow) -1 0) 0])
                    (key-place column row))
               (connector-place column row top-hotswap hotswap-connector)))))))

(def translated-hotswap-clamp (translate [-3.1 -1.5 0] official-hotswap-clamp))
(def hotswap-mesh-clamps
  (hotswap-place translated-hotswap-clamp))
(def left-hotswap-mesh-clamps
  (hotswap-place (mirror [-1 0 0] translated-hotswap-clamp)))

(def thumb-hotswap-mesh-clamps
  (thumb-hotswap-place translated-hotswap-clamp))
(def left-thumb-hotswap-mesh-clamps
  (thumb-hotswap-place (mirror [-1 0 0] translated-hotswap-clamp)))

(def hotswap-screw-hole (cylinder screw-insert-case-radius 10))

(defn hotswap-screw-place [in-shape]
  (let [shape (translate [0 -12.5 0] (rotate (deg2rad -12) [1 0 0] in-shape))]
    (union
     (key-place 1 0 shape)
     (key-place 3 0 shape)
     (key-place 1 1 shape)
     (key-place 3 2 (translate [0 -1 0] shape)))))

(defn thumb-hotswap-screw-place [in-shape]
  (let [shape (translate [0 -11.5 -2] in-shape)]
    (union
     (if trackball-enabled (thumb-tr-place (translate [-10.5 4 0] shape)) (thumb-tl-place shape))
     (thumb-bl-place shape))))

(def hotswap-holes (hotswap-screw-place hotswap-screw-hole))
(def thumb-hotswap-holes (thumb-hotswap-screw-place (translate [0 0 4] (cylinder screw-insert-case-radius 15))))

(def hotswap-screw-holders
  (let [shape      (rotate (deg2rad 180) [1 0 0]
                           (screw-insert-shape (+ screw-insert-bottom-radius 1.4) (+ screw-insert-top-radius 1.4) (+ screw-insert-height 1.5)))
        hollow-out (rotate (deg2rad 180) [1 0 0]
                           (screw-insert-shape screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))]
    (difference
     (union
      (hotswap-screw-place shape)
      )
     (hotswap-screw-place hollow-out)
     (hotswap-screw-place hotswap-screw-hole))))

(def thumb-hotswap-screw-holders
  (let [shape      (rotate (deg2rad 180) [1 0 0]
                           (screw-insert-shape (+ screw-insert-bottom-radius 1.3) (+ screw-insert-top-radius 1.3) (+ screw-insert-height 1.5)))
        hollow-out (rotate (deg2rad 180) [1 0 0]
                           (screw-insert-shape screw-insert-bottom-radius screw-insert-top-radius screw-insert-height))]
    (difference
     (union
      (thumb-hotswap-screw-place shape)
      (thumb-hotswap-screw-place (hull shape (translate [-1.5 -3 2] (cube 3 2 3)))))
     (thumb-hotswap-screw-place hollow-out)
     (thumb-hotswap-screw-place hotswap-screw-hole))))

(def hotswap-mesh
  (difference
   (union
    hotswap-mesh-clamps
    hotswap-screw-holders)
   hotswap-holes
   (translate [0 0 -20] (cube 350 350 40)) ; Make sure it doesn't go below the ground
   ))

(def left-hotswap-mesh
  (mirror [-1 0 0] (difference
   (union
    left-hotswap-mesh-clamps
    hotswap-screw-holders)
   hotswap-holes
   (translate [0 0 -20] (cube 350 350 40)) ; Make sure it doesn't go below the ground
   )))

(def thumb-hotswap-mesh
  (difference
   (union
    thumb-hotswap-mesh-clamps
    thumb-hotswap-mesh-connectors
    thumb-hotswap-screw-holders)
    thumb-hotswap-holes))

(def left-thumb-hotswap-mesh
  (mirror [-1 0 0] (difference
   (union
    left-thumb-hotswap-mesh-clamps
    thumb-hotswap-mesh-connectors
    thumb-hotswap-screw-holders)
   thumb-hotswap-holes)))
;   model-right
;   trackball-mount-translated-to-model))

(spit "things/hotswap-mesh.scad" (write-scad hotswap-mesh))
(spit "things/thumb-hotswap-mesh.scad" (write-scad thumb-hotswap-mesh))
(spit "things/left-hotswap-mesh.scad" (write-scad left-hotswap-mesh))
(spit "things/left-thumb-hotswap-mesh.scad" (write-scad  left-thumb-hotswap-mesh))
