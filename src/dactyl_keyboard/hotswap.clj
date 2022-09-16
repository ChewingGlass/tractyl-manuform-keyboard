(ns dactyl-keyboard.hotswap
  (:refer-clojure :exclude
                  [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.utils :refer [deg2rad]]))

(def socket-distance 5.5)
(def socket-height 5.5)

(defn official-hotswap [width length height wings?]
  (translate [0 0 0]
             (difference
              (union
               (translate [0 -0.4 0] (cube width length height))
               (translate [(* 0.866 socket-distance) (* -0.5 socket-distance) 0]
                          (cube width length height))
               (if wings?
                 (union
                  (translate [(/ width -2) -0.4 0] (cube width 2.5 height))
                  (translate
                   [(+ (* 0.866 socket-distance) (/ width 2)) (* -0.5 socket-distance) 0]
                   (cube width 2.5 height)))
                 nil)))))

(def official-hotswap-clamp
  (translate [-1 -1 -0.5]
             (difference
              (official-hotswap 6.25 6.25 3.5 false)
              (translate [0 0 1] (official-hotswap 5.25 5.25 2 true))
              ; The middle piece
              (->>
               (cube 2 5 2)
               (translate
                [(+ (/ (* 0.866 socket-distance) 2) 0.5)
                 (+ (/ (* 0.5 socket-distance) -1) 2)
                 2.5])
               (rotate (deg2rad -30) [0 0 1])))))

;
;(def single-plate-with-hotswap (difference
;                                (translate [0 2 (/ plate-thickness 2)] (cube (+ keyswitch-width 4) (+ keyswitch-height 7) 3))
;                                (translate [0 0 (/ plate-thickness 2)] (cube keyswitch-width keyswitch-height 3))
;                                buckle-holes-on-key))