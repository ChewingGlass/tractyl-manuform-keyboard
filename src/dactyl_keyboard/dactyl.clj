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
            [dactyl-keyboard.hotswap-mesh :refer [thumb-hotswap-holes hotswap-holes hotswap-mesh]]
            [dactyl-keyboard.screws :refer [screw-insert-shape]]
            [dactyl-keyboard.palm-rest
             :refer
             [palm-rest-hole-rotate palm-buckle-holes palm-hole-origin]]
            [dactyl-keyboard.placement :refer :all]
            [dactyl-keyboard.web-connectors :refer :all]
            [dactyl-keyboard.trackball
             :refer
             [trackball-origin trackball-insertion-cyl raised-trackball btu-angle]]
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
  hotswap-mesh
  thumb-hotswap-holes
  ;;  hotswap-mesh
   (if trackball-enabled
     (translate trackball-origin (btu-angle raised-trackball))
     nil)
   (translate [0 0 -20] (cube 350 350 40))))

;(spit "things/palm-rest.scad" (write-scad palm-rest))

(spit "things/left.scad"
      (write-scad (mirror [-1 0 0] model-right)))
(def right-keys (difference
 (union
  connectors
  pinky-connectors
  key-holes
  (triangle-hulls    ; top two to the main keyboard, starting on the left
   (key-place 2 lastrow web-post-br)
   (key-place 3 lastrow web-post-bl)
   (key-place 2 lastrow web-post-tr)
   (key-place 3 lastrow web-post-tl)
   (key-place 3 cornerrow web-post-bl)
   (key-place 3 lastrow web-post-tr)
   (key-place 3 cornerrow web-post-br)
   (key-place 4 cornerrow web-post-bl)))
 hotswap-holes))

(def right-thumb (difference
  (union 
  thumb
    (if trackball-enabled
    (union
     ; top right vertical
     (triangle-hulls
      (thumb-tr-place web-post-br)
      (thumb-tr-place web-post-bl)
      (thumb-mr-place web-post-br))
     ; Between the top and middle
     (triangle-hulls
      (thumb-tr-place web-post-tl)
      (thumb-mr-place web-post-tr)
      (thumb-mr-place web-post-br))
     (triangle-hulls
      (thumb-tr-place web-post-bl)
      (thumb-tr-place web-post-tl)
      (thumb-mr-place web-post-br))
     ; Between middle and first bottom
     (triangle-hulls
      (thumb-mr-place web-post-tl)
      (thumb-br-place web-post-tr)
      (thumb-br-place web-post-br))
     (triangle-hulls
      (thumb-mr-place web-post-bl)
      (thumb-mr-place web-post-tl)
      (thumb-br-place web-post-br)
      (thumb-bl-place web-post-br))
     ; Between the top and middle over by the trackball
     (triangle-hulls
      (thumb-tr-place web-post-tl)
      (thumb-mr-place web-post-tr)
      (thumb-mr-place web-post-tl))
     ; Between the bottom two
     (triangle-hulls
      (thumb-br-place web-post-tr)
      (thumb-br-place web-post-tl)
      (thumb-bl-place web-post-br))
     (triangle-hulls
      (thumb-bl-place web-post-br)
      (thumb-bl-place web-post-bl)
      (thumb-br-place web-post-tl))
     ; Between the middle and the bl
     (triangle-hulls
      (thumb-mr-place web-post-tl)
      (thumb-bl-place web-post-tr)
      (thumb-bl-place web-post-br))
     )
    (union
     (triangle-hulls    ; top two
      (thumb-tl-place web-post-tr)
      (thumb-tl-place web-post-br)
      (thumb-tr-place thumb-post-tl)
      (thumb-tr-place thumb-post-bl))
     (triangle-hulls    ; bottom two
      (thumb-br-place web-post-tr)
      (thumb-br-place web-post-br)
      (thumb-mr-place web-post-tl)
      (thumb-mr-place web-post-bl))
     (triangle-hulls
      (thumb-mr-place web-post-tr)
      (thumb-mr-place web-post-br)
      (thumb-tr-place thumb-post-br))
     (triangle-hulls    ; between top row and bottom row
      (thumb-br-place web-post-tl)
      (thumb-bl-place web-post-bl)
      (thumb-br-place web-post-tr)
      (thumb-bl-place web-post-br)
      (thumb-mr-place web-post-tl)
      (thumb-tl-place web-post-bl)
      (thumb-mr-place web-post-tr)
      (thumb-tl-place web-post-br)
      (thumb-tr-place web-post-bl)
      (thumb-mr-place web-post-tr)
      (thumb-tr-place web-post-br))
     (triangle-hulls    ; top two to the middle two, starting on the left
      (thumb-tl-place web-post-tl)
      (thumb-bl-place web-post-tr)
      (thumb-tl-place web-post-bl)
      (thumb-bl-place web-post-br)
      (thumb-mr-place web-post-tr)
      (thumb-tl-place web-post-bl)
      (thumb-tl-place web-post-br)
      (thumb-mr-place web-post-tr)))
    )
  )
    thumb-hotswap-holes)
)

(spit "things/thumb-keys.scad"
      (write-scad
       (include "../nutsnbolts/cyl_head_bolt.scad")
       right-thumb))

(spit "things/right.scad"
      (write-scad
       (include "../nutsnbolts/cyl_head_bolt.scad")
       model-right))

(defn -main [dum] 1)

; dummy to make it easier to batch
