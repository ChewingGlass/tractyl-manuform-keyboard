(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude
                  [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.palm-rest :refer [palm-rest-hole-rotate]]
            [dactyl-keyboard.utils :refer [deg2rad]]
            [dactyl-keyboard.constants :refer :all]
            [dactyl-keyboard.hand :refer [hand]]
            [dactyl-keyboard.buckle :refer :all]
            [dactyl-keyboard.hotswap-mesh :refer [hotswap-holes]]
            [dactyl-keyboard.screws :refer [screw-insert-shape]]
            [dactyl-keyboard.palm-rest
             :refer
             [palm-rest-hole-rotate palm-buckle-holes palm-hole-origin]]
            [dactyl-keyboard.placement :refer :all]
            [dactyl-keyboard.web-connectors :refer :all]
            [dactyl-keyboard.trackball
             :refer
             [trackball-origin trackball-insertion-cyl raised-trackball dowell-angle]]
            [dactyl-keyboard.thumbs :refer :all]
            [dactyl-keyboard.walls :refer :all]
            [dactyl-keyboard.peripherals :refer :all]
            [dactyl-keyboard.screws :refer :all]))

(def connectors
  (apply union
         (concat
          ;; Row connections
          (for [column (range 0 (dec ncols))
                row    (range 0 lastrow)]
            (triangle-hulls
             (key-place (inc column) row web-post-tl)
             (key-place column row web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place column row web-post-br)))

          ;; Column connections
          (for [column columns
                row    (range 0 cornerrow)]
            (triangle-hulls
             (key-place column row web-post-bl)
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tl)
             (key-place column (inc row) web-post-tr)))

          ;; Diagonal connections
          (for [column (range 0 (dec ncols))
                row    (range 0 cornerrow)]
            (triangle-hulls
             (key-place column row web-post-br)
             (key-place column (inc row) web-post-tr)
             (key-place (inc column) row web-post-bl)
             (key-place (inc column) (inc row) web-post-tl))))))


(def pinky-connectors
  (apply union
         (concat
          ;; Row connections
          (for [row (range 0 lastrow)]
            (triangle-hulls
             (key-place lastcol row web-post-tr)
             (key-place lastcol row wide-post-tr)
             (key-place lastcol row web-post-br)
             (key-place lastcol row wide-post-br)))

          ;; Column connections
          (for [row (range 0 cornerrow)]
            (triangle-hulls
             (key-place lastcol row web-post-br)
             (key-place lastcol row wide-post-br)
             (key-place lastcol (inc row) web-post-tr)
             (key-place lastcol (inc row) wide-post-tr))))))

(def pinky-walls
  (union
   (key-wall-brace lastcol cornerrow 0 -1 web-post-br lastcol cornerrow 0 -1 wide-post-br)
   (key-wall-brace lastcol 0 0 1 web-post-tr lastcol 0 0 1 wide-post-tr)))


(def model-right
  (difference
   (union
    key-holes
    pinky-connectors
    pinky-walls
    connectors
    thumb
    thumb-connectors
    ;    usb-jack
    (difference
      (union
       case-walls
       screw-insert-outers)
      ; Leave room to insert the ball
      (if trackball-enabled (translate trackball-origin trackball-insertion-cyl) nil)
      usb-jack
      trrs-holder-hole
      screw-insert-holes
      (translate palm-hole-origin (palm-rest-hole-rotate palm-buckle-holes))))
    hotswap-holes
   (if trackball-enabled
     (translate trackball-origin (dowell-angle raised-trackball))
     nil)
   (translate [0 0 -20] (cube 350 350 40))))

;(spit "things/palm-rest.scad" (write-scad palm-rest))

(spit "things/left.scad"
      (write-scad (mirror [-1 0 0] model-right)))

(spit "things/right.scad"
      (write-scad
       (include "../nutsnbolts/cyl_head_bolt.scad")
       (difference
       (union
       connectors
       pinky-connectors
       key-holes)
       hotswap-holes
        )))

(defn -main [dum] 1)

; dummy to make it easier to batch
