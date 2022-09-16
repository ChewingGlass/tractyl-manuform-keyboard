(ns dactyl-keyboard.right-test
  (:refer-clojure :exclude
                  [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.utils :refer [deg2rad]]
            [dactyl-keyboard.constants :refer :all]
            [dactyl-keyboard.thumbs :refer :all]
            [dactyl-keyboard.dactyl :refer [right-thumb model-right right-keys]]
            [dactyl-keyboard.bottom-plate :refer [right-plate]]
            [dactyl-keyboard.hotswap-mesh :refer [left-hotswap-mesh thumb-hotswap-mesh left-thumb-hotswap-mesh hotswap-mesh]]
            [dactyl-keyboard.placement :refer [key-place]]
            [dactyl-keyboard.hand :refer [hand]]
            [dactyl-keyboard.trackball :refer :all]))

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.25)
(def sa-double-length 37.5)
(def sa-cap
  {1   (let [bl2     (/ 18.5 2)
             m       (/ 17 2)
             key-cap (cube 18.25 18.25 2)]
         (->> key-cap
              (translate [0 0 (+ 4 plate-thickness)])
              (color [220/255 163/255 163/255 1])))
   2   (let [bl2     sa-length
             bw2     (/ 18.25 2)
             key-cap (hull
                      (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                           (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                           (translate [0 0 0.05]))
                      (->> (polygon [[6 16] [6 -16] [-6 -16] [-6 16]])
                           (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                           (translate [0 0 12])))]
         (->> key-cap
              (translate [0 0 (+ 5 plate-thickness)])
              (color [127/255 159/255 127/255 1])))
   1.5 (let [bl2     (/ 18.25 2)
             bw2     (/ 27.94 2)
             key-cap (hull
                      (->> (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
                           (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                           (translate [0 0 0.05]))
                      (->> (polygon [[11 6] [-11 6] [-11 -6] [11 -6]])
                           (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                           (translate [0 0 12])))]
         (->> key-cap
              (translate [0 0 (+ 5 plate-thickness)])
              (color [240/255 223/255 175/255 1])))})

(def thumbcaps
  (union
   (thumb-1x-layout (sa-cap 1))
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] (sa-cap 1)))))

(def caps
  (apply union
         (for [column columns
               row    rows
               :when  (or (.contains [2 3] column)
                          (not= row lastrow))]
           (->> (sa-cap (if (and (true? pinky-15u) (= column lastcol)) 1.5 1))
                (key-place column row)))))

(def hand-on-test
  (translate [-5 -60 92]
             (rotate (deg2rad -27) [1 0 0]
                     (rotate (deg2rad 12) [0 0 1]
                             (rotate (+ tenting-angle (deg2rad 5)) [0 1 0]
                                     (rotate
                                      (deg2rad -90) [1 0 0]
                                      (mirror [0 1 0] hand)))))))

(spit "things/right-test.scad"
      (write-scad
       (difference
        (union
         ;         hand-on-test
         (color [220/255 120/255 120/255 1] (translate [0 0 -1.5] hotswap-mesh))
         (color [220/255 120/255 120/255 1] thumb-hotswap-mesh)

         (color [220/255 163/255 163/255 1] right-plate)
         right-thumb)
         ;         (translate (map + palm-hole-origin [0 (+ buckle-length 3) (/ buckle-height 2)])
         ;                    (palm-rest-hole-rotate palm-rest))
         ;         (if trackball-enabled (translate trackball-origin test-ball) nil)
         ;         thumbcaps
         ;         caps)


         (translate [0 0 -20] (cube 350 350 40)))))

(spit "things/left-thumb-hotswap-mesh-test.scad" (write-scad  (union left-thumb-hotswap-mesh (mirror [-1 0 0] right-thumb) )))
(spit "things/left-hotswap-mesh-test.scad" (write-scad  (union left-hotswap-mesh (mirror [-1 0 0] right-keys))))
