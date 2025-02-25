(ns dactyl-keyboard.trackball
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
            [dactyl-keyboard.web-connectors :refer :all]
            [dactyl-keyboard.walls :refer [wall-locate3 bottom-hull bottom]]))

;;;;;;;;;;;;;;;
;; Trackball ;;
;;;;;;;;;;;;;;;

(def dowel-depth-in-shell 1.5)
(def bearing-protrude (- 3 dowel-depth-in-shell))

; Radius of the baring minus how deep it's going into the shell
(def trackball-width 34)
(def trackball-width-plus-bearing (+ bearing-protrude trackball-width 1))

; Add one just to give some wiggle
(def holder-thickness 4.2)
(def outer-width (+ (* 2 holder-thickness) trackball-width-plus-bearing))

(def axel-angle 15)
(def dowell-width 3)
(def dowel-top-change 0)
(def dowel-top-height 1.5)
(def dowell-height 6)

; Dowel height is actually 6mm. But attempting to get it to "snap" in place
(def dowell
  (union
    (cylinder (- (/ dowell-width 2) dowel-top-change) (+ dowell-height dowel-top-height) :fn 50)
    (cylinder (/ dowell-width 2) dowell-height :fn 50)))
(def bearing (cylinder (/ 8.5 2) 3))

; Bearing is actually 6mm x 2.5mm, model it as 8.5mm x 3 to give it room to spin
(def dowell-bearing (rotate (deg2rad 90) [1 0 0] (union dowell bearing)))

(defn rotated_dowell [angle]
  (rotate (deg2rad angle) [0, 0, 1]
          (rotate (deg2rad axel-angle) [0, 1, 0]
                  (translate [(+ (/ trackball-width-plus-bearing 2) dowel-depth-in-shell) 0 0]
                             (union
                              ; Add a cube on the side of the dowell so there's an insertion point when we diff with the shell
                              (translate [(- (/ dowell-width 2)) 0 0]
                                         (cube (+ dowell-width 1) (- dowell-height dowel-top-change) dowell-width))
                              dowell-bearing)))))

(def dowells
  (union
   (rotated_dowell 0)
   (rotated_dowell 120)
   (rotated_dowell 240)))
(def vertical-hold 0)

; Millimeters of verticle hold after the curviture of the sphere ends to help hold the ball in

(def cup
  (difference
   (union
    (sphere (/ outer-width 2)) ; Main cup sphere
    ; add a little extra to hold ball in
    (translate [0, 0, (/ vertical-hold 2)] (cylinder (/ outer-width 2) vertical-hold)))

   (sphere (/ trackball-width-plus-bearing 2))

   ; cut out the upper part of the main cup sphere
   (translate [0, 0, (+ (/ outer-width 2) vertical-hold)]
              (cylinder (/ outer-width 2) outer-width))))




; We know the ball will sit approx bearing-protrude over the sensor holder. Eliminate the bottom and make it square
; up to that point with trim
(def trim (- (+ holder-thickness bearing-protrude) 0.5))
(def bottom-trim-origin [0 0 (- (- (/ outer-width 2) (/ trim 2)))])
(def bottom-trim ; trim the bottom off of the cup to get a lower profile
  (translate bottom-trim-origin (cube outer-width outer-width trim)))

(def holder-negatives
  (union
   dowells
   bottom-trim))
(def cup-bottom
  (translate [0 0 (- (- (/ outer-width 2) (/ trim 2)))] (cube outer-width outer-width trim)))


(defn clearance [extrax extray extraz]
  (translate [0 0 (/ extraz 2)]
             (cube (+ keyswitch-width extrax) (+ keyswitch-width extray) extraz)))

(def thumb-key-clearance
  (union
   (thumb-1x-layout (clearance 0 0 30))
   (thumb-15x-layout (rotate (/ π 2) [0 0 1] (clearance 2.5 2.5 30)))))

(def key-clearance
  (union
   (apply union
          (for [column columns
                row    rows
                :when  (or (.contains [2 3] column)
                           (not= row lastrow))]
            (->> (clearance keyswitch-width keyswitch-width 30)
                 (key-place column row))))))

(defn trackball-mount-rotate [thing]
  (rotate (deg2rad -12) [0 0 1]
          (rotate (deg2rad 34) [1 0 0]
                  (rotate (deg2rad -39) [0 1 0] thing))))

(def sensor-length 28)
(def sensor-width 22)
(def sensor-holder-width (/ sensor-width 2))
(def sensor-height 7)
(def sensor-holder-arm
  (translate [0 -0.5 0]
             (union
              (translate [0 (- (/ 4 2) (/ 1 2)) 1] (cube sensor-holder-width 4 2))
              (translate [0 0 (- (/ sensor-height 2))] (cube sensor-holder-width 1 sensor-height))
              (translate [0 (- (/ 4 2) (/ 1 2)) (- (+ sensor-height (/ 1 2)))]
                         (cube sensor-holder-width 4 1)))))
(def sensor-holder
  (translate (map + bottom-trim-origin [0 0 (/ trim 2)])
             (union
              (translate [0 (- (/ sensor-length 2)) 0] sensor-holder-arm)
              (->>
               sensor-holder-arm
               (mirror [0 1 0])
               (translate [0 (/ sensor-length 2) 0])))))

(defn sensor-hole-angle [shape]
  (->> shape
       (rotate (deg2rad -55) [0 1 0])
       (rotate (deg2rad 40) [0 0 1])))
(defn dowell-angle [shape]
  (->> shape
       (rotate (deg2rad (+ 90 35)) [0 0 1])
       (rotate (deg2rad -30) [0 1 0])
       (rotate (deg2rad 25) [1 0 0])))

(def rotated-dowells
  (dowell-angle
   (translate [0 0 (- (/ holder-thickness 2))] dowells)))

(def rotated-bottom-trim (sensor-hole-angle bottom-trim))

; This makes sure we can actually insert the trackball by leaving a column a little wider than it's width
(def trackball-insertion-cyl
  (dowell-angle
    (translate [0 0 (- (/ trackball-width 2) (/ holder-thickness 2))]
               (cylinder (+ (/ trackball-width 2) 1) (+ (/ outer-width 2) 10)))))

(def trackball-raise (+ bearing-protrude 0.5))

(defn filler-rotate [p]
  (->> p
       (trackball-mount-rotate)
       ;                       (rotate (deg2rad 0) [0 1 0])
       (rotate (deg2rad 20) [0 0 1])))
       ;                         (rotate (deg2rad -40) [1 0 0])))

(def filler-half-circle
  (->>
    (difference
     (sphere (/ trackball-width-plus-bearing 2))
     (translate [0 0 (+ (/ outer-width 2) vertical-hold)]
                (cylinder (/ outer-width 2) outer-width))
     ; cut out the upper part of the main cup spher)
    (translate [0 0 trackball-raise]))
    filler-rotate))

(def trackball-mount
  (union
   (difference
    (union
     (trackball-mount-rotate cup)
     (filler-rotate cup))
    ; subtract out room for the axels
    rotated-dowells
    ; Subtract out the bottom trim clearing a hole for the sensor
    rotated-bottom-trim)
   (sensor-hole-angle sensor-holder)))

(def raised-trackball
  (translate [0 0 trackball-raise] (sphere (+ (/ trackball-width 2) 0.5))))

(def trackball-origin (map + thumb-tip-origin [-8.5 10 -5]))

(def test-holder
  (difference
   cup
   holder-negatives))

(def test-ball (sphere (/ trackball-width 2)))

(def test-holder-with-ball
  (union
   (translate [0 0 (- (/ holder-thickness 2))] cup)
   test-ball))


(def case-filler-cup
  (difference (translate trackball-origin filler-half-circle)
              key-clearance
              thumb-key-clearance
              (translate trackball-origin rotated-dowells)))

(def trackball-to-case
  (difference
    (union
     ; Trackball mount to left outside of case
     (hull
      (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) big-boi-web-post))
      case-filler-cup)
     ; Gap between trackball mount and top key
     (hull
      (key-place 0 cornerrow web-post-bl)
      (key-place 0 cornerrow web-post-br)
      (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) big-boi-web-post)))
     ; Between the trackball and the outside of the case near the bottom, to ensure a nice seal
     (hull
      (bottom 25
              (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) big-boi-web-post)))
      (translate trackball-origin (trackball-mount-rotate cup))))
    (translate trackball-origin rotated-dowells)
    (translate trackball-origin rotated-bottom-trim)))


(def trackball-walls
  (union
   ; clunky bit on the top left thumb connection  (normal connectors don't work well)
   ; merging with hulls to the trackball mount
   (difference
    (union
     ; Thumb to rest of case
     (bottom-hull
      (bottom 25
              (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) big-boi-web-post)))
      ;             (left-key-place cornerrow -1 (translate (wall-locate3 -1 0) web-post))
      (thumb-bl-place web-post-tr)
      (thumb-bl-place web-post-tl)))
    key-clearance
    thumb-key-clearance
    (translate trackball-origin rotated-bottom-trim)
    (translate trackball-origin rotated-dowells))))


(def trackball-subtract
  (union
   ; Subtract out the actual trackball
   (translate trackball-origin (dowell-angle raised-trackball))
   ; Subtract out space for the cup, because sometimes things from the keyboard creep in
   (translate trackball-origin (sphere (/ trackball-width-plus-bearing 2)))
   ; Just... double check that we have the full dowell negative
   (translate trackball-origin rotated-dowells)))

(def trackball-mount-translated-to-model
  (difference
   (union
    (translate trackball-origin trackball-mount)
    trackball-walls
    trackball-to-case)
   trackball-subtract
   key-clearance
   thumb-key-clearance
   (translate trackball-origin trackball-insertion-cyl)))

(spit "things/trackball-test.scad"
      (write-scad
       (difference
        (union
         trackball-mount-translated-to-model
         trackball-walls)
        trackball-subtract
        thumb-key-clearance
        (translate [0 0 -20] (cube 350 350 40)))))

