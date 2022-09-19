(ns dactyl-keyboard.constants
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]
            [unicode-math.core :refer :all]
            [dactyl-keyboard.utils :refer [deg2rad]]))

(def screw-insert-case-radius 1.5)

(def bottom-plate-thickness 2)

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 14.2)

;; Was 14.1, then 14.25
(def keyswitch-width 14.2)

(def sa-profile-key-height 12.7)

(def plate-thickness 3)
(def side-nub-thickness 4)
(def retention-tab-thickness 1.5)

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

(def nrows 4)
(def ncols 5)
(def trackball-enabled false)
(def printed-hotswap? false)

; Whether you want the 3d printed version of the hotswap or you ordered some from krepublic

(def α (/ π 8))

; curvature of the columns
(def β (/ π 26))

; curvature of the rows
(def centerrow (- nrows 2.5))

; controls front-back tilt
(def centercol 2)

; controls left-right tilt / tenting (higher number is more tenting)
(def tenting-angle (deg2rad 20))

; or, change this for more precise tenting control
(def column-style
  (if (> nrows 5) :orthographic :standard))

; options include :standard, :orthographic, and :fixed
; (def column-style :fixed)
(def pinky-15u false)

(defn column-offset [column]
  (cond
    (= column 2)  [0 2.82 -4.5]
    (= column 3)  [0 -1 -4]
    (>= column 4) [0 -16 -5.50]
    ; original [0 -5.8 5.64]
    :else         [0 -5 1.5]))

(def thumb-offsets [6 0 10])

(def keyboard-z-offset 23.5)

; controls overall height; original=9 with centercol=3; use 16 for centercol=2

(def extra-width 2.5)

; extra space between the base of keys; original= 2
(def extra-height 1.0)

; original= 0.5

(def wall-z-offset -5)

; original=-15 length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 5)

; offset in the x and/or y direction for the first downward-sloping part of the wall (negative)
(def wall-thickness 3)

; wall thickness parameter; originally 5

;; Settings for column-style == :fixed
;; The defaults roughly match Maltron settings
;;   http://patentimages.storage.googleapis.com/EP0219944A2/imgf0002.png
;; Fixed-z overrides the z portion of the column ofsets above.
;; NOTE: THIS DOESN'T WORK QUITE LIKE I'D HOPED.
(def fixed-angles [(deg2rad 10) (deg2rad 10) 0 0 0 (deg2rad -15) (deg2rad -15)])
(def fixed-x [-41.5 -22.5 0 20.3 41.4 65.5 89.6])

; relative to the middle finger
(def fixed-z [12.1 8.3 0 5 10.7 14.5 17.5])
(def fixed-tenting (deg2rad 0))


(def trackball-middle-translate [-6.5 6 -0.5])
(def thumb-tip-offset [-35 -16 -6.5])

(def lastrow (dec nrows))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

(def retention-tab-hole-thickness (- plate-thickness retention-tab-thickness))
(def mount-width (+ keyswitch-width 3))
(def mount-height (+ keyswitch-height 3))

; If you use Cherry MX or Gateron switches, this can be turned on.
; If you use other switches such as Kailh, you should set this as false
(def create-side-nubs? false)
(def create-top-nubs? false)

; Hole Depth Y: 4.4
(def screw-insert-height 4)

; Hole Diameter C: 4.1-4.4
(def screw-insert-bottom-radius (/ 4.0 2))
(def screw-insert-top-radius (/ 3.9 2))

(def left-wall-x-offset 5)

; original 10
(def left-wall-z-offset 3)

; original 3

(def columns (range 0 ncols))
(def rows (range 0 nrows))