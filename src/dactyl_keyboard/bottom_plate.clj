(ns dactyl-keyboard.bottom-plate
  (:refer-clojure :exclude
                  [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.utils :refer [deg2rad]]
            [dactyl-keyboard.walls :refer :all]
            [dactyl-keyboard.web-connectors :refer :all]
            [dactyl-keyboard.placement :refer :all]
            [dactyl-keyboard.constants :refer :all]
            [dactyl-keyboard.screws :refer :all]
            [dactyl-keyboard.thumbs :refer :all]
            [dactyl-keyboard.hotswap-mesh :refer [hotswap-mesh]]
            [dactyl-keyboard.tenting
             :refer
             [thumb-tent-origin index-tent-origin tent-nut tent-thread]]
            [dactyl-keyboard.trackball :refer [trackball-mount-translated-to-model]]
            [dactyl-keyboard.peripherals :refer :all]
            [dactyl-keyboard.dactyl :refer [model-right]]))

;;;;;;;;;;
;; Case ;;
;;;;;;;;;;


; Based on right-wall, just trying to make the plate cutout of it
(def right-wall-plate
  (let [tr        (if (true? pinky-15u) wide-post-tr web-post-tr)
        br        (if (true? pinky-15u) wide-post-br web-post-br)
        hull-with (translate (key-position 0 0 [0 0 0]) (square 1 1))]
    (union (hull (cut (key-wall-brace lastcol 0 0 1 tr lastcol 0 1 0 tr)) hull-with)
           (for [y (range 0 lastrow)]
             (hull (cut (key-wall-brace lastcol y 1 0 tr lastcol y 1 0 br)) hull-with))
           (for [y (range 1 lastrow)]
             (hull (cut (key-wall-brace lastcol (dec y) 1 0 br lastcol y 1 0 tr)) hull-with))
           (hull (cut (key-wall-brace lastcol cornerrow 0 -1 br lastcol cornerrow 1 0 br)) hull-with))))

(def plate-attempt
  (difference
   (extrude-linear {:height bottom-plate-thickness}
                   (union
                    ; pro micro wall
                    (for [x (range 0 (- ncols 1))]
                      (hull (cut (key-wall-brace x 0 0 1 web-post-tl x 0 0 1 web-post-tr))
                            (translate (key-position x lastrow [0 0 0])
                                       (square (+ keyswitch-width 15) keyswitch-height))))
                    (for [x (range 1 ncols)]
                      (hull (cut (key-wall-brace x 0 0 1 web-post-tl (dec x) 0 0 1 web-post-tr))
                            (translate (key-position x 2 [0 0 0]) (square 1 1))))
                    (hull (cut back-pinky-wall)
                          (translate (key-position lastcol 0 [0 0 0]) (square keyswitch-width keyswitch-height)))
                    (hull (cut thumb-walls) (translate bl-thumb-loc (square 1 1)))
                    right-wall-plate
                    (hull (cut back-convex-thumb-wall-0) (translate bl-thumb-loc (square 1 1)))
                    (hull (cut back-convex-thumb-wall-1) (translate bl-thumb-loc (square 1 1)))
                    (hull (cut back-convex-thumb-wall-2) (translate bl-thumb-loc (square 1 1)))
                    (hull (cut thumb-corners))
                    (hull (cut thumb-to-left-wall)
                          (translate (key-position (- lastcol 1) (- lastrow 1) [0 0 0]) (square 1 1)))
                    (hull (cut non-thumb-walls))))
   (translate [0 0 -10] screw-insert-screw-holes)))


(spit "things/test.scad"
      (write-scad
       (difference trrs-holder trrs-holder-hole)))


(def right-plate
  (difference
   (union
    (if trackball-enabled trackball-mount-translated-to-model nil)
    usb-holder-holder
    trrs-holder
    (translate [0 0 (/ bottom-plate-thickness -2)] plate-attempt)
    (translate thumb-tent-origin tent-nut)
    (translate index-tent-origin tent-nut))
   (translate thumb-tent-origin tent-thread)
   (translate index-tent-origin tent-thread)
   (translate [0 0 -22] (cube 350 350 40))
   usb-jack
   trrs-holder-hole
   model-right)) ; Just rm the whole model-right to make sure there's no obstruction))

(spit "things/right-plate.scad"
      (write-scad
       (include "../nutsnbolts/cyl_head_bolt.scad")
       right-plate))

(spit "things/left-plate.scad"
      (write-scad
       (include "../nutsnbolts/cyl_head_bolt.scad")
       (mirror [-1 0 0] right-plate)))